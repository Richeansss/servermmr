package org.example.servermmr.model;

import jakarta.persistence.*;
import lombok.Data;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private int port;

    private int x;
    private int y;

    @Column(unique = true)
    private String token; // Токен для авторизации

    private LocalDateTime tokenExpiry; // Время истечения токена

    public Player() {}

    public Player(String name, InetAddress address, int port) {
        this.name = name;
        this.ip = address.getHostAddress();
        this.port = port;
        this.x = 0;
        this.y = 0;
        this.token = generateToken();
        this.tokenExpiry = LocalDateTime.now().plusDays(7); // Токен действует 7 дней
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public boolean isTokenValid() {
        return tokenExpiry != null && LocalDateTime.now().isAfter(tokenExpiry);
    }

    public void refreshToken() {
        this.token = generateToken();
        this.tokenExpiry = LocalDateTime.now().plusDays(7);
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
}
