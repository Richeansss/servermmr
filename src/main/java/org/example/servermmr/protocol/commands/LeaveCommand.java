package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class LeaveCommand implements CommandHandler {

    private final GameService gameService;

    public LeaveCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("LEAVE");

        if (parts.length == 2) {
            String name = parts[1];
            gameService.deletePlayer(name);
            response.setStatus("OK");
            response.setMessage("Вы вышли из игры.");
            gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
            gameService.broadcast("Игрок " + name + " вышел из игры.", socket);
        } else {
            response.setStatus("ERROR");
            response.setMessage("Неверный формат команды LEAVE. Ожидается: LEAVE <имя>");
            gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
        }
    }
}
