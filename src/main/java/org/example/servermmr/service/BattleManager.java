package org.example.servermmr.service;

import org.example.servermmr.model.Battle;
import org.example.servermmr.model.Player;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BattleManager {
    private final Map<String, Battle> battles = new HashMap<>();
    private final Map<String, String> playerToBattle = new HashMap<>();

    public synchronized Battle startBattle(Player p1, Player p2) {
        Battle battle = new Battle(p1, p2);
        battles.put(battle.getId(), battle);
        playerToBattle.put(p1.getName(), battle.getId());
        playerToBattle.put(p2.getName(), battle.getId());
        return battle;
    }

    public Optional<Battle> getBattleByPlayer(String name) {
        String id = playerToBattle.get(name);
        return Optional.ofNullable(id).map(battles::get);
    }


    public void removeBattle(String battleId) {
        Battle battle = battles.remove(battleId);
        if (battle != null) {
            playerToBattle.remove(battle.getPlayer1().getName());
            playerToBattle.remove(battle.getPlayer2().getName());
        }
    }

    public String rollDigitForPlayer(String playerName) {
        Battle battle = battles.values().stream()
                .filter(b -> b.getPlayer1().getName().equals(playerName) || b.getPlayer2().getName().equals(playerName))
                .findFirst()
                .orElse(null);

        if (battle == null || !battle.isActive()) {
            return "Вы не участвуете в активной игре.";
        }

        Player player = battle.getPlayer1().getName().equals(playerName) ? battle.getPlayer1() : battle.getPlayer2();
        return battle.rollDigit(player);
    }

}
