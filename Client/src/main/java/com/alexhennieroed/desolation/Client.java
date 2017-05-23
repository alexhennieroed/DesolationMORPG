package main.java.com.alexhennieroed.desolation;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.com.alexhennieroed.desolation.networking.ServerConnector;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * The main class of the client
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Client extends Application {

    private Stage mainStage;
    private ServerConnector serverConnector;
    private DatagramSocket socket;
    private final String HOSTNAME = "Alex-Inspiron-15";
    private final int PORT = 4545;
    private boolean running = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        try {
            socket = new DatagramSocket();
            serverConnector = new ServerConnector(socket, HOSTNAME, PORT);
            serverConnector.connect();
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.close();
        System.exit(1);
    }

}
