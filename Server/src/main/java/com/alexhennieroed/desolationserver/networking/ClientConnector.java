package main.java.com.alexhennieroed.desolationserver.networking;

import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.game.model.Character;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * Connects to the clients
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ClientConnector extends Thread {

    private final DatagramSocket socket;
    private final Server myServer;
    private final ServerThread mainThread;

    private InetAddress clientAddress;
    private int clientPort;
    private User currentUser;

    /**
     * Creates a new ServerThread
     * @param socket the socket of the server
     * @param packet the packet received from the client
     */
    ClientConnector(DatagramSocket socket, DatagramPacket packet,
                    ServerThread mainThread, Server server) {
        this.clientAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        this.socket = socket;
        this.mainThread = mainThread;
        this.myServer = server;
        this.currentUser = null;
    }

    @Override
    public void run() {
        try {
            sendPacket("connected");
            myServer.getLogger().logNetworkEvent(clientAddress.toString() +
                " connected to the server.");
            while (true) {
                String received = receivePacket();
                if (received != null) {
                    if (received.equals("disconnect")) {
                        myServer.getLogger().logNetworkEvent(clientAddress.toString() +
                            " disconnected from the server.");
                        break;
                    } else if (received.contains("LOGIN")) {
                        String[] credentials = received.split(":")[1].split("&");
                        User checkUser = new User(credentials[0], credentials[1]);
                        if (myServer.getDbconnector().checkUser(checkUser)) {
                            currentUser = myServer.getDbconnector().getUser(credentials[0]);
                            currentUser.setActive(true);
                            myServer.getLogger().logNetworkEvent(currentUser.getUsername() +
                                " logged in.");
                            sendPacket("login_success");
                            sendPacket(currentUser.getCharacter().toPacketData());
                        } else {
                            sendPacket("login_failure");
                        }
                    } else if (received.contains("LOGOUT")) {
                        currentUser.setActive(false);
                        myServer.getLogger().logNetworkEvent(currentUser.getUsername() +
                            " logged out.");
                        currentUser = null;
                        sendPacket("logout_success");
                    } else if (received.contains("MAKE USER")) {
                        String[] credentials = received.split(":")[1].split("&");
                        User addUser = new User(credentials[0], credentials[1]);
                        if (myServer.getDbconnector().addUser(addUser)) {
                            currentUser = addUser;
                            currentUser.setActive(true);
                            myServer.getLogger().logDatabaseEvent("Created new user " +
                                    currentUser.getUsername() + ".");
                            sendPacket("make_user_success");
                            sendPacket(currentUser.getCharacter().toPacketData());
                        } else {
                            sendPacket("make_user_failure");
                        }
                    } else if (received.contains("MAKE CHAR")) {
                        currentUser.setCharacter(new Character(received.split(":")[1]));
                        myServer.getDbconnector().updateUser(currentUser);
                        myServer.getLogger().logDatabaseEvent(currentUser.getUsername() +
                            " made a new character.");
                        sendPacket("make_character_success");
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
