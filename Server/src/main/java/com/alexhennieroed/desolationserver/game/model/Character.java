package main.java.com.alexhennieroed.desolationserver.game.model;

import java.util.List;

/**
 * A player controlled actor
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class Character extends Actor {

    private String name;
    private int level;
    private int maxHealth;
    private int currentHealth;
    private int maxStamina;
    private int currentStamina;
    private int expToNext;
    private int currentExp;

    /**
     * Creates a new character with the specified name
     * @param name the character's name
     */
    public Character(String name) {
        super(0,0);
        this.name = name;
        this.level = 1;
        this.maxHealth = 10;
        this.currentHealth = 10;
        this.maxStamina = 10;
        this.currentStamina = 10;
        this.expToNext = 10;
        this.currentExp = 0;
    }

    /**
     * Creates a new character with the data in the list
     * @param list the list of data
     */
    public Character(List list) throws IllegalArgumentException {
        super(0,0);
        if (list.size() != 8) {
            throw new IllegalArgumentException("List doesn't contain the proper number of elements.");
        }
        this.name = (String) list.get(0);
        this.level = (Integer) list.get(1);
        this.maxHealth = (Integer) list.get(2);
        this.currentHealth = (Integer) list.get(3);
        this.maxStamina = (Integer) list.get(4);
        this.currentStamina = (Integer) list.get(5);
        this.expToNext = (Integer) list.get(6);
        this.currentExp = (Integer) list.get(7);
    }

    public Character(String name, int x, int y) {
        this(name);
        this.worldX = x;
        this.worldY = y;
    }

    /**
     * Returns the name of the character
     * @return the name of the character
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the level of the character
     * @return the level of the character
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the max health of the character
     * @return the character's max health
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Returns the current health of the character
     * @return the current health of the character
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Returns the experience to the next level
     * @return the experience to the next level
     */
    public int getExpToNext() {
        return expToNext;
    }

    /**
     * Returns the current exp of the character
     * @return the current exp of the character
     */
    public int getCurrentExp() {
        return currentExp;
    }

    /**
     * Returns the max stamina of the character
     * @return the max stamina of the character
     */
    public int getMaxStamina() {
        return maxStamina;
    }

    /**
     * Returns the current stamina of the character
     * @return the current stamina of the character
     */
    public int getCurrentStamina() {
        return currentStamina;
    }

    /**
     * Turns the character into a packet string
     * @return the packet string
     */
    public String toPacketData() {
        return "char_data:" + name + ":" + level + ":" + maxHealth + ":"
                + currentHealth + ":" + maxStamina + ":" +
                currentStamina + ":" + expToNext + ":" + currentExp;
    }

    @Override
    public String toString() { return name + " [Lv. " + level + "]"; }
}
