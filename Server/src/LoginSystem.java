import model.Role;
import model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LoginSystem {
    private static final LoginSystem instance;
    private final Map<String, User> users = new HashMap<>();
    private static final String FILE_PATH = "resources/users.dat";

    // Static block
    static {
        // Automatically load the instance during the class loading
        instance = new LoginSystem();
    }

    // Private constructor (Singleton pattern)
    private LoginSystem() {
        loadFromFile();

        // If not any users, create some default users
        if (users.isEmpty()) {
            register("user1", "Passw0rd", Role.USER);
            register("admin", "admin", Role.ADMIN);
            saveToFile();
        }
    }

    // Get instance method
    public static synchronized LoginSystem getInstance() { return instance; }

    // Login a user
    public User login(String username, String password) {
        User u = users.get(username);
        if (u != null && u.checkPassword(password)) {
            return u;
        }
        return null; // login fallito
    }


    // Register a new user
    public synchronized boolean register(User user) {
        if (userExists(user.getUsername())) return false; // Yet registered
        users.put(user.getUsername(), user);
        return true;
    }

    // Register overload
    public synchronized boolean register(String username, String password, Role role) {
        return register(new User(username, password, role));
    }

    // Check user existence
    public synchronized boolean userExists(String username) {
        return users.containsKey(username);
    }

    // Remove a user
    public synchronized boolean removeUser(String username) {
        return users.remove(username) != null;
    }

    // Set permissions/roles
    public synchronized boolean setRole(String username, Role newRole) {
        User user = users.get(username);
        if (user == null || user.getRole() == newRole) return false;
        user.setRole(newRole);
        return true;
    }


    // Get the user list (readonly)
    public synchronized Map<String, User> getAllUsers() {
        return Map.copyOf(users);
    }


    //--- SERIALIZATION ---

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error during users saving: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        Path path = Path.of(FILE_PATH);

        try {
            Files.createDirectories(path.getParent());

            if (!Files.exists(path)) {
                Files.createFile(path);
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
                Object obj = ois.readObject();
                if (obj instanceof Map<?, ?> loaded) {
                    users.putAll((Map<String, User>) loaded);
                }
            }

        } catch (EOFException e) {
            // EOF: do nothing
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during users loading: " + e.getMessage());
        }
    }
}