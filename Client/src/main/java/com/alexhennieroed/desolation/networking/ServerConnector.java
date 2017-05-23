package main.java.com.alexhennieroed.desolation.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Connects the client to the server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerConnector {

    private final InetAddress address;
    private final DatagramSocket socket;
    private final int port;

    /**
     * Creates a new ServerConnector
     * @param socket the socket to connect to
     * @param hostname the hostname of the server
     * @param port the server's connection port
     * @throws IOException when IO issues occur
     */
    public ServerConnector(DatagramSocket socket, String hostname, int port)
            throws IOException {
        address = InetAddress.getByName(hostname);
        this.socket = socket;
        this.port = port;
    }

    /**
     * Connects to the server
     * @throws IOException when IO issues occur
     */
    public void connect() throws IOException {
        byte[] buf = new byte[256];
        sendPacket(buf, "");
        String received = receivePacket(buf);
        System.out.println(received);
        if (received.contains("Error")) {
            return;
        }
        int x = 0;
        while (x < 1000) {
            if (x % 10 == 0) {
                sendPacket(buf, Integer.toString(x));
                String rec = receivePacket(buf);
                System.out.println(rec);
            }
            x++;
        }
        sendPacket(buf, "disconnect");
    }

    /**
     * Receives a packet from the server
     * @param buf the buffer to hold stuff
     * @return the data from the packet
     * @throws IOException when there is an IO issue
     */
    private String receivePacket(byte[] buf) throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new String(packet.getData(),
                0, packet.getLength());
    }

    /**
     * Sends a packet to the server
     * @param buf the buffer to hold stuff
     * @param data the data to send
     * @throws IOException when there is an IO issue
     */
    private void sendPacket(byte[] buf, String data) throws IOException {
        buf = data.getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(buf, buf.length, address, port);
        socket.send(sendPacket);
    }

}
