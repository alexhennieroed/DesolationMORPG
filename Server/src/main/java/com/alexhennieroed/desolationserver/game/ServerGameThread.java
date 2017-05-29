package main.java.com.alexhennieroed.desolationserver.game;

import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.game.model.World;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the game in the server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerGameThread extends Thread {

    private final Server myServer;
    private final World myWorld;

    private String currentGameTime;
    private boolean running;
    private List<String> visuals;
    private int currentVisual;

    public ServerGameThread(Server server) {
        this.myServer = server;
        this.myWorld = new World();
        this.visuals = setupVisuals();
        this.currentVisual = 0;
    }

    @Override
    public void run() {
        running = true;
        myServer.getLogger().logGameEvent("Game world started.");
        int loopcount = 150000;
        while (running) {
            try {
                if (loopcount >= 150000) {
                    loopcount = 0;
                    currentVisual++;
                    if (currentVisual >= 3) {
                        currentVisual = 0;
                    }
                } else {
                    loopcount++;
                }
                handleInput();
                update();
            } catch (Exception e) {
                myServer.getLogger().logException(e);
                running = false;
            }
        }
        myServer.getLogger().logGameEvent("Game world stopped.");
    }

    private List<String> setupVisuals() {
        List<String> ans = new ArrayList<>();
        ans.add("forest.png");
        ans.add("city.png");
        ans.add("mountains.png");
        return ans;
    }

    private void handleInput() {
        //TODO
    }

    private void update() {
        //Do game stuff
        currentGameTime = localTimeToGameTime();
        myServer.getMainThread().sendToAllConnections("game_update:" + currentGameTime +
            ":" + visuals.get(currentVisual));
    }

    /**
     * Converts the local GMT time to the game's time
     * @return the time as a string
     */
    private String localTimeToGameTime() {
        LocalDateTime gmt = LocalDateTime.now(ZoneId.of("GMT+0"));
        String[] gmtBits = gmt.toString().split("T");
        StringBuilder builder = new StringBuilder();
        builder.append(gmtBits[0]);
        if (gmt.getHour() < 12) {
            builder.append("/Cycle-1 | ");
        } else {
            builder.append("/Cycle-2 | ");
        }
        int time = (((gmt.getHour() * 24) + gmt.getMinute()) * 60) + gmt.getSecond();
        time *= 2;
        builder.append((time / (24 * 60)) / 2).append(":");
        time = time - ((time / (24 * 60)) * (24 * 60));
        if (time < 600) builder.append("0");
        builder.append(time / 60);
        return builder.toString();
    }

    /**
     * Returns the current game time
     * @return the current game time
     */
    public String getCurrentGameTime() { return currentGameTime; }

    /**
     * Sets the value of running
     * @param running the value to set
     */
    public void setRunning(boolean running) { this.running = running; }

}
