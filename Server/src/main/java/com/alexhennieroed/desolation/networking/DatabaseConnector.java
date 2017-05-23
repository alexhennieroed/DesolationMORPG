package main.java.com.alexhennieroed.desolation.networking;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import main.java.com.alexhennieroed.desolation.Settings;
import org.bson.Document;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages the connection to the MongoDB database
 * @author Alexander Hennie-Roed
 * @version 1.0.0
 */
public class DatabaseConnector {

    private MongoDatabase database;
    private MongoCollection<Document> usercol;
    private int numusers;

    /**
     * Connects to or creates the database
     */
    public DatabaseConnector() {
        numusers = 0;
        connect();
        usercol = database.getCollection("users");
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
     * Adds a new user to the database
     * @param user the user to add
     */
    public boolean addUser(User user) {
        MongoCursor<Document> cursor = usercol.find(eq("username", user.getUsername())).iterator();
        if (cursor.hasNext()) {
            System.out.println("User already exists.");
            return false;
        }
        Document doc = new Document("username", user.getUsername())
                .append("password", user.getPassword());
        usercol.insertOne(doc);
        numusers++;
        return true;
    }

    /**
     * Removes the user from thte database
     * @param user the user to remove
     */
    public boolean removeUser(User user) {
        if (checkUser(user)) {
            usercol.deleteOne(eq("username", user.getUsername()));
            numusers--;
            return true;
        } else {
            System.out.println("User didn't check out.");
        }
        return false;
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
        System.out.println("User does not exist.");
        return false;
    }

    /**
     * Finds the user in the database based on the username
     * @param username the username of the user
     * @return the user in the database
     */
    public User getUser(String username) {
        MongoCursor<Document> cursor = usercol.find(eq("username", username))
                .projection(fields(include("username", "password"), excludeId()))
                .iterator();
        if (cursor.hasNext()) {
            List list = new ArrayList(cursor.next().values());
            return new User((String) list.get(0), (String) list.get(1));
        }
        return null;
    }

    /**
     * Returns the database being used
     * @return the database
     */
    public MongoDatabase getDatabase() { return database; }

    /**
     * Returns the number of users on the database
     * @return the number of users
     */
    public int getNumusers() { return numusers; }

}
