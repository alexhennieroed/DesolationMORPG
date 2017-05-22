package main.java.com.alexhennieroed.desolation;

/**
 * Contains important settings for the game server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Settings {

    //Database info
    public static final String MONGODB_HOST = "localhost";
    public static final int MONGODB_PORT = 27017;
    public static final String DATABASE_NAME = "desolation";
    public static final String DATABASE_USERNAME = "username";
    public static final char[] DATABASE_PASSWORD = "password".toCharArray();
    public static final int MAX_USER_COUNT = 10;

}
