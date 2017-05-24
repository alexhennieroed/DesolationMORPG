package main.java.com.alexhennieroed.desolationserver.networking;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;

import main.java.com.alexhennieroed.desolationserver.Server;
import main.java.com.alexhennieroed.desolationserver.Settings;
import main.java.com.alexhennieroed.desolationserver.game.model.Character;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages the connection to the MongoDB database
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class DatabaseConnector {

    private final Server myServer;

    private MongoDatabase database;
    private MongoCollection<Document> usercol;
    private MongoCollection<Document> charcol;
    private int numusers;

    /**
     * Connects to or creates the database
     */
    public DatabaseConnector(Server server) {
        this.myServer = server;
        numusers = 0;
        connect();
        usercol = database.getCollection("users");
        charcol = database.getCollection("characters");
        MongoCursor<Document> cursor = usercol.find().iterator();
        while (cursor.hasNext()) {
            numusers++;
            cursor.next();
        }
    }

    /**
     * Connects to the database from Settings
     */
    private void connect() {
        MongoClient mongoClient = new MongoClient(new ServerAddress(Settings.MONGODB_HOST,
                Settings.MONGODB_PORT));
        database = mongoClient.getDatabase(Settings.DATABASE_NAME);
    }

    /**
     * Gets a list of all users in the database
     * @return the list of users
     */
    public List<User> getAllUsers() {
        List userList = new ArrayList();
        MongoCursor<Document> cursor = usercol.find().projection(excludeId()).iterator();
        while (cursor.hasNext()) {
            List list = new ArrayList(cursor.next().values());
            User user = new User((String) list.get(0), (String) list.get(1));
            user.setCharacter(getCharacter((ObjectId) list.get(2)));
            userList.add(user);
        }
        return userList;
    }

    /**
     * Updates all of the users in the list
     * @param userList the list of users to update
     */
    public void updateAllUsers(List<User> userList) {
        for (User u : userList) {
            updateUser(u);
        }
        myServer.getLogger().logDatabaseEvent("Updated all users in database.");
    }

    /**
     * Adds a new user to the database
     * @param user the user to add
     * @return a boolean representing success
     */
    public boolean addUser(User user) {
        if (numusers >= Settings.MAX_USER_COUNT) {
            myServer.getLogger().logDatabaseEvent("Failed to create new user " +
                user.getUsername() + " because the maximum user count has been reached.");
        }
        MongoCursor<Document> cursor = usercol.find(eq("username", user.getUsername())).iterator();
        if (cursor.hasNext()) {
            myServer.getLogger().logDatabaseEvent("Failed to create new user " + user.getUsername()
                    + " because the username already exists.");
            return false;
        }
        Document doc = new Document("username", user.getUsername())
                .append("password", user.getPassword())
                .append("character_id", addCharacter(user.getCharacter()));
        usercol.insertOne(doc);
        numusers++;
        return true;
    }

    /**
     * Adds a new character to the database
     * @param character the character to add
     * @return the id of the character's document
     */
    private ObjectId addCharacter(Character character) {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("name", character.getName())
                .append("level", character.getLevel())
                .append("max_health", character.getMaxHealth())
                .append("current_health", character.getCurrentHealth())
                .append("max_stamina", character.getMaxStamina())
                .append("current_stamina", character.getCurrentStamina())
                .append("exp_to_next", character.getExpToNext())
                .append("current_exp", character.getCurrentExp());
        charcol.insertOne(doc);
        return id;
    }

    /**
     * Removes the user from the database
     * @param user the user to remove
     */
    public boolean removeUser(User user) {
        if (checkUser(user)) {
            MongoCursor<Document> cursor = usercol.find(eq("username", user.getUsername()))
                    .projection(fields(include("character_id")))
                    .iterator();
            ObjectId id = (ObjectId) new ArrayList<>(cursor.next().values()).get(0);
            removeCharacter(id);
            usercol.deleteOne(eq("username", user.getUsername()));
            numusers--;
            myServer.getLogger().logDatabaseEvent("Removed user " + user.getUsername() + ".");
            return true;
        } else {
            myServer.getLogger().logDatabaseEvent("Failed to remove user " + user.getUsername() +
                " because the credentials did not match.");
        }
        return false;
    }

    /**
     * Removes the character from the database
     * @param id the character_id in the user
     */
    private void removeCharacter(ObjectId id) {
        charcol.deleteOne(eq("_id", id));
    }

    /**
     * Checks the credentials of the user
     * @param user the user in question
     * @return a boolean representing the success
     */
    public boolean checkUser(User user) {
        MongoCursor<Document> cursor = usercol.find(eq("username", user.getUsername()))
                .projection(fields(include("password"), excludeId()))
                .iterator();
        if (cursor.hasNext()) {
            List list = new ArrayList(cursor.next().values());
            return user.getPassword().equals(list.get(0));
        }
        return false;
    }

    /**
     * Finds the user in the database based on the username
     * @param username the username of the user
     * @return the user in the database
     */
    public User getUser(String username) {
        MongoCursor<Document> cursor = usercol.find(eq("username", username))
                .projection(fields(include("username", "password", "character_id"), excludeId()))
                .iterator();
        if (cursor.hasNext()) {
            List list = new ArrayList(cursor.next().values());
            User user = new User((String) list.get(0), (String) list.get(1));
            user.setCharacter(getCharacter((ObjectId) list.get(2)));
            return user;
        }
        myServer.getLogger().logDatabaseEvent("Failed to get user " + username +
            " because the user does not exist.");
        return null;
    }

    /**
     * Finds the character in the database based on the ObjectId
     * @param id the character_id in the user's document
     * @return the character in the database
     */
    private Character getCharacter(ObjectId id) {
        MongoCursor<Document> cursor = charcol.find(eq("_id", id))
                .projection(fields(excludeId()))
                .iterator();
        if (cursor.hasNext()) {
            List list = new ArrayList(cursor.next().values());
            return new Character(list);
        }
        return null;
    }

    /**
     * Updates a user in the database
     * @param user the user to update
     */
    public void updateUser(User user) {
        if (checkUser(user)) {
            usercol.updateOne(eq("username", user.getUsername()),
                    set("password", user.getPassword()));
            MongoCursor<Document> cursor = usercol.find(eq("username", user.getUsername()))
                    .projection(fields(include("character_id")))
                    .iterator();
            ObjectId id = (ObjectId) new ArrayList<>(cursor.next().values()).get(0);
            updateCharacter(user.getCharacter(), id);
        } else {
            myServer.getLogger().logDatabaseEvent("Failed to update user" + user.getUsername() +
                " because the credentials did not match.");
        }
    }

    /**
     * Updates a character in the database
     * @param character the character to update
     * @param id the character_id in the user document
     */
    private void updateCharacter(Character character, ObjectId id) {
        charcol.updateOne(eq("_id", id),
                combine(set("level", character.getLevel()),
                        set("max_health", character.getMaxHealth()),
                        set("current_health", character.getCurrentHealth()),
                        set("max_stamina", character.getMaxStamina()),
                        set("current_stamina", character.getCurrentStamina()),
                        set("exp_to_next", character.getExpToNext()),
                        set("current_exp", character.getCurrentExp())));
    }

    /**
     * Returns the number of users on the database
     * @return the number of users
     */
    public int getNumusers() { return numusers; }

}
