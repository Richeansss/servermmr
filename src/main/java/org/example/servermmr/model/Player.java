package org.example.servermmr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "players",
        indexes = {
                @Index(name = "idx_token", columnList = "token"),
                @Index(name = "idx_name", columnList = "name")
        }
)
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

    private int x = 0;
    private int y = 0;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime tokenExpiry;

    private LocalDateTime lastSeen;

    public Player(String name, InetAddress address, int port) {
        this.name = name;
        this.ip = address.getHostAddress();
        this.port = port;
        this.x = 0;
        this.y = 0;
        refreshToken();
        this.lastSeen = LocalDateTime.now();
    }

    public boolean isTokenValid() {
        return tokenExpiry != null && LocalDateTime.now().isBefore(tokenExpiry);
    }

    public void refreshToken() {
        this.token = generateToken();
        this.tokenExpiry = LocalDateTime.now().plusDays(7);
    }

    private String generateToken() {
        // Упрощённый UUID-based токен
        return UUID.randomUUID().toString();
    }

    @PrePersist
    @PreUpdate
    public void ensureToken() {
        if (token == null || tokenExpiry == null || !isTokenValid()) {
            refreshToken();
        }
        this.lastSeen = LocalDateTime.now();
    }
}
