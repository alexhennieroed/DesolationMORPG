package main.java.com.alexhennieroed.desolationserver.game;

import main.java.com.alexhennieroed.desolationserver.Server;

/**
 * Controls the game in the server
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class ServerGameThread extends Thread {

    private final Server myServer;

    public ServerGameThread(Server server) {
        this.myServer = server;
    }

    @Override
    public void run() {
        myServer.getLogger().logGameEvent("Game thread started.");
        while (true) {
            //TODO
        }
    }
}
