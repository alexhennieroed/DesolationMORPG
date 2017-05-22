package main.java.com.alexhennieroed.desolation.networking;

/**
 * Represents a user of the game
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class User {

    private String username;
    private String password;

    /**
     * Creates a new user object
     * @param username the user's username
     * @param password the user's password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
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

}
