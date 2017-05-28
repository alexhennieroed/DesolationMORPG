package main.java.com.alexhennieroed.desolationserver.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.networking.ClientConnector;
import main.java.com.alexhennieroed.desolationserver.networking.User;

import java.util.Optional;

/**
 * Controls the server's UI
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerControlController {

    private Server myServer = null;
    private User duser = null;

    @FXML
    private ListView<String> logListView;

    @FXML
    private ListView<String> usersListView;

    @FXML
    private ListView<String> chatListView;

    @FXML
    private Label serverStatusLabel;

    @FXML
    private Label currentPlayersLabel;

    @FXML
    private Label numberUsersLabel;

    @FXML
    private Label lastSaveLabel;

    @FXML
    private Label gameTimeLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button disconnectButton;

    @FXML
    private Button gameControlButton;

    @FXML
    private TextField messageField;

    @FXML
    public void initialize() {
        usersListView.setOnMouseClicked(event -> selectionActions());
    }

    @FXML
    public void closeServer() {
        myServer.close();
    }

    @FXML
    public void forceSave() {
        myServer.getDbconnector().updateAllUsers(
                myServer.getMainThread().getUserList());
    }

    private void selectionActions() {
        String duserstring = usersListView.getSelectionModel().getSelectedItem();
        if (duserstring != null) {
            String dusername = duserstring.split(":")[0];
            duser = myServer.getDbconnector().getUser(dusername);
            if (duser != null) {
                duser = myServer.getMainThread().getUserList().get(
                        myServer.getMainThread().getUserList().indexOf(duser));
                if (!duser.isActive()) {
                    disconnectButton.setDisable(true);
                    deleteButton.setDisable(false);
                } else {
                    disconnectButton.setDisable(false);
                    deleteButton.setDisable(true);
                }
            }
        }
    }

    @FXML
    public void deleteUser() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete User");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete user " +
            duser.getUsername() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            myServer.getDbconnector().removeUser(duser);
        }
        deleteButton.setDisable(true);
    }

    @FXML
    public void disconnectUser() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Disconnect User");
        alert.setHeaderText(null);
        alert.setContentText("Would you like to just disconnect the user or\n" +
            "would you also like to blacklist them?");

        ButtonType buttonTypeOne = new ButtonType("Disconnect");
        ButtonType buttonTypeTwo = new ButtonType("Blacklist");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            myServer.getMainThread().disconnectUser(duser, "admin");
        } else if (result.get() == buttonTypeTwo) {
            myServer.getMainThread().disconnectUser(duser, "blacklist");
        }
        disconnectButton.setDisable(true);
    }

    @FXML
    public void sendMessage() {
        myServer.getMainThread().sendToAllConnections("[SERVER] " + messageField.getText());
        messageField.setText("");
    }

    @FXML
    public void startGame() {
        gameControlButton.setText("Stop");
        gameControlButton.setOnAction(event -> stopGame());
        myServer.getGameThread().start();
    }

    private void stopGame() {
        gameControlButton.setText("Start");
        gameControlButton.setOnAction(event -> startGame());
        myServer.getGameThread().setRunning(false);
    }

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
        lastSaveLabel.textProperty().bind(myServer.getMainThread().lastSaveProperty());
        gameTimeLabel.textProperty().bind(myServer.getMainThread().gameTimeProperty());
        serverStatusLabel.textProperty().bind(myServer.getMainThread().statusProperty());

        usersListView.itemsProperty().bind(myServer.getMainThread().getUserListUpdater());
        logListView.itemsProperty().bind(myServer.getLogger().getLogListProperty());
        chatListView.itemsProperty().bind(myServer.getMainThread().getChatUpdater());
    }

}
