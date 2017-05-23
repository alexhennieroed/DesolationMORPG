package main.java.com.alexhennieroed.desolation.networking;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.java.com.alexhennieroed.desolation.Settings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The thread for the server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerThread extends Thread {

    private final DatabaseConnector dbconnector = new DatabaseConnector();

    private Map<InetAddress, ClientConnector> clientAddressList = new HashMap<>();
    private StringProperty numberClients = new SimpleStringProperty();
    private StringProperty numberUsers = new SimpleStringProperty();
    private Map<InetAddress, DatagramPacket> clientBuffers = new HashMap<>();

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(Settings.SERVER_PORT);
            ServerConsoleUpdateThread update = new ServerConsoleUpdateThread();
            update.start();
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                if (clientAddressList.size() < Settings.MAX_THREADS &&
                        !clientAddressList.containsKey(address)) {
                    // new thread for a client
                    clientAddressList.put(address, new ClientConnector(socket, packet, this));
                    clientBuffers.put(address, packet);
                    clientAddressList.get(address).start();
                } else if (clientAddressList.containsKey(address)) {
                    clientBuffers.put(address, packet);
                } else {
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
     * Returns the clientBuffers map
     * @return the clientBuffers map
     */
    Map<InetAddress, DatagramPacket> getClientBuffers() {
        return clientBuffers;
    }

    /**
     * Returns the DatabaseConnector
     * @return the DatabaseConnector
     */
    DatabaseConnector getDbconnector() { return dbconnector; }

    /**
     * Empties the maps before closing the application
     */
    public void close() {
        for (InetAddress address : clientAddressList.keySet()) {
            clientAddressList.remove(address);
        }
        for (InetAddress address : clientBuffers.keySet()) {
            clientBuffers.remove(address);
        }
    }

    /**
     * A thread specifically for updating the ui with important info
     */
    private class ServerConsoleUpdateThread extends Thread {
        @Override
        public void run() {
            while (true) {
                Platform.runLater(
                        () -> {
                            for (InetAddress addr : clientAddressList.keySet()) {
                                if (!clientAddressList.get(addr).isAlive()) {
                                    clientAddressList.remove(addr);
                                }
                            }

                            numberClients.setValue(Integer.toString(clientAddressList.size()));
                            numberUsers.setValue(Integer.toString(dbconnector.getNumusers()));
                        }
                );
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
