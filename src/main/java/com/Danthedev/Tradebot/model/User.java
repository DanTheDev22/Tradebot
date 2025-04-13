package com.Danthedev.Tradebot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
    @Column(name = "user_id",unique = true)
    private Long userId;

    @Column(name="userName")
    private String userName;

    private LocalDateTime registered_at;
}
