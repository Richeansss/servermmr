package org.example.servermmr.repository;

import org.example.servermmr.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PlayerRepository extends JpaRepository<Player, Long> {
}

