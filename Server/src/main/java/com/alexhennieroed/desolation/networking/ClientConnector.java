package main.java.com.alexhennieroed.desolation.networking;

import main.java.com.alexhennieroed.desolation.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Connects to the clients
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ClientConnector extends Thread {

    private final DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
    public ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new ServerThread
     * @param socket the socket of the server
     * @param packet the packet received from the client
     */
    public ClientConnector(DatagramSocket socket, DatagramPacket packet) {
        this.clientAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            sendPacket("Connection Successful.");
            while (true) {
                String received = receivePacket();
                if (received != null) {
                    if (received.equals("disconnect")) {
                        break;
                    }
                    System.out.println(received);
                    int rec = Integer.parseInt(received);
                    String sendit = Integer.toString(rec * 21);
                    sendPacket(sendit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives a packet from the client
     * @return the data from the packet
     * @throws IOException when there is an IO issue
     */
    private String receivePacket() throws IOException {
        //lock.readLock().lock();
        DatagramPacket packet = Server.clientBuffers.get(clientAddress);
        //lock.readLock().unlock();
        if (packet != null) {
            return new String(packet.getData(),
                    0, packet.getLength());
        }
        return null;
    }

    /**
     * Sends a packet to the client
     * @param data the data to send
     * @throws IOException when there is an IO issue
     */
    private void sendPacket(String data) throws IOException {
        byte[] buf = data.getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(buf, buf.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

}
