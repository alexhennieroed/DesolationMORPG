package main.java.com.alexhennieroed.desolationserver.networking;

import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.game.model.Character;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private boolean disconnected;
    private boolean isInGame;
    private boolean[] moving; // [forward, left, back, right]
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

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
        this.disconnected = false;
        this.moving = new boolean[4];
        Arrays.fill(moving, false);
    }

    @Override
    public void run() {
        try {
            sendPacket("connected");
            myServer.getLogger().logNetworkEvent(clientAddress.toString() +
                " connected to the server.");
            while (!disconnected) {
                String received = receivePacket();
                if (received != null) {
                    if (received.equals("disconnect")) {
                        myServer.getLogger().logNetworkEvent(clientAddress.toString() +
                            " disconnected from the server.");
                        break;
                    } else if (received.contains("LOGIN")) {
                        String[] credentials = received.split(":")[1].split("&");
                        User checkUser = new User(credentials[0], credentials[1]);
                        if (myServer.getDbconnector().checkUser(checkUser) &&
                                checkBlacklist(checkUser)) {
                            currentUser = myServer.getDbconnector().getUser(credentials[0]);
                            currentUser.setActive(true);
                            myServer.getLogger().logNetworkEvent(currentUser.getUsername() +
                                " logged in.");
                            sendPacket("login_success");
                            sendPacket(currentUser.getCharacter().toPacketData());
                        } else {
                            myServer.getLogger().logNetworkEvent(checkUser.getUsername() +
                                " failed to log in due to incorrect password.");
                            sendPacket("login_failure");
                        }
                    } else if (received.contains("LOGOUT")) {
                        currentUser.setActive(false);
                        myServer.getDbconnector().updateUser(currentUser);
                        myServer.getLogger().logDatabaseEvent(currentUser.getUsername() +
                            " was updated in the database.");
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
                            " made a new character " + currentUser.getCharacter() + ".");
                        sendPacket(currentUser.getCharacter().toPacketData());
                        sendPacket("make_character_success");
                    } else if (received.contains("client_message")) {
                        String message = "#" + currentUser.getUsername() +
                                " - " + received.split(":")[1];
                        myServer.getMainThread().sendToAllConnections(message);
                    } else if (received.equals("PLAY")) {
                        if (myServer.getGameThread().isAlive()) {
                            myServer.getLogger().logGameEvent(currentUser.toString() +
                                    " has joined the world.");
                            isInGame = true;
                            sendPacket("game_start");
                        } else {
                            sendPacket("game_not_started");
                        }
                    } else if (received.equals("STOP_PLAY")) {
                        myServer.getLogger().logGameEvent(currentUser.toString() +
                                " has left the world.");
                        isInGame = false;
                        sendPacket("game_end");
                    } else if (received.contains("move")) {
                        String command = received.split(":")[1];
                        switch (command) {
                            case "FORWARD_START":
                                rwlock.writeLock().lock();
                                moving[0] = true;
                                rwlock.writeLock().unlock();
                                break;
                            case "FORWARD_STOP":
                                rwlock.writeLock().lock();
                                moving[0] = false;
                                rwlock.writeLock().unlock();
                                break;
                            case "LEFT_START":
                                rwlock.writeLock().lock();
                                moving[1] = true;
                                rwlock.writeLock().unlock();
                                break;
                            case "LEFT_STOP":
                                rwlock.writeLock().lock();
                                moving[1] = false;
                                rwlock.writeLock().unlock();
                                break;
                            case "BACK_START":
                                rwlock.writeLock().lock();
                                moving[2] = true;
                                rwlock.writeLock().unlock();
                                break;
                            case "BACK_STOP":
                                rwlock.writeLock().lock();
                                moving[2] = false;
                                rwlock.writeLock().unlock();
                                break;
                            case "RIGHT_START":
                                rwlock.writeLock().lock();
                                moving[3] = true;
                                rwlock.writeLock().unlock();
                                break;
                            case "RIGHT_STOP":
                                rwlock.writeLock().lock();
                                moving[3] = false;
                                rwlock.writeLock().unlock();
                                break;
                            default:
                                myServer.getLogger().logServerError("Received Unknown Movement Command: " + command);
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the user is blacklisted
     * @param user the user to check
     * @return a boolean representing lack of blacklisting
     */
    private boolean checkBlacklist(User user) {
        if (mainThread.getBlacklistUsers().contains(user)) {
            try {
                sendPacket("disconnected:blacklist");
            } catch (IOException e) {
                e.printStackTrace();
            }
            disconnected = true;
            myServer.getLogger().logNetworkEvent("Blacklisted user " +
                    user.getUsername() + "\nwas prevented from connecting.");
            return false;
        }
        return true;
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
     * A public method that calls sendPacket after checking the data
     * @param data the data to send
     */
    public void sendData(String data) {
        if (isInGame) {
            try {
                sendPacket(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the current user
     * @return the current user
     */
    public User getCurrentUser() { return currentUser; }

    /**
     * Returns the ReadWriteLock for the moving array
     * @return the moving array's ReadWriteLock
     */
    public ReadWriteLock getRwlock() { return rwlock; }

    /**
     * Returns the moving array
     * @return the moving array
     */
    public boolean[] getMoving() { return moving; }

    /**
     * Sets the value of disconnected
     * @param disconnected the value to set
     */
    public void setDisconnected(boolean disconnected) { this.disconnected = disconnected; }

}
