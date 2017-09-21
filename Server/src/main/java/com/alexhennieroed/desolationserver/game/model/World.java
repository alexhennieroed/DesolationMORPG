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

    /**
     * Default World constructor
     */
    public World() {
        this.objectMap = makeObjectMap();
    }

    /**
     * Creates a World from a list of mapObjects
     * @param mapObjects a list of object to include
     */
    public World(List<WorldObject> mapObjects) {
        this.objects = mapObjects;
        this.objectMap = makeObjectMap();
    }

    /**
     * Initializes an object map from a list of WorldObjects
     * @return the object map
     */
    private WorldObject[][] makeObjectMap() {
        WorldObject[][] oMap = new WorldObject[10][10];
        objects.forEach(object -> oMap[object.getWorldY()][object.getWorldX()] = object);
        return oMap;
    }

    /**
     * Updates all objects in the ObjectMap
     */
    public void updateObjectMap() {
        objectMap = makeObjectMap();
    }

    /**
     * Returns a list of all objects near a target object
     * @param target the object of interest
     * @return the list of objects in a 5x5 surrounding area
     */
    public List<WorldObject> getNearbyObjects(WorldObject target) {
        List returnList = new ArrayList();
        int ystart = (target.getWorldY() - 2) <= 0 ? 0 : target.getWorldY() - 2;
        int yend = (target.getWorldY() + 2) >= 10 ? 10 : target.getWorldY() + 2;
        int xstart = (target.getWorldX() - 2) <= 0 ? 0 : target.getWorldX() - 2;
        int xend = (target.getWorldX() + 2) >= 10 ? 10 : target.getWorldX() + 2;
        for (int i = ystart; i < yend; i++) {
            for (int j = xstart; j < xend; j++) {
                if (objectMap[i][j] != null) {
                    returnList.add(objectMap[i][j]);
                }
            }
        }
        return returnList;
    }

}
