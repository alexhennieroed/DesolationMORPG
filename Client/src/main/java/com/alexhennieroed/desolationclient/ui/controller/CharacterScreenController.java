package main.java.com.alexhennieroed.desolationclient.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.java.com.alexhennieroed.desolationclient.Client;

/**
 * Controls the CharacterScreen
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class CharacterScreenController extends Controller {

    @FXML
    private Label characterNameLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField characterNameField;

    @FXML
    private Button actionButton;

    @FXML
    private Button logoutButton;

    @FXML
    public void playGame() {
        actionButton.setDisable(true);
        logoutButton.setDisable(true);
        myClient.getServerConnector().sendData("PLAY");
    }

    @FXML
    public void logout() {
        actionButton.setDisable(true);
        logoutButton.setDisable(true);
        statusLabel.setText("Joining game...");
        myClient.getServerConnector().sendData("LOGOUT");
    }

    @Override
    public void updateCharacterLabel(String characterName) {
        if (characterName.contains("Default")) {
            actionButton.setOnAction((event) -> makeCharacter());
            actionButton.setText("Make Character");
            characterNameLabel.setDisable(true);
            characterNameLabel.setVisible(false);
            characterNameField.setDisable(false);
            characterNameField.setVisible(true);
        } else {
            characterNameLabel.setText(characterName);
            characterNameField.setDisable(true);
            characterNameField.setVisible(false);
            characterNameLabel.setDisable(false);
            characterNameLabel.setVisible(true);
            actionButton.setText("Play");
            actionButton.setOnAction((event) -> playGame());
        }
        actionButton.setDisable(false);
        logoutButton.setDisable(false);
    }

    /**
     * Tells the server to make a new character
     */
    private void makeCharacter() {
        logoutButton.setDisable(true);
        actionButton.setDisable(true);
        myClient.getServerConnector().sendData(
                "MAKE CHAR:" + characterNameField.getText());
    }

    @Override
    public void enableButtons() {
        actionButton.setDisable(false);
        logoutButton.setDisable(false);
        statusLabel.setText("Server game is not running right now.");
    }

}
