package main.java.com.alexhennieroed.desolationserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.alexhennieroed.desolationserver.game.ServerGameThread;
import main.java.com.alexhennieroed.desolationserver.networking.DatabaseConnector;
import main.java.com.alexhennieroed.desolationserver.networking.ServerThread;
import main.java.com.alexhennieroed.desolationserver.ui.controller.ServerControlController;
import main.java.com.alexhennieroed.desolationserver.util.ServerLogger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The main class of the server that interfaces between the UI and the logic
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Server extends Application {

    private Stage mainStage;
    private ServerLogger logger = new ServerLogger();

    private final DatabaseConnector dbconnector = new DatabaseConnector(this);
    private ServerThread mainThread;
    private final ServerGameThread gameThread = new ServerGameThread(this);
    private File jarLocation;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        try {
            jarLocation = new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            mainThread = new ServerThread(this);
        } catch (URISyntaxException e) {
            getLogger().logException(e);
        }
        mainStage.setOnCloseRequest((event) -> {
            event.consume();
            close();
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
     * Returns the game thread of the server
     * @return the game thread
     */
    public ServerGameThread getGameThread() { return gameThread; }

    /**
     * Returns the DatabaseConnector
     * @return the DatabaseConnector
     */
    public DatabaseConnector getDbconnector() { return dbconnector; }

    /**
     * Returns the ServerLogger
     * @return the ServerLogger
     */
    public ServerLogger getLogger() { return logger; }

    /**
     * Returns the jarLocation File
     * @return the jarLocation File
     */
    public File getJarLocation() { return jarLocation; }

    /**
     * Closes the application
     */
    public void close() {
        mainThread.close();
        dbconnector.updateAllUsers(mainThread.getUserList());
        logger.saveLog();
        Platform.exit();
        System.exit(1);
    }

}
