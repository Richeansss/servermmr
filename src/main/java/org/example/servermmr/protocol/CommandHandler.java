package org.example.servermmr.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public interface CommandHandler {
    void handle(String[] parts, DatagramPacket packet, DatagramSocket socket);
}
