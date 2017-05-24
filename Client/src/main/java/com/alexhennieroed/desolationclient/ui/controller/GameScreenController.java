package main.java.com.alexhennieroed.desolationclient.ui.controller;

import javafx.fxml.FXML;

/**
 * Controls the GameScreen
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class GameScreenController extends Controller {

    @FXML
    public void logout() {
        myClient.getServerConnector().sendData("LOGOUT");
    }

}
