package main.java.com.alexhennieroed.desolation.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import main.java.com.alexhennieroed.desolation.Client;

/**
 * Controls the HomeScreen
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class HomeScreenController extends Controller {

    @FXML
    private Button newUserButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button connectionButton;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        connectionButton.setOnAction((event) -> connect());
    }

    @FXML
    public void toNewUser() {
        myClient.setScreen("NewUserScreen");
    }

    @FXML
    public void toLogin() {
        myClient.setScreen("LoginScreen");
    }

    @Override
    protected void disconnect() { myClient.close(); }

    @Override
    protected void connect() {
        connectionButton.setDisable(true);
        statusLabel.setText("Connecting...");
        myClient.setState(Client.ClientState.ATTEMPTING_CONNECTION);
        myClient.startServerConnector();
    }

    @Override
    public void enableButtons() {
        loginButton.setDisable(false);
        newUserButton.setDisable(false);
        connectionButton.setText("Disconnect");
        connectionButton.setDisable(false);
        connectionButton.setOnAction((event) -> disconnect());
        statusLabel.setText("Connected.");
    }

    @Override
    public void timeoutSetup() {
        statusLabel.setText("Connection timeout.");
        connectionButton.setDisable(false);
    }

}
