package main.java.com.alexhennieroed.desolation.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import main.java.com.alexhennieroed.desolation.Server;

/**
 * Controls the server's UI
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerControlController {

    private Server myServer = null;

    @FXML
    private ListView logListView;

    @FXML
    private Label serverStatusLabel;

    @FXML
    private Label currentPlayersLabel;

    @FXML
    private Label numberUsersLabel;

    @FXML
    private Label lastSaveLabel;

    @FXML
    public void initialize() {
        serverStatusLabel.setText("Running");
        currentPlayersLabel.textProperty().bind(Server.numberClients);
        numberUsersLabel.textProperty().bind(Server.numberUsers);
    }

    @FXML
    public void closeServer() {
        myServer.close();
    }

    /**
     * Sets the Server for this controller
     * @param server the server to set as
     */
    public void setMyServer(Server server) { this.myServer = server; }

}
