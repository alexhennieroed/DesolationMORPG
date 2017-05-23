package main.java.com.alexhennieroed.desolation.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Connects to the clients
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ClientConnector extends Thread {

    private final DatagramSocket socket;
    private final ServerThread mainThread;

    private InetAddress clientAddress;
    private int clientPort;
    private User currentUser;

    /**
     * Creates a new ServerThread
     * @param socket the socket of the server
     * @param packet the packet received from the client
     */
    ClientConnector(DatagramSocket socket, DatagramPacket packet, ServerThread mainThread) {
        this.clientAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        this.socket = socket;
        this.mainThread = mainThread;
        this.currentUser = null;
    }

    @Override
    public void run() {
        try {
            sendPacket("connected");
            while (true) {
                String received = receivePacket();
                if (received != null) {
                    if (received.equals("disconnect")) {
                        break;
                    } else if (received.contains("LOGIN")) {
                        System.out.println("Logging in");
                        String[] credentials = received.split(":")[1].split("&");
                        User checkUser = new User(credentials[0], credentials[1]);
                        if (mainThread.getDbconnector().checkUser(checkUser)) {
                            currentUser = mainThread.getDbconnector().getUser(credentials[0]);
                            sendPacket("login_success");
                        } else {
                            sendPacket("login_failure");
                        }
                    } else if (received.contains("LOGOUT")) {
                        currentUser = null;
                        System.out.println("Logging out");
                        sendPacket("logout_success");
                    } else if (received.contains("MAKE USER")) {
                        System.out.println("Making user");
                        String[] credentials = received.split(":")[1].split("&");
                        User addUser = new User(credentials[0], credentials[1]);
                        if (mainThread.getDbconnector().addUser(addUser)) {
                            currentUser = addUser;
                            sendPacket("make_user_success");
                        } else {
                            System.out.println("User already exists.");
                            sendPacket("make_user_failure");
                        }
                    }
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
        DatagramPacket packet = mainThread.getClientBuffers().get(clientAddress);
        if (packet != null) {
            String returnString = new String(packet.getData(),
                    0, packet.getLength());
            mainThread.getClientBuffers().put(clientAddress, null);
            return returnString;
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

    /**
     * Returns the current user
     * @return the current user
     */
    public User getCurrentUser() { return currentUser; }

}
