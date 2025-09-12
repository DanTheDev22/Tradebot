package com.Danthedev.tradebot.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
    @Column(name = "user_id",unique = true,nullable = false)
    private Long userId;

    @Column(name="user_name",nullable = false)
    private String username;

    @Column(nullable = false)
    private Boolean hasAccess = false;

    @Column(nullable = false)
    private LocalDateTime registered_at;
}
