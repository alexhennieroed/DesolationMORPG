package main.java.com.alexhennieroed.desolation.ui.controller;

import main.java.com.alexhennieroed.desolation.Client;

/**
 * Generic controller class
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Controller {

    protected Client myClient;

    /**
     * Sets the client of the controller
     * @param client the client
     */
    public void setMyClient(Client client) { this.myClient = client; }

    protected void connect() {
        System.out.println("Connection in wrong controller.");
    }

    protected void disconnect() {
        System.out.println("Disconnection in the wrong controller.");
    }

    public void enableButtons() {
        System.out.println("Enabling in the wrong controller.");
    }

    public void timeoutSetup() {
        System.out.println("Timeout in the wrong controller.");
    }

}
