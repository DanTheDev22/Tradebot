package com.Danthedev.Tradebot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
    @Column(name = "user_id",unique = true,nullable = false)
    private final Long userId;

    @Column(name="userName",nullable = false)
    private final String userName;

    @Column(nullable = false)
    private final LocalDateTime registered_at;
}
