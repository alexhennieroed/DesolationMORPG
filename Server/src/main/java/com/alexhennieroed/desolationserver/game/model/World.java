package main.java.com.alexhennieroed.desolationserver.game.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game world
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class World {

    private WorldObject[][] objectMap;
    private List<WorldObject> objects = new ArrayList<>();

    public World() {
        this.objectMap = makeObjectMap();
    }

    private WorldObject[][] makeObjectMap() {
        WorldObject[][] oMap = new WorldObject[10][10];
        objects.forEach(object -> oMap[object.getWorldY()][object.getWorldX()] = object);
        return oMap;
    }

}
