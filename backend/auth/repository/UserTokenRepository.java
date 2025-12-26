package com.example.backend.auth.repository;

import com.example.backend.auth.entity.UserToken;
import com.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    // Sửa 'AndAnd' thành 'And'
    Optional<UserToken> findByTokenHashAndTokenType(String tokenHash, String tokenType);

    // Sửa tên hàm delete cho chuẩn (bỏ 'UserToken' thừa, bỏ 'And' thừa)
    // Lưu ý: Hàm delete thường trả về void hoặc Long/Integer (số dòng đã xóa)
    void deleteByUserAndTokenType(User user, String tokenType);
}
