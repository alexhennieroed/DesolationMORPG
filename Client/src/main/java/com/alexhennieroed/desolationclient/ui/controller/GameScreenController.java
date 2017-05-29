package main.java.com.alexhennieroed.desolationclient.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;

/**
 * Controls the GameScreen
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class GameScreenController extends Controller {

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Pane messagePane;

    @FXML
    private ListView<String> messageListView;

    @FXML
    private TextField messageField;

    @FXML
    private ImageView displayImageView;

    @FXML
    public void sendMessage() {
        myClient.getServerConnector().sendData("client_message:" + messageField.getText());
        messageField.setText("");
    }

    @FXML
    public void leaveGame() {
        myClient.getServerConnector().sendData("STOP_PLAY");
    }

    @Override
    public void bindValues() {
        currentTimeLabel.textProperty().bind(myClient.getGameThread().timeUpdaterProperty());
        messageListView.itemsProperty().bind(myClient.getGameThread().messagesProperty());
    }

    @Override
    public void showHideSection() {
        if (messagePane.isVisible()) {
            messagePane.setVisible(false);
            messageListView.setVisible(false);
            messageField.setVisible(false);
            messageField.setDisable(true);
        } else {
            messageListView.setVisible(true);
            messageField.setDisable(false);
            messageField.setVisible(true);
            messagePane.setVisible(true);
        }
    }

    @Override
    public void updateVisuals(String visual) {
        try {
            File imageFile =
                    new File("src/main/res/visuals/" + visual);
            Image image = new Image(imageFile.toURI().toURL().toExternalForm());
            displayImageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
