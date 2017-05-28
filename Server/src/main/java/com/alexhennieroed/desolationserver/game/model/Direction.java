package main.java.com.alexhennieroed.desolationserver.game.model;

/**
 * An enum representing the cardinal directions
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public enum Direction {

    NORTH(0, 1),
    SOUTH(0, -1),
    EAST(1, 0),
    WEST(-1, 0),
    NORTHEAST(1, 1),
    NORTHWEST(-1, 1),
    SOUTHEAST(1, -1),
    SOUTHWEST(-1, -1);

    private int dx, dy;

    /**
     * Defines a Direction
     * @param dx the change in x of this direction
     * @param dy the change in y of this direction
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns the change in x
     * @return the change in x
     */
    public int getDx() { return dx; }

    /**
     * Returns the change in y
     * @return the change in y
     */
    public int getDy() { return dy; }

}
