package main.java.com.alexhennieroed.desolationserver.util;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs server activity
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerLogger {

    private ListProperty logListProperty = new SimpleListProperty();
    private List<String> logList = new ArrayList<>();

    public void logDatabaseEvent(String eventDescrip) {
        logList.add("[DATABASE] " + eventDescrip);
    }

    public void logNetworkEvent(String eventDescrip) {
        logList.add("[NETWORK] " + eventDescrip);
    }

    public void logGameEvent(String eventDescrip) {
        logList.add("[GAME] " + eventDescrip);
    }

    public void logException(Exception e) {
        logList.add("[EXCEPTION] " + e.getMessage());
        for (StackTraceElement elem : e.getStackTrace()) {
            logList.add(elem.toString());
        }
    }

    public void logServerError(String errorDescrip) {
        logList.add("[ERROR] " + errorDescrip);
    }

    public void logServerEvent(String eventDescrip) {
        logList.add("[SERVER] " + eventDescrip);
    }

    public ListProperty getLogListProperty() { return logListProperty; }

    public List<String> getLogList() { return logList; }

    public void saveLog() {
        System.out.println("Saved log.");
    }

}
