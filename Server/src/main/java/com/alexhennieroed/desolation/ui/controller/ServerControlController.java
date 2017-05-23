package main.java.com.alexhennieroed.desolation.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import main.java.com.alexhennieroed.desolation.Server;
import main.java.com.alexhennieroed.desolation.networking.User;

/**
 * Controls the server's UI
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerControlController {

    private Server myServer = null;

    @FXML
    private ListView<String> logListView;

    @FXML
    private ListView<User> usersListView;

    @FXML
    private Label serverStatusLabel;

    @FXML
    private Label currentPlayersLabel;

    @FXML
    private Label numberUsersLabel;

    @FXML
    private Label lastSaveLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button disconnectButton;

    @FXML
    public void initialize() {
        serverStatusLabel.setText("Running");
    }

    @FXML
    public void closeServer() {
        serverStatusLabel.setText("Closing...");
        myServer.close();
    }

    @FXML
    public void forceSave() { System.out.println("Saving users and characters."); }

    @FXML
    public void deleteUser() {
        System.out.println("Deleting user...");
        User duser = usersListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void disconnectUser() { System.out.println("Disconnecting user..."); }

    /**
     * Sets the Server for this controller
     * @param server the server to set as
     */
    public void setMyServer(Server server) { this.myServer = server; }

    /**
     * Binds values to the labels
     */
    public void bindValues() {
        currentPlayersLabel.textProperty().bind(myServer.getMainThread().numberClientsProperty());
        numberUsersLabel.textProperty().bind(myServer.getMainThread().numberUsersProperty());
    }

}
