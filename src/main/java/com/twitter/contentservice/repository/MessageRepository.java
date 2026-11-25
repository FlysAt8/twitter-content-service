package com.twitter.contentservice.repository;

import com.twitter.contentservice.entity.Message;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

    @Query("""
            SELECT * FROM messages
            WHERE author_id = :authorId
            ORDER BY created_at DESC, id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Message> findByAuthor(@Param("authorId") UUID authorId,
                               @Param("limit") int limit,
                               @Param("offset") long offset);

    @Query("""
            SELECT * FROM messages
            WHERE author_id = :userId
               OR author_id IN (SELECT following_id FROM subscriptions WHERE follower_id = :userId)
            ORDER BY created_at DESC, id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Message> findFeed(@Param("userId") UUID userId,
                           @Param("limit") int limit,
                           @Param("offset") long offset);
}
