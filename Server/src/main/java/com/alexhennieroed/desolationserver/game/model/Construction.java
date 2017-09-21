package main.java.com.alexhennieroed.desolationserver.game.model;

/**
 * Represents a construction in the world
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Construction extends WorldObject {

    private ConstructionType type;

    /**
     * Creates a new construction
     * @param x the world x of the construction
     * @param y the world y of the construction
     * @param type the type of construction
     */
    public Construction(int x, int y, ConstructionType type) {
        super(x, y);
        this.type = type;
    }

    @Override
    public String toString() {
        return "[CON]" + type.getName() + " (" + getWorldX() + "," + getWorldY() + ")";
    }

    /**
     * An enum of different types of constructions
     */
    public enum ConstructionType {

        WALL("Wall", false, false);

        private String name;
        private boolean destructible;
        private boolean penetrable;

        /**
         * Defines a new type of construction
         * @param name the name of the construction
         * @param destructible whether or not this can be destroyed
         * @param penetrable whether or not this can be penetrated
         */
        ConstructionType(String name, boolean destructible, boolean penetrable) {
            this.name = name;
            this.destructible = destructible;
            this.penetrable = penetrable;
        }

        /**
         * Returns the name
         * @return the name
         */
        public String getName() { return name; }

        /**
         * Returns the destructible property
         * @return the destructible property
         */
        public boolean isDestructable() { return destructible; }

        /**
         * Returns the penetrable property
         * @return the penetrable property
         */
        public boolean isPenetrable() { return penetrable; }

    }

}
