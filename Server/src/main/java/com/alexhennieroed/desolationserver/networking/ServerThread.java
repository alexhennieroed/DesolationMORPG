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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

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
    private Map<InetAddress, DatagramPacket> clientBuffers = new HashMap<>();
    private Map<InetAddress, User> clientCurrentUsers = new HashMap<>();
    private ListProperty userListUpdater = new SimpleListProperty();
    private List<User> userList = new ArrayList<>();
    private List<InetAddress> blacklistAddresses = new ArrayList<>();
    private List<User> blacklistUsers = new ArrayList<>();

    public ServerThread(Server server) {
        this.myServer = server;
        this.userList = myServer.getDbconnector().getAllUsers();
    }

    @Override
    public void run() {
        myServer.getLogger().logServerEvent("Server has been started.");
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(10 * 60 * 1000);
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
            while (true) {
                byte[] buf = new byte[256];
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

    public void disconnectUser(User user, String reason) {
        InetAddress address = getUserAddress(user);
        if (address != null) {
            if (reason.equals("blacklist")) {
                blacklistAddresses.add(address);
                blacklistUsers.add(user);
            }
            clientAddressList.get(address).sendData("disconnected:" + reason);
            clientAddressList.get(address).setDisconnected(true);
        }
        myServer.getLogger().logNetworkEvent(user.getUsername() +
            " was disconnected.");
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
     * Returns the userListUpdater list property
     * @return the ListProperty for userListUpdater
     */
    public ListProperty getUserListUpdater() { return userListUpdater; }

    /**
     * Returns the clientBuffers map
     * @return the clientBuffers map
     */
    Map<InetAddress, DatagramPacket> getClientBuffers() {
        return clientBuffers;
    }

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

    public List<InetAddress> getBlacklistAddresses() {
        return blacklistAddresses;
    }

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
                for (InetAddress addr : clientAddressList.keySet()) {
                    if (!clientAddressList.get(addr).isAlive()) {
                        clientAddressList.remove(addr);
                        clientCurrentUsers.remove(addr);
                        clientBuffers.remove(addr);
                    }
                }
                Platform.runLater(
                        () -> {
                            numberClients.setValue(Integer.toString(clientAddressList.size()));
                            numberUsers.setValue(Integer.toString(myServer.getDbconnector().getNumusers()));
                            myServer.getLogger().getLogListProperty().setValue(
                                    FXCollections.observableArrayList(myServer.getLogger().getLogList()));
                        }
                );
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
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
