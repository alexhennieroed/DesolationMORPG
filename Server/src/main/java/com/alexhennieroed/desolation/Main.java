package main.java.com.alexhennieroed.desolation;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.com.alexhennieroed.desolation.networking.ClientConnector;
import main.java.com.alexhennieroed.desolation.networking.DatabaseConnector;

/**
 * The main class of the program that interfaces between the UI and the logic
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Main extends Application {

    private Stage mainStage;
    private final DatabaseConnector dbconnector = new DatabaseConnector();
    private ClientConnector clientConnector = new ClientConnector();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
    }

}
