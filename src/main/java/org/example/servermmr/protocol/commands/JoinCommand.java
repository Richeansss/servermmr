package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.model.Player;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class JoinCommand implements CommandHandler {

    private final GameService gameService;

    public JoinCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        try {
            MessageEnvelope response = new MessageEnvelope();
            response.setType("response");
            response.setCommand("JOIN");

            if (parts.length == 2) {
                String playerName = parts[1];
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                Player player = gameService.findOrCreatePlayer(playerName, address, port);
                response.setStatus("OK");
                response.setMessage("Добро пожаловать, " + playerName + "!");
                response.setToken(player.getToken());

                gameService.sendJsonResponse(response, address, port, socket);
                gameService.broadcast("Игрок " + playerName + " вошел в игру.", socket);
            } else {
                response.setStatus("ERROR");
                response.setMessage("Неверный формат команды JOIN. Ожидается: JOIN <имя>");
                gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
