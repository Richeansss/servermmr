package org.example.servermmr.protocol.commands;

import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.protocol.CommandHandler;
import org.example.servermmr.service.GameService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class CountCommand implements CommandHandler {

    private final GameService gameService;

    public CountCommand(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(String[] parts, DatagramPacket packet, DatagramSocket socket) {
        long onlineCount = gameService.getOnlinePlayerCount();

        MessageEnvelope response = new MessageEnvelope();
        response.setType("response");
        response.setCommand("COUNT");
        response.setStatus("OK");
        response.setMessage("Сейчас в игре: " + onlineCount + " игроков.");

        gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
    }
}
