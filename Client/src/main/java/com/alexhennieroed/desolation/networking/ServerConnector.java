package main.java.com.alexhennieroed.desolation.networking;

import javafx.application.Platform;
import main.java.com.alexhennieroed.desolation.Client;
import main.java.com.alexhennieroed.desolation.Settings;
import sun.plugin2.os.windows.SECURITY_ATTRIBUTES;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Connects the client to the server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerConnector extends Thread {

    private final InetAddress address;
    private final DatagramSocket socket;
    private final int port;
    private final Client myClient;
    /**
     * Creates a new ServerConnector
     * @param socket the socket to connect to
     * @param hostname the hostname of the server
     * @param port the server's connection port
     * @throws IOException when IO issues occur
     */
    public ServerConnector(DatagramSocket socket, String hostname, int port, Client client)
            throws IOException {
        this.address = InetAddress.getByName(hostname);
        this.socket = socket;
        this.port = port;
        this.myClient = client;
    }

    @Override
    public void run() {
        byte[] buf = new byte[256];
        try {
            if (!connect(buf)) {
                return;
            }
            socket.setSoTimeout(0);
            while (true) {
                String received = receivePacket(buf);
                if (received != null) {
                    System.out.println(received);
                    if (received.equals("login_success") ||
                            received.equals("make_user_success")) {
                        myClient.setState(Client.ClientState.IN_GAME);
                        Platform.runLater(() -> myClient.setScreen("GameScreen"));
                    } else if (received.contains("failure")) {
                        System.out.println(received);
                    } else if (received.equals("logout_success")) {
                        myClient.setState(Client.ClientState.IN_INIT_SCREEN);
                        Platform.runLater(() -> myClient.setScreen("HomeScreen"));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    /**
     * Connects to the server
     * @throws IOException when IO issues occur
     */
    private boolean connect(byte[] buf) throws IOException {
        try {
            socket.setSoTimeout(Settings.TIMEOUT);
            sendPacket(buf, "connect");
            String received = receivePacket(buf);
            System.out.println(received);
            if (received.contains("Error")) {
                System.out.println(received);
                return false;
            } else if (received.contains("connected")) {
                myClient.setState(Client.ClientState.IN_INIT_SCREEN);
                return true;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Error: connection timeout.");
            myClient.setState(Client.ClientState.CONNECTION_TIMEOUT);
            return false;
        }
        return false;
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

    public void sendData(String data) {
        byte[] buf = new byte[256];
        //TODO
        //Check that the data is acceptable
        try {
            sendPacket(buf, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
