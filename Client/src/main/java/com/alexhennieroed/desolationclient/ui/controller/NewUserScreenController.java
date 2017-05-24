package main.java.com.alexhennieroed.desolationclient.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controls the NewUserScreen
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class NewUserScreenController extends Controller {

    @FXML
    private Label statusLabel;

    @FXML
    private TextField userNameField;

    @FXML
    private TextField passwordField;

    @FXML
    public void makeUser() {
        boolean failed = false;
        statusLabel.setText("");
        if (userNameField.getText().equals("")) {
            statusLabel.setText(statusLabel.getText() + "Username is required.");
            failed = true;
        } else if (passwordField.getText().equals("")) {
            statusLabel.setText(statusLabel.getText() + "Password is required.");
            failed = true;
        }
        if (failed) { return; }
        statusLabel.setText("Creating new user...");
        String makeUserString = "MAKE USER:" + userNameField.getText() +
                "&" + passwordField.getText();
        myClient.getServerConnector().sendData(makeUserString);
    }


}
