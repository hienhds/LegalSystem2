package com.example.userservice.auth.service;



import com.example.userservice.auth.dto.request.LoginRequest;
import com.example.userservice.auth.dto.response.JwtResponse;
import com.example.userservice.auth.entity.RefreshToken;
import com.example.userservice.auth.entity.UserToken;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.common.security.JwtTokenProvider;
import com.example.userservice.common.service.EmailService;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
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
        String accessToken = jwtProvider.generateTokenWithProfile(user);
        String ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");
        String refreshToken = refreshTokenService.createRefreshToken(user, ip, ua);

        log.info("User {} logged in successfully.", user.getEmail());

        return new JwtResponse(accessToken, refreshToken, jwtProvider.getJwtExpirationMs());
    }

    public JwtResponse refresh(String rawRefreshToken, HttpServletRequest servletRequest) {
        RefreshToken token = refreshTokenService.validateRefreshToken(rawRefreshToken);
        User user = token.getUser();

        String newAccess = jwtProvider.generateTokenWithProfile(user);
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
