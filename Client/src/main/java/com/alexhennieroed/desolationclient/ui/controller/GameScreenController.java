package main.java.com.alexhennieroed.desolationclient.ui.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        messagePane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) {
                    myClient.getServerConnector().sendData("move:FORWARD_START");
                } else if(event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                    myClient.getServerConnector().sendData("move:LEFT_START");
                } else if(event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) {
                    myClient.getServerConnector().sendData("move:BACK_START");
                } else if(event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                    myClient.getServerConnector().sendData("move:RIGHT_START");
                }
            }
        });
        messagePane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) {
                    myClient.getServerConnector().sendData("move:FORWARD_STOP");
                } else if(event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                    myClient.getServerConnector().sendData("move:LEFT_STOP");
                } else if(event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) {
                    myClient.getServerConnector().sendData("move:BACK_STOP");
                } else if(event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                    myClient.getServerConnector().sendData("move:RIGHT_STOP");
                }
            }
        });
        messagePane.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() ==KeyCode.T) {
                    showHideSection();
                }
            }
        });
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
