package org.example.servermmr.repository;

import org.example.servermmr.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
    long countByLastSeenAfter(LocalDateTime time);
}

