package main.java.com.alexhennieroed.desolationclient.game;

import main.java.com.alexhennieroed.desolationclient.Client;
import main.java.com.alexhennieroed.desolationclient.game.model.Character;

/**
 * Controls the game in the client
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ClientGameThread extends Thread {

    private final Client myClient;

    private Character currentCharacter;

    public ClientGameThread(Client client) {
        this.myClient = client;
        currentCharacter = myClient.getServerConnector().getCurrentCharacter();
    }

    @Override
    public void run() {
        System.out.println("Game thread, yay!");
        while (myClient.getState() == Client.ClientState.IN_GAME) {
            //TODO
            //This is the primary game loop
        }
        System.out.println("Game is done.");
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

}
