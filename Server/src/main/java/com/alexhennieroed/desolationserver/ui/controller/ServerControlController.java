package main.java.com.alexhennieroed.desolationserver.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.networking.User;

import java.util.Optional;

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
        User duser = usersListView.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete User");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete user " +
            duser.getUsername() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            myServer.getDbconnector().removeUser(duser);
        }
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
        usersListView.itemsProperty().bind(myServer.getMainThread().getUserListUpdater());
        logListView.itemsProperty().bind(myServer.getLogger().getLogListProperty());
    }

}
