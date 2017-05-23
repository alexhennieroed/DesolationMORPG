package main.java.com.alexhennieroed.desolation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.alexhennieroed.desolation.networking.ClientConnector;
import main.java.com.alexhennieroed.desolation.networking.DatabaseConnector;
import main.java.com.alexhennieroed.desolation.networking.ServerThread;
import main.java.com.alexhennieroed.desolation.ui.controller.ServerControlController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * The main class of the server that interfaces between the UI and the logic
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Server extends Application {

    private Stage mainStage;
    private final ServerThread mainThread = new ServerThread();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        mainStage.setOnCloseRequest((event) -> {
            event.consume();
            Platform.exit();
            System.exit(1);
        });
        mainStage.setTitle("Desolation Server");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("./ui/view/ServerControl.fxml"));
        mainStage.setScene(new Scene(loader.load()));
        ServerControlController controller = loader.getController();
        controller.setMyServer(this);
        controller.bindValues();
        mainStage.show();
        mainThread.start();
    }

    /**
     * Returns the main thread of the server
     * @return the main thread
     */
    public ServerThread getMainThread() { return mainThread; }

    /**
     * Closes the application
     */
    public void close() {
        mainThread.close();
        System.exit(1);
    }

}
