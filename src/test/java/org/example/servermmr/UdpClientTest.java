package org.example.servermmr;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClientTest {
    public static void main(String[] args) {
        try {
            String message = "JOIN Sergo"; // команда
            InetAddress address = InetAddress.getByName("localhost");
            int port = 9876;

            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = message.getBytes();

            // Отправка пакета
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println("📤 Отправлено: " + message);

            // Получение ответа
            byte[] responseBuffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.setSoTimeout(2000); // таймаут 2 секунды

            socket.receive(response);
            String responseText = new String(response.getData(), 0, response.getLength());
            System.out.println("📩 Ответ от сервера: " + responseText);

            socket.close();
        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        }
    }
}
