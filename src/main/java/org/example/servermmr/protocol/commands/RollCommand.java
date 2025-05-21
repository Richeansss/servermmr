package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RollCommand implements CommandHandler {
    private final GameService gameService;

    public RollCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        if (parts.length != 3) {
            MessageEnvelope error = new MessageEnvelope();
            error.setType("response");
            error.setCommand("ROLL");
            error.setStatus("ERROR");
            error.setMessage("Неверный формат команды ROLL. Ожидается: ROLL <name> <token>");
            gameService.sendJsonResponse(error, packet.getAddress(), packet.getPort(), socket);
            return;
        }

        String name = parts[1];
        String token = parts[2];

        MessageEnvelope response = gameService.rollDigit(name, token);
        gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
    }
}
