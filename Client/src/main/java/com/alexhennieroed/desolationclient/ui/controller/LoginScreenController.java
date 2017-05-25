package main.java.com.alexhennieroed.desolationclient.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controls the LoginScreen of the client
 */
public class LoginScreenController extends Controller {

    @FXML
    private Label loginStatusLabel;

    @FXML
    private TextField userNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    public void login() {
        boolean failed = false;
        loginStatusLabel.setText("");
        if (userNameField.getText().equals("")) {
            loginStatusLabel.setText(loginStatusLabel.getText() + "Username is required.");
            failed = true;
        } else if (passwordField.getText().equals("")) {
            loginStatusLabel.setText(loginStatusLabel.getText() + "Password is required.");
            failed = true;
        }
        if (failed) { return; }
        loginButton.setDisable(true);
        loginStatusLabel.setText("Logging in...");
        String makeUserString = "LOGIN:" + userNameField.getText() +
                "&" + passwordField.getText();
        myClient.getServerConnector().sendData(makeUserString);
    }

    @Override
    public void solveCredentialProblem() {
        loginStatusLabel.setText("Incorrect username or password.");
        loginButton.setDisable(false);
    }

}
