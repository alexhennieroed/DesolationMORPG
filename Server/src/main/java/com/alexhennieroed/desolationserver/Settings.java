package main.java.com.alexhennieroed.desolationserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Contains important settings for the game server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Settings {

    //Database info
    public static String MONGODB_HOST = "localhost";
    public static int MONGODB_PORT = 27017;
    public static String DATABASE_NAME = "desolation";
    public static int MAX_USER_COUNT = 100;
    public static int UPDATE_TIME = 10 * 60 * 1000;
    //Server info
    public static int MAX_THREADS = 5;
    public static int SERVER_PORT = 4545;
    public static File JAR_LOCATION = new File("");

    public Settings(File jarLocation) throws IOException {
        JAR_LOCATION = jarLocation;
        List<String> settingsList = new ArrayList<>();
        File settingsFile = new File(jarLocation.getAbsolutePath() + "/settings.cfg");
        if (settingsFile.createNewFile()) {
            Scanner scanner = new Scanner(settingsFile);
            while (scanner.hasNextLine()) {
                settingsList.add(scanner.nextLine());
            }
            if (settingsList.size() != 7) {
                MONGODB_HOST = settingsList.get(0).split("=")[1].trim();
                MONGODB_PORT = Integer.parseInt(settingsList.get(1).split("=")[1].trim());
                DATABASE_NAME = settingsList.get(2).split("=")[1].trim();
                MAX_USER_COUNT = Integer.parseInt(settingsList.get(3).split("=")[1].trim());
                UPDATE_TIME = Integer.parseInt(settingsList.get(4).split("=")[1].trim());
                MAX_THREADS = Integer.parseInt(settingsList.get(5).split("=")[1].trim());
                SERVER_PORT = Integer.parseInt(settingsList.get(6).split("=")[1].trim());
            } else {
                System.out.println("Not enough settings.");
            }
        } else {
            throw new IOException("Could not create settings file.");
        }
    }

}
