package main.java.com.alexhennieroed.desolation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.java.com.alexhennieroed.desolation.networking.ServerConnector;
import main.java.com.alexhennieroed.desolation.ui.controller.Controller;
import main.java.com.alexhennieroed.desolation.ui.controller.HomeScreenController;
import main.java.com.alexhennieroed.desolation.ui.controller.LoginScreenController;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * The main class of the client
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Client extends Application {

    private Stage mainStage;
    private DatagramSocket socket;
    private ServerConnector serverConnector;
    private ClientState state;
    private Controller currentController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        state = ClientState.IN_STARTUP;
        ClientStateCheckThread thread = new ClientStateCheckThread();
        thread.start();
        mainStage = stage;
        mainStage.setOnCloseRequest((event) -> {
            event.consume();
            close();
        });
        mainStage.setTitle("Desolation");
        setScreen("HomeScreen");
        mainStage.show();
    }

    /**
     * Sets the screen of the client
     * @param screenName the name of the screen to set as active
     */
    public void setScreen(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("./ui/view/" + screenName + ".fxml"));
            mainStage.setScene(new Scene(loader.load()));
            currentController = loader.getController();
            currentController.setMyClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the server connector
     * @return the server connector
     */
    public ServerConnector getServerConnector() {
        return serverConnector;
    }

    /**
     * Starts a new ServerConnector
     */
    public void startServerConnector() {
        try {
            socket = new DatagramSocket();
            serverConnector = new ServerConnector(socket,
                    Settings.HOSTNAME, Settings.PORT, this);
            serverConnector.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the client's state
     * @param state the state to set
     */
    public void setState(ClientState state) { this.state = state;}

    public ClientState getState() { return state; }

    /**
     * Closes the client
     */
    public void close() {
        if (state != ClientState.CONNECTION_TIMEOUT &&
                state != ClientState.ATTEMPTING_CONNECTION &&
                state != ClientState.IN_STARTUP) {
            serverConnector.sendData("disconnect");
        }
        Platform.exit();
        System.exit(1);
    }

    /**
     * Represents the different states of the client
     */
    public enum ClientState {

        IN_STARTUP(),
        IN_GAME(),
        IN_INIT_SCREEN(),
        ATTEMPTING_CONNECTION(),
        CONNECTION_TIMEOUT();

    }

    /**
     * Constantly checks the state of the client
     */
    private class ClientStateCheckThread extends Thread {
        @Override
        public void run() {
            ClientState lastState = state;
            while(true) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                state = Client.this.getState();
                if (lastState != state) {
                    System.out.println("State change!");
                    if (state == ClientState.CONNECTION_TIMEOUT) {
                        Platform.runLater(() -> currentController.timeoutSetup());
                    } else if (state == ClientState.IN_INIT_SCREEN) {
                        Platform.runLater(() -> currentController.enableButtons());
                    } else if (state == ClientState.IN_GAME) {
                        //TODO
                    } else if (state == ClientState.ATTEMPTING_CONNECTION) {
                        //TODO
                    } else if (state == ClientState.IN_STARTUP) {
                        //TODO
                    }
                    lastState = state;
                }
            }
        }
    }

}
