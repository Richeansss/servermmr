package org.example.servermmr;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpInteractiveClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9876;
    private static String playerName = "";
    private static String token = "";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(3000); // таймаут 3 сек

            System.out.println("🎮 UDP Клиент готов. Введите команды (JOIN, COUNT, MOVE, LEAVE):");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("🚪 Завершение клиента.");
                    break;
                }

                String[] parts = input.split(" ");
                String command = parts[0].toUpperCase();

                String messageToSend = null;

                switch (command) {
                    case "JOIN":
                        if (parts.length == 2) {
                            playerName = parts[1];
                            messageToSend = "JOIN " + playerName;
                        } else {
                            System.out.println("❗ Пример: JOIN Sergo");
                        }
                        break;
                    case "START":
                        if (playerName.isEmpty() || token.isEmpty()) {
                            System.out.println("❗ Сначала выполните JOIN.");
                        } else {
                            messageToSend = "START " + playerName + " " + token;
                        }
                        break;
                    case "MOVE":
                        if (playerName.isEmpty() || token.isEmpty()) {
                            System.out.println("❗ Сначала выполните JOIN.");
                            continue;
                        }

                        if (parts.length == 3) {
                            try {
                                int x = Integer.parseInt(parts[1]);
                                int y = Integer.parseInt(parts[2]);
                                messageToSend = "MOVE " + playerName + " " + token + " " + x + " " + y;
                            } catch (NumberFormatException e) {
                                System.out.println("❗ Неверные координаты. Пример: MOVE 10 20");
                            }
                        } else {
                            System.out.println("❗ Пример: MOVE 10 20");
                        }
                        break;

                    case "COUNT":
                        messageToSend = "COUNT";
                        break;

                    case "LEAVE":
                        if (playerName.isEmpty()) {
                            System.out.println("❗ Сначала выполните JOIN.");
                        } else {
                            messageToSend = "LEAVE " + playerName;
                        }
                        break;

                    default:
                        System.out.println("❌ Неизвестная команда.");
                        break;
                }

                if (messageToSend != null) {
                    flushIncomingPackets(socket);
                    sendAndReceive(socket, messageToSend);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void flushIncomingPackets(DatagramSocket socket) {
        try {
            socket.setSoTimeout(100);
            byte[] buffer = new byte[1024];
            DatagramPacket flushPacket = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(flushPacket);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                socket.setSoTimeout(3000);
            } catch (Exception ignored) {}
        }
    }

    private static void sendAndReceive(DatagramSocket socket, String message) {
        try {
            InetAddress address = InetAddress.getByName(SERVER_HOST);
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, SERVER_PORT);
            socket.send(packet);
            System.out.println("📤 Отправлено: " + message);

            byte[] responseBuffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(response);

            String json = new String(response.getData(), 0, response.getLength());
            MessageEnvelope envelope = mapper.readValue(json, MessageEnvelope.class);

            if ("broadcast".equalsIgnoreCase(envelope.getType())) {
                System.out.println("🔔 [Broadcast] " + envelope.getMessage());
            } else if ("response".equalsIgnoreCase(envelope.getType())) {
                System.out.println("📩 Ответ от сервера: " + envelope.getMessage());
                if ("JOIN".equalsIgnoreCase(envelope.getCommand()) && "OK".equalsIgnoreCase(envelope.getStatus())) {
                    if (envelope.getToken() != null) {
                        token = envelope.getToken();
                        System.out.println("🔑 Сохранён токен: " + token);
                    }
                }
            } else {
                System.out.println("📩 Неизвестный формат ответа: " + json);
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

}