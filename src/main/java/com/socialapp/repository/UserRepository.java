package com.socialapp.repository;

import com.socialapp.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Dùng để tìm bot Groq khi khởi động app */
    Optional<User> findByUsernameAndIsBot(String username, Boolean isBot);

    /**
     * Tìm user theo username hoặc displayName (không phân biệt hoa thường).
     * Bỏ qua bot và user bị khóa. Giới hạn 20 kết quả.
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.isBot = false
              AND u.isActive = true
              AND (
                LOWER(u.username)    LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            ORDER BY u.username ASC
            """)
    List<User> searchByKeyword(@Param("q") String q, Pageable pageable);
}