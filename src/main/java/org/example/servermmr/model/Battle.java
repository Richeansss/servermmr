package org.example.servermmr.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Battle {
    private final String id = UUID.randomUUID().toString();
    private final Player player1;
    private final Player player2;
    private final LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;
    private String result; // "P1_WIN", "P2_WIN", "DRAW", "ABORTED"
    private boolean isActive = true;
}

