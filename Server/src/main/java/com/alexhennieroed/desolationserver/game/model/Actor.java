package main.java.com.alexhennieroed.desolationserver.game.model;

/**
 * Represents an actor in the game
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Actor extends WorldObject {

    public Actor(int x, int y) {
        super(x, y);
    }

    /**
     * Moves the actor in the given direction
     * @param dir the direction of movement
     * @return a boolean representing success
     */
    public boolean move(Direction dir) {
        int newX = worldX + dir.getDx();
        int newY = worldY + dir.getDy();
        if (newX == newY) {
            worldX = newX;
            worldY= newY;
            return true;
        }
        return false;
    }

    @Override
    public String toString() { return "[ACT]### (" + getWorldX() + "," + getWorldY() + ")"; }

}
