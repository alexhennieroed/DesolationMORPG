package main.java.com.alexhennieroed.desolationclient.networking;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import main.java.com.alexhennieroed.desolationclient.Client;
import main.java.com.alexhennieroed.desolationclient.Settings;
import main.java.com.alexhennieroed.desolationclient.game.model.Character;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        byte[] buf = new byte[512];
        try {
            if (!connect(buf)) {
                return;
            }
            socket.setSoTimeout(0);
            while (true) {
                String received = receivePacket(buf);
                if (received != null) {
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
                        currentCharacter = new Character(charDataList);
                    } else if (received.contains("failure")) {
                        Platform.runLater(() -> myClient.getCurrentController()
                            .solveCredentialProblem());
                    } else if (received.equals("logout_success")) {
                        myClient.setState(Client.ClientState.IN_INIT_SCREEN);
                        Platform.runLater(() -> myClient.setScreen("HomeScreen"));
                    } else if (received.equals("make_character_success")) {
                        Platform.runLater(() -> myClient.getCurrentController()
                                .updateCharacterLabel(currentCharacter.toString()));
                    } else if (received.contains("disconnected")) {
                        String message = received.split(":")[1];
                        if (message.equals("admin")) {
                            message = "An admin has disconnected you from the server.";
                        } else if (message.equals("blacklist")) {
                            message = "An admin has blocked you from the server";
                        } else if (message.equals("server_closed")) {
                            message = "The server has been closed.";
                        }
                        showMessageAndClose(message);
                    } else if (received.contains("server_message") &&
                            myClient.getState() == Client.ClientState.IN_GAME) {
                        String message = received.split(":")[1];
                        myClient.getGameThread().addNewMessage(message);
                    } else if (received.contains("game_start")) {
                        Platform.runLater(() -> myClient.setScreen("GameScreen"));
                        myClient.setState(Client.ClientState.LOADING);
                    } else if (received.equals("game_not_started")) {
                        Platform.runLater(() -> myClient.getCurrentController().enableButtons());
                    } else if (received.contains("game_update") &&
                            myClient.getState() == Client.ClientState.IN_GAME) {
                        String[] updateInfo = received.split(":");
                        myClient.getGameThread().setCurrentTime(updateInfo[1] + ":" + updateInfo[2]);
                        myClient.getGameThread().setCurrentVisual(updateInfo[3]);
                        myClient.getGameThread().updateCharacterPosition(updateInfo[4], updateInfo[5]);
                    } else if (received.equals("game_end") &&
                            myClient.getState() == Client.ClientState.IN_GAME) {
                        myClient.setState(Client.ClientState.IN_CHAR_SCREEN);
                        Platform.runLater(() -> myClient.setScreen("CharacterScreen"));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }

    /**
     * Shows the given message then returns to the home screen
     * @param message the message to show
     */
    public void showMessageAndClose(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Disconnected from Server");
            alert.setHeaderText(null);
            alert.setContentText(message +
                    "\nYou will be returned to the home screen.");
            alert.showAndWait();
        });
        myClient.setState(Client.ClientState.IN_STARTUP);
        Platform.runLater(() -> myClient.setScreen("HomeScreen"));
    }

    /**
     * Connects to the server
     * @throws IOException when IO issues occur
     */
    private boolean connect(byte[] buf) throws IOException {
        try {
            socket.setSoTimeout(Settings.TIMEOUT);
            sendPacket("connect");
            String received = receivePacket(buf);
            if (received.contains("Error")) {
                System.out.println(received);
                return false;
            } else if (received.equals("connected")) {
                myClient.setState(Client.ClientState.IN_INIT_SCREEN);
                return true;
            } else if (received.contains("blacklist")) {
                showMessageAndClose("You have been blocked by the server's blacklist.");
            }
        } catch (SocketTimeoutException e) {
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
     * @param data the data to send
     * @throws IOException when there is an IO issue
     */
    private void sendPacket(String data) throws IOException {
        byte[] buf = data.getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(buf, buf.length, address, port);
        socket.send(sendPacket);
    }

    /**
     * A public method that calls sendPacket after checking the data
     * @param data the data to send
     */
    public void sendData(String data) {
        //TODO
        //Check that the data is acceptable
        try {
            sendPacket(data);
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
