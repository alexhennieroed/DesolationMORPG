package main.java.com.alexhennieroed.desolationclient.game;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import main.java.com.alexhennieroed.desolationclient.Client;
import main.java.com.alexhennieroed.desolationclient.game.model.Character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls the game in the client
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ClientGameThread extends Thread {

    private final Client myClient;
    private final int MAX_DELAY_MS = 17;

    private Character currentCharacter;
    private boolean running;
    private List<String> messageBuffer = new ArrayList<>();
    private Map<String, Integer> messageTimers = new HashMap<>();
    private String currentTime = "";
    private StringProperty timeUpdater = new SimpleStringProperty();
    private ListProperty<String> messages = new SimpleListProperty<>();

    public ClientGameThread(Client client) {
        this.myClient = client;
        currentCharacter = myClient.getServerConnector().getCurrentCharacter();
    }

    @Override
    public void run() {
        System.out.println("Game thread, yay!");
        running = true;
        while (running) {
            try {
                long delay = update();
                sleep(Math.max(0, MAX_DELAY_MS - delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Game is done.");
    }

    private long update() {
        long time = System.currentTimeMillis();

        //Update game data and visuals
        for (String message : messageTimers.keySet()) {
            if (messageTimers.get(message) > 0) {
                messageTimers.put(message, messageTimers.get(message) - 1);
            } else {
                messageTimers.remove(message);
                messageBuffer.remove(message);
            }
        }


        Platform.runLater(() -> {
            timeUpdater.setValue(currentTime);
            messages.setValue(FXCollections.observableArrayList(messageBuffer));
        });

        return System.currentTimeMillis() - time;
    }

    /**
     * Returns the current character
     * @return the current character
     */
    public Character getCurrentCharacter() { return currentCharacter; }

    /**
     * Sets the current character
     * @param character the new current character
     */
    public void setCurrentCharacter(Character character) { this.currentCharacter = character; }

    public StringProperty timeUpdaterProperty() { return timeUpdater; }

    public ListProperty<String> messagesProperty() { return messages; }

    public void addNewMessage(String message) {
        messageBuffer.add(message);
        messageTimers.put(message, (60 * 5));
    }

    public void setCurrentTime(String time) { this.currentTime = time; }

    public void setRunning(boolean running) { this.running = running; }

}
