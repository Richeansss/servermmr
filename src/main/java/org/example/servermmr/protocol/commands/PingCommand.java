package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.model.Player;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Optional;

public class PingCommand implements CommandHandler {

    private final GameService gameService;

    public PingCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("PING");

        if (parts.length == 3) {
            String name = parts[1];
            String token = parts[2];

            Optional<Player> playerOpt = gameService.validatePlayer(name, token);
            if (playerOpt.isPresent()) {
                gameService.updateLastSeen(playerOpt.get());
                response.setStatus("OK");
                response.setMessage("PONG");
            } else {
                response.setStatus("ERROR");
                response.setMessage("Неверный токен или игрок не найден");
            }

        } else {
            response.setStatus("ERROR");
            response.setMessage("Формат: PING <name> <token>");
        }

        gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
    }
}
