package org.example.servermmr.network;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.servermmr.protocol.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Component
public class GameServer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    @Value("${game.server.port:9876}")
    private int port;

    @Value("${game.server.bufferSize:2048}")
    private int bufferSize;

    private final CommandDispatcher dispatcher;
    private volatile boolean running = true;
    private Thread serverThread;

    public GameServer(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostConstruct
    public void start() {
        serverThread = new Thread(this, "GameServerThread");
        serverThread.start();
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            log.info("✅ Game server started on port {}", port);

            while (running) {
                try {
                    byte[] buffer = new byte[bufferSize];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    dispatcher.dispatch(packet, socket);
                } catch (Exception e) {
                    if (running) {
                        log.error("Ошибка при обработке пакета", e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Не удалось запустить сервер", e);
        }

        log.info("🛑 Game server остановлен");
    }
}
