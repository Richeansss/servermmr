package org.example.servermmr.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.servermmr.MessageEnvelope;
import org.example.servermmr.protocol.commands.*;
import org.example.servermmr.service.GameService;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommandDispatcher {
    private final Map<String, CommandHandler> handlers = new HashMap<>();
    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommandDispatcher(GameService gameService) {
        this.gameService = gameService;
        handlers.put("JOIN", new JoinCommand(gameService));
        handlers.put("LEAVE", new LeaveCommand(gameService));
        handlers.put("COUNT", new CountCommand(gameService));
        handlers.put("MOVE", new MoveCommand(gameService));
        handlers.put("PING", new PingCommand(gameService));
        handlers.put("START", new StartCommand(gameService));
    }

    public void dispatch(DatagramPacket packet, DatagramSocket socket) {
        try {
            String message = new String(packet.getData(), 0, packet.getLength()).trim();
            String[] parts = message.split(" ");
            String commandKey = parts[0].toUpperCase();

            CommandHandler handler = handlers.get(commandKey);
            if (handler != null) {
                handler.handle(parts, packet, socket);
            } else {
                MessageEnvelope response = new MessageEnvelope();
                response.setType("response");
                response.setCommand(commandKey);
                response.setStatus("ERROR");
                response.setMessage("Неизвестная команда");
                gameService.sendJsonResponse(response, packet.getAddress(), packet.getPort(), socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
