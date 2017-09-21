package main.java.com.alexhennieroed.desolationserver.game.model;

/**
 * Represents a non-player character in the game
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class NPC extends Actor {

    private String name;

    /**
     * Creates a new NPC at the given location
     * @param name the name of the NPC
     * @param x world x for the NPC
     * @param y world y for the NPC
     */
    public NPC(String name, int x, int y) {
        super(x, y);
        this.name = name;
    }

    /**
     * Returns the name
     * @return the name
     */
    public String getName() { return name; }

    @Override
    public String toString() {
        return "[NPC]" + getName() + " (" + getWorldX() + "," + getWorldY() + ")";
    }

}
