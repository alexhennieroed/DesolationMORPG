package main.java.com.alexhennieroed.desolationserver.util;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import main.java.com.alexhennieroed.desolationserver.Settings;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Logs server activity
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerLogger {

    private ListProperty logListProperty = new SimpleListProperty();
    private List<String> logList = new ArrayList<>();

    public void logDatabaseEvent(String eventDescrip) {
        logList.add(LocalDateTime.now().toString().split("\\.")[0].replace('T', '@') +
                " | [DATABASE] " + eventDescrip);
    }

    public void logNetworkEvent(String eventDescrip) {
        logList.add(LocalDateTime.now().toString().split("\\.")[0].replace('T', '@') +
                " | [NETWORK] " + eventDescrip);
    }

    public void logGameEvent(String eventDescrip) {
        logList.add(LocalDateTime.now().toString().split("\\.")[0].replace('T', '@') +
                " | [GAME] " + eventDescrip);
    }

    public void logException(Exception e) {
        logList.add(LocalDateTime.now().toString().split("\\.")[0].replace('T', '@') +
                "| [EXCEPTION] " + e.getMessage());
        for (StackTraceElement elem : e.getStackTrace()) {
            logList.add(elem.toString());
        }
    }

    public void logServerError(String errorDescrip) {
        logList.add(LocalDateTime.now().toString().split("\\.")[0].replace('T', '@') +
                " | [ERROR] " + errorDescrip);
    }

    public void logServerEvent(String eventDescrip) {
        logList.add(LocalDateTime.now().toString().split("\\.")[0].replace('T', '@') +
                " | [SERVER] " + eventDescrip);
    }

    public ListProperty getLogListProperty() { return logListProperty; }

    public List<String> getLogList() { return logList; }

    public void saveLog() {
        try {
            File file = new File(Settings.JAR_LOCATION.getAbsolutePath() +
                    "/logs/" + LocalDateTime.now().toString().split("\\.")[0].replace(":", "")
                    .replace("T", "") + ".log");
            if (!file.createNewFile()) {
                throw new IOException("Could not create log file.");
            }
            PrintStream stream = new PrintStream(file);
            logList.forEach(message -> stream.println(message));
            stream.close();
        } catch (IOException e) {
            logException(e);
        }
    }

}
