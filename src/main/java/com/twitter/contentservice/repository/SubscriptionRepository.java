package com.twitter.contentservice.repository;

import com.twitter.contentservice.entity.Subscription;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends CrudRepository<Subscription, UUID> {

    Optional<Subscription> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Modifying
    @Query("""
            INSERT INTO subscriptions (follower_id, following_id, created_at)
            VALUES (:followerId, :followingId, :createdAt)
            ON CONFLICT (follower_id, following_id) DO NOTHING
            """)
    int insertIfAbsent(@Param("followerId") UUID followerId,
                       @Param("followingId") UUID followingId,
                       @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query("DELETE FROM subscriptions WHERE follower_id = :followerId AND following_id = :followingId")
    int deleteByPair(@Param("followerId") UUID followerId,
                     @Param("followingId") UUID followingId);

    @Query("""
            SELECT * FROM subscriptions
            WHERE following_id = :userId
            ORDER BY created_at DESC, id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Subscription> findFollowers(@Param("userId") UUID userId,
                                     @Param("limit") int limit,
                                     @Param("offset") long offset);

    @Query("""
            SELECT * FROM subscriptions
            WHERE follower_id = :userId
            ORDER BY created_at DESC, id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Subscription> findFollowing(@Param("userId") UUID userId,
                                     @Param("limit") int limit,
                                     @Param("offset") long offset);
}
