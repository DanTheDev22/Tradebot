package com.Danthedev.Tradebot.repository;

import com.Danthedev.Tradebot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users (user_id,user_name,registered_at)" +
            "VALUES (:user_id,:user_name,:registered_at)" +
            "ON CONFLICT (user_id) DO NOTHING", nativeQuery = true)
    void insertIfNotExists(@Param("user_id") Long userId,
                           @Param("user_name") String user_name,
                           @Param("registered_at")LocalDateTime registered_at);

    User findByUserId(Long userId);

}
