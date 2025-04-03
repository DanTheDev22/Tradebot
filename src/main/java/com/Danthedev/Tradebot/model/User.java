package com.Danthedev.Tradebot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
    @Column(name = "user_id")
    private int userId;

    @Column(name = "user_name")
    private String userName;

    private LocalDateTime registered_at;
}
