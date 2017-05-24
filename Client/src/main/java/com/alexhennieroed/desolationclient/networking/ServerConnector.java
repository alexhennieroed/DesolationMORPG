package main.java.com.alexhennieroed.desolationclient.networking;

import javafx.application.Platform;
import main.java.com.alexhennieroed.desolationclient.Client;
import main.java.com.alexhennieroed.desolationclient.Settings;
import main.java.com.alexhennieroed.desolationclient.game.model.Character;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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

    private Character currentCharacter;

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
                        myClient.setState(Client.ClientState.IN_CHAR_SCREEN);
                        Platform.runLater(() -> myClient.setScreen("CharacterScreen"));
                    } else if (received.contains("char_data")) {
                        String[] charData = received.split(":");
                        List<String> charDataList = new ArrayList<>();
                        for (int i = 1; i < charData.length; i++) {
                            charDataList.add(charData[i]);
                        }
                        Character theChar = new Character(charDataList);
                        currentCharacter = theChar;
                    } else if (received.contains("failure")) {
                        System.out.println(received);
                    } else if (received.equals("logout_success")) {
                        myClient.setState(Client.ClientState.IN_INIT_SCREEN);
                        Platform.runLater(() -> myClient.setScreen("HomeScreen"));
                    } else if (received.equals("make_character_success")) {
                        Platform.runLater(() -> myClient.getCurrentController()
                                .updateCharacterLabel(currentCharacter.toString()));
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

    /**
     * A public method that calls sendPacket after checking the data
     * @param data the data to send
     */
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

    /**
     * Returns the current character
     * @return the current character
     */
    public Character getCurrentCharacter() { return currentCharacter; }

}