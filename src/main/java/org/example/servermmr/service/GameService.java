package org.example.servermmr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.model.Battle;
import org.example.servermmr.model.Player;
import org.example.servermmr.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;


@Service
public class GameService {
    private final PlayerRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BattleManager battleManager;
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    public GameService(PlayerRepository repository, BattleManager battleManager) {
        this.repository = repository;
        this.battleManager = battleManager;
    }

    public void sendJsonResponse(MessageEnvelope envelope, InetAddress address, int port, DatagramSocket socket) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(envelope);
            socket.send(new DatagramPacket(data, data.length, address, port));
            logger.info(String.format("Отправлен JSON [%s] на %s:%d", envelope.getCommand(), address.getHostAddress(), port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message, DatagramSocket socket) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setType("broadcast");
        envelope.setMessage(message);

        for (Player player : repository.findAll()) {
            try {
                InetAddress address = InetAddress.getByName(player.getIp());
                sendJsonResponse(envelope, address, player.getPort(), socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateLastSeen(Player player) {
        player.setLastSeen(LocalDateTime.now());
        repository.save(player);
    }

    public long getOnlinePlayerCount() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(30);
        return repository.countByLastSeenAfter(cutoff);
    }

    public Player findOrCreatePlayer(String name, InetAddress address, int port) {
        Player player = repository.findByName(name).orElse(new Player(name, address, port));

        if (!player.isTokenValid()) {
            player.refreshToken();
        }

        player.setIp(address.getHostAddress());
        player.setPort(port);
        player.setLastSeen(LocalDateTime.now());

        return repository.save(player);
    }

    public void deletePlayer(String name) {
        repository.findByName(name).ifPresent(repository::delete);
    }

    public long getPlayerCount() {
        return repository.count();
    }

    public MessageEnvelope tryMovePlayerEnvelope(String name, String token, int x, int y, DatagramSocket socket) {
        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("MOVE");

        Optional<Player> playerOpt = repository.findByName(name);
        if (playerOpt.isEmpty()) {
            response.setStatus("ERROR");
            response.setMessage("Игрок не найден");
            return response;
        }

        Player player = playerOpt.get();
        if (!player.getToken().equals(token) || !player.isTokenValid()) {
            response.setStatus("ERROR");
            response.setMessage("Неверный или истекший токен");
            return response;
        }

        player.setX(x);
        player.setY(y);
        player.setLastSeen(LocalDateTime.now());
        repository.save(player);

        broadcast("Игрок " + name + " передвинулся в [" + x + "," + y + "]", socket);

        response.setStatus("OK");
        response.setMessage("Вы успешно передвинулись в [" + x + "," + y + "]");
        return response;
    }

    public Optional<Player> validatePlayer(String name, String token) {
        return repository.findByName(name)
                .filter(p -> p.getToken().equals(token) && p.isTokenValid());
    }


    private final Queue<Player> matchmakingQueue = new LinkedList<>();
    private final Set<String> inBattle = new HashSet<>();

    public synchronized MessageEnvelope tryStartMatchmaking(String name, String token, DatagramSocket socket) {
        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("START");

        Optional<Player> playerOpt = validatePlayer(name, token);
        if (playerOpt.isEmpty()) {
            response.setStatus("ERROR");
            response.setMessage("Неверный токен или игрок не найден");
            return response;
        }

        Player player = playerOpt.get();
        if (inBattle.contains(name)) {
            response.setStatus("ERROR");
            response.setMessage("Вы уже в бою.");
            return response;
        }

        if (matchmakingQueue.stream().anyMatch(p -> p.getName().equals(name))) {
            response.setStatus("ERROR");
            response.setMessage("Вы уже в очереди.");
            return response;
        }

        matchmakingQueue.add(player);

        if (matchmakingQueue.size() >= 2) {
            Player p1 = matchmakingQueue.poll();
            Player p2 = matchmakingQueue.poll();

            Battle battle = battleManager.startBattle(p1, p2);
            sendBattleStart(p1, p2, socket, battle.getId());
            sendBattleStart(p2, p1, socket, battle.getId());
        }


        response.setStatus("OK");
        response.setMessage("Вы добавлены в очередь на матчмейкинг.");
        return response;
    }

    private void sendBattleStart(Player self, Player opponent, DatagramSocket socket, String battleId) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setType("response");
        envelope.setCommand("START");
        envelope.setStatus("OK");
        envelope.setMessage("Матч найден! Противник: " + opponent.getName());
        envelope.setToken(battleId); // временно кладём ID боя в поле token, или заведи отдельное поле в envelope

        try {
            InetAddress address = InetAddress.getByName(self.getIp());
            logger.info(String.format("Отправка START-сообщения игроку %s [%s:%d] - противник %s, battleId = %s",
                    self.getName(), self.getIp(), self.getPort(), opponent.getName(), battleId));
            sendJsonResponse(envelope, address, self.getPort(), socket);
        } catch (Exception e) {
            logger.severe("Ошибка при отправке START-сообщения игроку " + self.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MessageEnvelope rollDigit(String name, String token) {
        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("ROLL");

        Optional<Player> playerOpt = validatePlayer(name, token);
        if (playerOpt.isEmpty()) {
            response.setStatus("ERROR");
            response.setMessage("Неверный токен или игрок не найден.");
            return response;
        }

        String result = battleManager.rollDigitForPlayer(name);

        response.setStatus("OK");
        response.setMessage(result);
        return response;
    }

}