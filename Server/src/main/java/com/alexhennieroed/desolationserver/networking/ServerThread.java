package main.java.com.alexhennieroed.desolationserver.networking;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.Settings;
import main.java.com.alexhennieroed.desolationserver.game.ServerGameThread;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The thread for the server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerThread extends Thread {

    private final Server myServer;

    private Map<InetAddress, ClientConnector> clientAddressList = new HashMap<>();
    private StringProperty numberClients = new SimpleStringProperty();
    private StringProperty numberUsers = new SimpleStringProperty();
    private StringProperty lastSave = new SimpleStringProperty();
    private StringProperty gameTime = new SimpleStringProperty();
    private StringProperty status = new SimpleStringProperty();
    private Map<InetAddress, DatagramPacket> clientBuffers = new HashMap<>();
    private Map<InetAddress, User> clientCurrentUsers = new HashMap<>();
    private ListProperty userListUpdater = new SimpleListProperty();
    private List<User> userList = new ArrayList<>();
    private List<InetAddress> blacklistAddresses = new ArrayList<>();
    private List<User> blacklistUsers = new ArrayList<>();
    private File blacklistAddressFile;
    private File blacklistUserFile;
    private ListProperty<String> chatUpdater = new SimpleListProperty<>();
    private List<String> messageLog = new ArrayList<>();
    private List<String> logDisplayList = new ArrayList<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public ServerThread(Server server) {
        this.myServer = server;
        this.userList = myServer.getDbconnector().getAllUsers();
        this.blacklistAddressFile = new File(myServer.getJarLocation().getAbsolutePath() +
            "/blacklistAddresses.txt");
        this.blacklistUserFile = new File(myServer.getJarLocation().getAbsolutePath() +
            "/blacklistUsers.txt");
        try {
            Scanner addressScanner = new Scanner(blacklistAddressFile);
            while (addressScanner.hasNextLine()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        myServer.getLogger().logServerEvent("Server has been started.");
        //Thread to run an automatic update of all users in the database
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(Settings.UPDATE_TIME);
                        myServer.getDbconnector().updateAllUsers(userList);
                    } catch (InterruptedException e) {
                        myServer.getLogger().logException(e);
                    }
                }
            }
        }.start();
        try {
            DatagramSocket socket = new DatagramSocket(Settings.SERVER_PORT);
            ServerConsoleUpdateThread update = new ServerConsoleUpdateThread();
            update.start();
            //Get a packet and send it to the proper destination
            while (true) {
                byte[] buf = new byte[512];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                if (blacklistAddresses.contains(address)) {
                    buf = "disconnected:blacklist".getBytes();
                    DatagramPacket sendPacket =
                            new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);
                    myServer.getLogger().logNetworkEvent("Blacklisted address " +
                        address.toString() + "\nwas prevented from connecting.");
                } else if (clientAddressList.size() < Settings.MAX_THREADS &&
                        !clientAddressList.containsKey(address)) {
                    //New thread for a client
                    clientAddressList.put(address,
                            new ClientConnector(socket, packet, this, myServer));
                    clientBuffers.put(address, packet);
                    clientAddressList.get(address).start();
                    clientCurrentUsers.put(address,
                            clientAddressList.get(address).getCurrentUser());
                } else if (clientAddressList.containsKey(address)) {
                    //Direct to the proper buffer
                    clientBuffers.put(address, packet);
                } else {
                    myServer.getLogger().logServerError("Could not connect " +
                        address.toString() + " because too many connections exist.");
                    buf = "Error: Too many connections.".getBytes();
                    DatagramPacket errorpacket =
                            new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                    socket.send(errorpacket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a chat message to all connected clients
     * @param message the message to send
     */
    public void sendToAllConnections(String message) {
        if (!message.contains("game_update")) {
            lock.writeLock().lock();
            messageLog.add(message);
            lock.writeLock().unlock();
            message = "server_message:" + processString(message, 40);
        }
        for (InetAddress address : clientAddressList.keySet()) {
            clientAddressList.get(address).sendData(message);
        }
    }

    /**
     * Turns the string into a chat-friendly size
     * @param string the string to process
     * @return the chat-friendly string
     */
    private String processString(String string, int lineLength) {
        String[] words = string.split(" ");
        List<String> lineList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (builder.length() > lineLength) {
                builder.append("\n");
                lineList.add(builder.toString());
                builder.delete(0, builder.length());
            }
            builder.append(word).append(" ");
        }
        lineList.add(builder.toString().trim());
        builder.delete(0, builder.length());
        lineList.forEach(word -> builder.append(word));
        return builder.toString();
    }

    /**
     * Disconnects the selected user and send them a message with the reason
     * @param user the user to disconnect
     * @param reason the reason for disconnection
     */
    public void disconnectUser(User user, String reason) {
        InetAddress address = getUserAddress(user);
        String type = "disconnected";
        if (address != null) {
            if (reason.equals("blacklist")) {
                blacklistAddresses.add(address);
                blacklistUsers.add(user);
                try {
                    PrintStream addressStream = new PrintStream(blacklistAddressFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                type = "disconnected and blacklisted";
            }
            clientAddressList.get(address).sendData("disconnected:" + reason);
            clientAddressList.get(address).setDisconnected(true);
        }
        myServer.getLogger().logNetworkEvent(user.getUsername() +
            " was " + type + ".");
    }

    /**
     * Returns the numberClients string property
     * @return the StringProperty for numberClients
     */
    public StringProperty numberClientsProperty() {
        return numberClients;
    }

    /**
     * Returns the numberUsers string property
     * @return the StringProperty for numberUsers
     */
    public StringProperty numberUsersProperty() {
        return numberUsers;
    }

    /**
     * Returns the lastSave string property
     * @return the StringProperty for lastSave
     */
    public StringProperty lastSaveProperty() { return lastSave; }

    /**
     * Returns the gameTime string property
     * @return the StringProperty for gameTime
     */
    public StringProperty gameTimeProperty() { return gameTime; }

    /**
     * Returns the status string property
     * @return the StringProperty for status
     */
    public StringProperty statusProperty() { return status; }

    /**
     * Returns the userListUpdater list property
     * @return the ListProperty for userListUpdater
     */
    public ListProperty getUserListUpdater() { return userListUpdater; }

    /**
     * Returns the chatUpdater list property
     * @return the ListProperty for chatUpdater
     */
    public ListProperty getChatUpdater() { return chatUpdater; }

    /**
     * Returns the clientBuffers map
     * @return the clientBuffers map
     */
    Map<InetAddress, DatagramPacket> getClientBuffers() {
        return clientBuffers;
    }

    public List getClientConnectors() { return (List<ClientConnector>) clientAddressList.values(); }

    /**
     * Returns the userList
     * @return the userList
     */
    public List<User> getUserList() { return userList; }

    /**
     * Returns the ClientConnector for the specified user
     * @param user the user to find
     * @return the user's connector
     */
    public InetAddress getUserAddress(User user) {
        for (Map.Entry<InetAddress, User> e : clientCurrentUsers.entrySet()) {
            if (e.getValue().equals(user)) {
                return e.getKey();
            }
        }
        return null;
    }

    /**
     * Retuns the list of blacklisted addresses
     * @return the list of blacklisted addresses
     */
    public List<InetAddress> getBlacklistAddresses() {
        return blacklistAddresses;
    }

    /**
     * Returns the list of blacklisted users
     * @return the list of blacklisted users
     */
    public List<User> getBlacklistUsers() {
        return blacklistUsers;
    }

    /**
     * Empties the maps before closing the application
     */
    public void close() {
        myServer.getLogger().logServerEvent("Server close requested.");
        for (InetAddress addr : clientCurrentUsers.keySet()) {
            clientCurrentUsers.put(addr,
                    clientAddressList.get(addr).getCurrentUser());
            if (clientAddressList.get(addr).getCurrentUser() != null) {
                userList.get(userList.indexOf(clientCurrentUsers.get(addr)))
                        .setCharacter(clientCurrentUsers.get(addr).getCharacter());
            }
            clientAddressList.get(addr).sendData("disconnect:server_closed");
            clientAddressList.remove(addr);
            clientBuffers.remove(addr);
            clientCurrentUsers.remove(addr);
        }
    }

    /**
     * Creates a list from the toString() of each user in the list
     * @return the list of toString()s
     */
    private List<String> userListToStrings() {
        List<String> userStrings = new ArrayList<>();
        for (User u : userList) {
            userStrings.add(u.toString());
        }
        return userStrings;
    }

    /**
     * A thread specifically for updating the ui with important info
     */
    private class ServerConsoleUpdateThread extends Thread {
        @Override
        public void run() {
            int loopCount = 5;
            while (true) {
                //If a client has disconnected, remove them from the lists
                for (InetAddress addr : clientAddressList.keySet()) {
                    if (!clientAddressList.get(addr).isAlive()) {
                        clientAddressList.remove(addr);
                        clientCurrentUsers.remove(addr);
                        clientBuffers.remove(addr);
                    }
                }
                //Update important information every loop
                if (logDisplayList.size() < myServer.getLogger().getLogList().size()) {
                    logDisplayList.clear();
                    myServer.getLogger().getLogList().forEach(message -> logDisplayList.add(processString(message, 55)));
                }
                Platform.runLater(
                        () -> {
                            numberClients.setValue(Integer.toString(clientAddressList.size()));
                            numberUsers.setValue(Integer.toString(myServer.getDbconnector().getNumusers()));
                            myServer.getLogger().getLogListProperty().setValue(
                                    FXCollections.observableArrayList(logDisplayList));
                            lock.readLock().lock();
                            chatUpdater.setValue(FXCollections.observableArrayList(messageLog));
                            lock.readLock().unlock();
                            lastSave.setValue(myServer.getDbconnector().getLastSave());
                            if (myServer.getGameThread().isAlive()) {
                                status.setValue("Running");
                                gameTime.setValue(myServer.getGameThread().getCurrentGameTime());
                            } else {
                                status.setValue("Stopped");
                                gameTime.setValue("");
                            }
                        }
                );
                //Update user information every 5 loops
                if (loopCount >= 5) {
                    Platform.runLater(() -> {
                        userList = myServer.getDbconnector().getAllUsers();
                        for (InetAddress addr : clientCurrentUsers.keySet()) {
                            clientCurrentUsers.put(addr,
                                    clientAddressList.get(addr).getCurrentUser());
                            if (clientAddressList.get(addr).getCurrentUser() != null) {
                                userList.get(userList.indexOf(clientCurrentUsers.get(addr))).setActive(true);
                                userList.get(userList.indexOf(clientCurrentUsers.get(addr)))
                                        .setCharacter(clientCurrentUsers.get(addr).getCharacter());
                            }
                        }
                        userListUpdater.setValue(FXCollections.observableArrayList(userListToStrings()));
                    });
                    loopCount = 0;
                } else {
                    loopCount++;
                }
                try {
                    //Stop the thread for one second before continuing
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
