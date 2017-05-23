package main.java.com.alexhennieroed.desolation.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controls the server's UI
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerControlController {

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

}
