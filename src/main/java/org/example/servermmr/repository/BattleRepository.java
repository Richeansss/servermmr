package org.example.servermmr.repository;

import org.example.servermmr.model.Battle;
import org.example.servermmr.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, String> {
    List<Battle> findByIsActiveTrue();
    Optional<Battle> findByPlayer1OrPlayer2(Player player1, Player player2);
}