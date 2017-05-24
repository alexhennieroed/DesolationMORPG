package main.java.com.alexhennieroed.desolationserver.networking;

import main.java.com.alexhennieroed.desolationserver.game.model.Character;

/**
 * Represents a user of the game
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class User {

    private String username;
    private String password;
    private Character character;
    private boolean isActive;

    /**
     * Creates a new user object
     * @param username the user's username
     * @param password the user's password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isActive = false;
        this.character = new Character("Default");
    }

    /**
     * Returns the user's username
     * @return a string representing the username
     */
    public String getUsername() { return username; }

    /**
     * Returns the user's password
     * @return a string representing the password
     */
    public String getPassword() { return password; }

    public Character getCharacter() { return character; }

    public void setCharacter(Character character) { this.character = character; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        String activeString = isActive() ? "ONLINE" : "OFFLINE";
        return username + ": " + character.toString() + " - " + activeString;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User u = (User) o;
            if (this.getUsername().equals(u.getUsername())) {
                return this.getPassword().equals(u.getPassword());
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (17 * username.hashCode()) + (13 * password.hashCode());
    }

}
