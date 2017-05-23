package main.java.com.alexhennieroed.desolation;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.com.alexhennieroed.desolation.networking.ClientConnector;
import main.java.com.alexhennieroed.desolation.networking.DatabaseConnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The main class of the server that interfaces between the UI and the logic
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Server extends Application {

    private Stage mainStage;
    private final DatabaseConnector dbconnector = new DatabaseConnector();
    private Map<InetAddress, ClientConnector> clientAddressList = new HashMap<>();
    private String numberClients;

    public ReadWriteLock lock = new ReentrantReadWriteLock();
    public static Map<InetAddress, DatagramPacket> clientBuffers = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        mainStage.setTitle("Desolation Server");
        try {
            runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the server
     */
    private void runServer() throws IOException {
        DatagramSocket socket = new DatagramSocket(Settings.SERVER_PORT);

        while (true) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            InetAddress address = packet.getAddress();
            if (clientAddressList.size() < Settings.MAX_THREADS &&
                    !clientAddressList.containsKey(address)) {
                // new thread for a client
                clientAddressList.put(address, new ClientConnector(socket, packet));
                clientBuffers.put(address, null);
                clientAddressList.get(address).start();
            } else if (clientAddressList.containsKey(address)) {
                //lock.writeLock().lock();
                clientBuffers.put(address, packet);
                //lock.writeLock().unlock();
            } else {
                buf = "Error: Too many connections.".getBytes();
                DatagramPacket errorpacket =
                        new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(errorpacket);
            }
            for (InetAddress addr : clientAddressList.keySet()) {
                if (!clientAddressList.get(addr).isAlive()) {
                    clientAddressList.remove(addr);
                }
            }
            numberClients = Integer.toString(clientAddressList.size());
        }
    }

    /**
     * Closes the application
     */
    public void close() {
        for (InetAddress address : clientAddressList.keySet()) {
            clientAddressList.remove(address);
        }
        System.exit(1);
    }

}
