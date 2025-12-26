package com.example.backend.auth.service;


import com.example.backend.auth.dto.request.LoginRequest;
import com.example.backend.auth.dto.response.JwtResponse;
import com.example.backend.auth.entity.RefreshToken;
import com.example.backend.common.service.EmailService;
import com.example.backend.user.entity.User;
import com.example.backend.auth.entity.UserToken;
import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;

    public JwtResponse login(LoginRequest request, HttpServletRequest servletRequest) {

        // 1️⃣ Lấy user trước để kiểm tra trạng thái
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy người dùng."));

        if (!user.isActive()) {
            // Nếu chưa kích hoạt thì gửi lại email xác nhận
            UserToken token = tokenService.createVerificationToken(user);
            emailService.sendVerificationEmail(user.getEmail(), token.getTokenHash());
            throw new AppException(ErrorType.FORBIDDEN, "Tài khoản chưa được kích hoạt. Đã gửi lại email xác nhận!");
        }

        // 2️⃣ Xác thực thông tin đăng nhập
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorType.UNAUTHORIZED, "Email hoặc mật khẩu không đúng!");
        } catch (LockedException e) {
            throw new AppException(ErrorType.FORBIDDEN, "Tài khoản đã bị khóa!");
        } catch (DisabledException e) {
            throw new AppException(ErrorType.FORBIDDEN, "Tài khoản bị vô hiệu hóa!");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3️⃣ Sinh access token + refresh token
        String accessToken = jwtProvider.generateToken(user.getEmail());
        String ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");
        String refreshToken = refreshTokenService.createRefreshToken(user, ip, ua);

        log.info("User {} logged in successfully.", user.getEmail());

        return new JwtResponse(accessToken, refreshToken, jwtProvider.getJwtExpirationMs());
    }

    public JwtResponse refresh(String rawRefreshToken, HttpServletRequest servletRequest) {
        RefreshToken token = refreshTokenService.validateRefreshToken(rawRefreshToken);
        User user = token.getUser();

        String newAccess = jwtProvider.generateToken(user.getEmail());
        String ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");

        String newRefresh = refreshTokenService.rotateRefreshToken(token, ip, ua);

        return new JwtResponse(newAccess, newRefresh, jwtProvider.getJwtExpirationMs());
    }

    public void logout(String rawRefreshToken) {
        RefreshToken token = refreshTokenService.validateRefreshToken(rawRefreshToken);
        refreshTokenService.revokeRefreshToken(token);
        log.info("User {} logged out successfully.", token.getUser().getEmail());
    }
}
