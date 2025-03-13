package org.example.servermmr.udp;

import org.springframework.stereotype.Component;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameServer implements Runnable {
    private static final int PORT = 9876;

    // Список активных игроков (Игрок -> IP, порт)
    private final ConcurrentHashMap<String, PlayerInfo> activePlayers = new ConcurrentHashMap<>();

    public GameServer() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[1024];
            System.out.println("✅ ИГРОВОЙ UDP сервер запущен на порту " + PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedData = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("📩 Получено: " + receivedData);

                handleMessage(receivedData, packet.getAddress(), packet.getPort(), socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String message, InetAddress address, int port, DatagramSocket socket) {
        String[] parts = message.split(" ");
        String command = parts[0];

        switch (command) {
            case "JOIN": // Вход в игру
                if (parts.length == 2) {
                    String playerName = parts[1];
                    activePlayers.put(playerName, new PlayerInfo(address, port, 0, 0));

                    System.out.println("👤 Игрок " + playerName + " вошел в игру.");
                    sendResponse("✅ Добро пожаловать, " + playerName + "!", address, port, socket);
                    broadcast("📢 Игрок " + playerName + " вошел в игру.", socket);
                }
                break;

            case "LEAVE": // Выход из игры
                if (parts.length == 2) {
                    String playerName = parts[1];
                    activePlayers.remove(playerName);

                    System.out.println("🚪 Игрок " + playerName + " вышел из игры.");
                    sendResponse("👋 Вы вышли из игры.", address, port, socket);
                    broadcast("📢 Игрок " + playerName + " вышел из игры.", socket);
                }
                break;

            case "COUNT": // Количество игроков
                int playerCount = activePlayers.size();
                sendResponse("🎮 Сейчас в игре: " + playerCount + " игроков.", address, port, socket);
                break;

            case "MOVE": // Движение игрока
                if (parts.length == 4) {
                    String playerName = parts[1];
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);

                    PlayerInfo player = activePlayers.get(playerName);
                    if (player != null) {
                        player.setX(x);
                        player.setY(y);
                        broadcast("🚀 " + playerName + " передвинулся в [" + x + "," + y + "]", socket);
                    }
                }
                break;

            default:
                sendResponse("❌ Неизвестная команда", address, port, socket);
        }
    }

    private void sendResponse(String message, InetAddress address, int port, DatagramSocket socket) {
        try {
            byte[] responseData = message.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
            socket.send(responsePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message, DatagramSocket socket) {
        activePlayers.forEach((name, player) -> sendResponse(message, player.getAddress(), player.getPort(), socket));
    }

    // Класс для хранения информации об игроках
    private static class PlayerInfo {
        private final InetAddress address;
        private final int port;
        private int x, y;

        public PlayerInfo(InetAddress address, int port, int x, int y) {
            this.address = address;
            this.port = port;
            this.x = x;
            this.y = y;
        }

        public InetAddress getAddress() { return address; }
        public int getPort() { return port; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
    }
}
