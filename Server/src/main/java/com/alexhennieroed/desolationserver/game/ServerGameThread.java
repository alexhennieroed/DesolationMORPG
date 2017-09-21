package main.java.com.alexhennieroed.desolationserver.game;

import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.game.model.*;
import main.java.com.alexhennieroed.desolationserver.game.model.Character;
import main.java.com.alexhennieroed.desolationserver.networking.ClientConnector;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Creates a new ServerGameThread
     * @param server the server that owns this thread
     */
    public ServerGameThread(Server server) {
        this.myServer = server;
        //Test list
        List<WorldObject> objlist = Arrays.asList(
                new Construction(4, 5, Construction.ConstructionType.WALL),
                new Construction(4, 6, Construction.ConstructionType.WALL),
                new Construction(4, 7, Construction.ConstructionType.WALL),
                new Construction(9, 0, Construction.ConstructionType.WALL),
                new NPC("John Doe", 3, 3), new NPC("Jane Doe", 8, 7));
        this.myWorld = new World(objlist);
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
                update();
            } catch (Exception e) {
                myServer.getLogger().logException(e);
                running = false;
            }
        }
        myServer.getLogger().logGameEvent("Game world stopped.");
    }

    /**
     * Initializes visuals for the game
     * @return a list of file names of the visuals
     */
    private List<String> setupVisuals() {
        List<String> ans = new ArrayList<>();
        ans.add("forest.png");
        ans.add("city.png");
        ans.add("mountains.png");
        return ans;
    }

    /**
     * Updates the game
     */
    private void update() {
        //Do game stuff
        currentGameTime = localTimeToGameTime();
        
        //Update the connected clients
        for(Object cc : myServer.getMainThread().getClientConnectors()) {
            ClientConnector clicon = (ClientConnector) cc;
            Character cliconchar = clicon.getCurrentUser().getCharacter();
            clicon.getRwlock().readLock().lock();
            boolean[] mArr = clicon.getMoving();
            if (mArr[0] && !mArr[2]) {
                cliconchar.move(Direction.NORTH);
            } else if (mArr[2] && !mArr[0]) {
                cliconchar.move(Direction.SOUTH);
            }
            if (mArr[1] && !mArr[3]) {
                cliconchar.move(Direction.WEST);
            } else if (mArr[3] && !mArr[1]) {
                cliconchar.move(Direction.EAST);
            }
            clicon.getRwlock().readLock().unlock();
            StringBuilder builder = new StringBuilder();
            builder.append("game_update:" + currentGameTime +
                    ":" + visuals.get(currentVisual) + ":"
                    + cliconchar.getWorldX() + ":" + cliconchar.getWorldY() + ":");
            myWorld.getNearbyObjects(cliconchar).forEach(object -> builder.append(object.toString() + ":"));
            builder.append("END");
            clicon.sendData(builder.toString());
        }
    }

    /**
     * Converts the local GMT time to the game's time
     * @return the time as a string
     */
    private String localTimeToGameTime() {
        LocalDateTime gmt = LocalDateTime.now(ZoneId.systemDefault());
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
