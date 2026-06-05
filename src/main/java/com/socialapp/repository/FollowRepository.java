package com.socialapp.repository;

import com.socialapp.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /** Số người đang follow userId */
    long countByFollowingId(Long userId);

    /** Số người userId đang follow */
    long countByFollowerId(Long userId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /** Danh sách Follow record mà following = userId (tức là followers của userId) */
    List<Follow> findByFollowingId(Long userId);

    /** Danh sách Follow record mà follower = userId (tức là người userId đang follow) */
    List<Follow> findByFollowerId(Long userId);
}