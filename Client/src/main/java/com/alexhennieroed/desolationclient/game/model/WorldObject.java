package main.java.com.alexhennieroed.desolationclient.game.model;

/**
 * Represents an object inside the world
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class WorldObject {

    protected int worldX;
    protected int worldY;

    /**
     * Creates a new WorldObject at the given position
     * @param x the world x of the object
     * @param y the world y of the object
     */
    public WorldObject(int x, int y) {
        this.worldX = x;
        this.worldY = y;
    }

    /**
     * Returns the world x
     * @return the world x
     */
    public int getWorldX() {
        return worldX;
    }

    /**
     * Returns the world y
     * @return the world y
     */
    public int getWorldY() {
        return worldY;
    }
}
