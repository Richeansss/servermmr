package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StartCommand implements CommandHandler {
    private final GameService gameService;

    public StartCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        if (parts.length != 3) {
            MessageEnvelope error = new MessageEnvelope();
            error.setType("response");
            error.setCommand("START");
            error.setStatus("ERROR");
            error.setMessage("Неверный формат команды START. Ожидается: START <name> <token>");
            gameService.sendJsonResponse(error, packet.getAddress(), packet.getPort(), socket);
            return;
        }

        String name = parts[1];
        String token = parts[2];

        MessageEnvelope response = gameService.tryStartMatchmaking(name, token, socket);
        gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
    }
}
