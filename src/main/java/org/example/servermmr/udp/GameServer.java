//package org.example.servermmr.udp;
//
//import org.example.servermmr.model.Player;
//import org.example.servermmr.repository.PlayerRepository;
//import org.springframework.stereotype.Component;
//
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//public class GameServer implements Runnable {
//    private static final int PORT = 9876;
//    private final PlayerRepository playerRepository;
//
//    public GameServer(PlayerRepository playerRepository) {
//        this.playerRepository = playerRepository;
//        new Thread(this).start();
//    }
//
//    @Override
//    public void run() {
//        try (DatagramSocket socket = new DatagramSocket(PORT)) {
//            byte[] buffer = new byte[1024];
//            System.out.println("✅ ИГРОВОЙ UDP сервер запущен на порту " + PORT);
//
//            while (true) {
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                socket.receive(packet);
//                String receivedData = new String(packet.getData(), 0, packet.getLength()).trim();
//                System.out.println("📩 Получено: " + receivedData);
//
//                handleMessage(receivedData, packet.getAddress(), packet.getPort(), socket);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleMessage(String message, InetAddress address, int port, DatagramSocket socket) {
//        String[] parts = message.split(" ");
//        String command = parts[0];
//
//        switch (command) {
//            case "JOIN": // Вход в игру
//                if (parts.length == 2) {
//                    String playerName = parts[1];
//                    Player player = playerRepository.findByName(playerName).orElse(new Player(playerName, address, port));
//
//                    // Проверяем токен
//                    if (player.getToken() == null || player.getTokenExpiry().isBefore(LocalDateTime.now())) {
//                        player.refreshToken();
//                    }
//
//                    playerRepository.save(player);
//                    System.out.println("👤 Игрок " + playerName + " вошел в игру.");
//
//                    // Отправляем игроку его токен
//                    sendResponse("✅ Добро пожаловать, " + playerName + "! Ваш токен: " + player.getToken(), address, port, socket);
//                    broadcast("📢 Игрок " + playerName + " вошел в игру.", socket);
//                }
//                break;
//
//            case "LEAVE": // Выход из игры
//                if (parts.length == 2) {
//                    String playerName = parts[1];
//                    playerRepository.findByName(playerName).ifPresent(playerRepository::delete);
//
//                    System.out.println("🚪 Игрок " + playerName + " вышел из игры.");
//                    sendResponse("👋 Вы вышли из игры.", address, port, socket);
//                    broadcast("📢 Игрок " + playerName + " вышел из игры.", socket);
//                }
//                break;
//
//            case "COUNT": // Количество игроков
//                long playerCount = playerRepository.count();
//                sendResponse("🎮 Сейчас в игре: " + playerCount + " игроков.", address, port, socket);
//                break;
//
//            case "MOVE": // Движение игрока (MOVE name token x y)
//                if (parts.length == 4) {
//                    String playerName = parts[1];
//                    String token = parts[2];
//                    int x = Integer.parseInt(parts[3]);
//                    int y = Integer.parseInt(parts[4]);
//
//                    Optional<Player> playerOpt = playerRepository.findByName(playerName);
//                    if (playerOpt.isPresent()) {
//                        Player player = playerOpt.get();
//                        if (player.getToken().equals(token) && player.getTokenExpiry().isAfter(LocalDateTime.now())) {
//                            player.setX(x);
//                            player.setY(y);
//                            playerRepository.save(player);
//                            broadcast("🚀 " + playerName + " передвинулся в [" + x + "," + y + "]", socket);
//                        } else {
//                            sendResponse("❌ Неверный или истекший токен!", address, port, socket);
//                        }
//                    } else {
//                        sendResponse("❌ Игрок не найден!", address, port, socket);
//                    }
//                }
//                break;
//
//            default:
//                sendResponse("❌ Неизвестная команда", address, port, socket);
//        }
//    }
//
//    private void sendResponse(String message, InetAddress address, int port, DatagramSocket socket) {
//        try {
//            byte[] responseData = message.getBytes();
//            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
//            socket.send(responsePacket);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void broadcast(String message, DatagramSocket socket) {
//        List<Player> players = playerRepository.findAll();
//        players.forEach(player -> {
//            try {
//                InetAddress address = InetAddress.getByName(player.getIp());
//                sendResponse(message, address, player.getPort(), socket);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//}
