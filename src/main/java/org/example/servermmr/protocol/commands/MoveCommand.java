package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MoveCommand implements CommandHandler {

    private final GameService gameService;

    public MoveCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("MOVE");

        if (parts.length == 5) {
            String name = parts[1];
            String token = parts[2];

            try {
                int x = Integer.parseInt(parts[3]);
                int y = Integer.parseInt(parts[4]);

                MessageEnvelope result = gameService.tryMovePlayerEnvelope(name, token, x, y, socket);
                gameService.sendJsonResponse(result, packet.getAddress(), packet.getPort(), socket);

            } catch (NumberFormatException e) {
                response.setStatus("ERROR");
                response.setMessage("Координаты должны быть числами");
                gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
            }
        } else {
            response.setStatus("ERROR");
            response.setMessage("Неверный формат команды MOVE. Ожидается: MOVE <name> <token> <x> <y>");
            gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
        }
    }
}
