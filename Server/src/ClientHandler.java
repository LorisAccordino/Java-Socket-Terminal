import model.*;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Server server;
    private final Socket socket;
    private final LoginSystem loginSystem = LoginSystem.getInstance();
    private final CommandHandler commandHandler = new CommandHandler(this);

    private User loggedUser = null;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    private void login() throws IOException, ClassNotFoundException {
        sendMessage(MessageType.INFO, "Welcome! Please log in.");
        sendMessage(MessageType.INPUT_REQUEST, "Username: ");
        String username = (String) in.readObject();

        sendMessage(MessageType.INPUT_REQUEST, "Password: ");
        String password = (String) in.readObject();

        loggedUser = loginSystem.login(username, password);

        if (loggedUser == null) {
            sendMessage(MessageType.INFO, "Invalid credentials. Connection closed.");
            return;
        }

        sendMessage(MessageType.INFO, "Login successful! Welcome " + loggedUser.getUsername() +
                " (" + loggedUser.getRole() + ")");

        // Send model.User object to the client
        sendObject(loggedUser);
    }

    @Override
    public void run() {
        String socketStr = socket.getInetAddress() + ":" + socket.getPort();
        Logger.log("SERVER", "New client connected: " + socketStr);

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            login();
            if (loggedUser == null) return;

            // Welcome the client
            Logger.log("SERVER","Client logged in: " + socketStr);
            sendMessage(MessageType.INFO, "You are logged in. Type '/exit' to quit.");
            sendMessage(MessageType.INFO, "Enter command (/help, /list, /cmd, /exit ...) or freely chat");

            // Notify others
            broadcastMessage("server", loggedUser.getUsername() + " has join the chat :)");

            // Main loop
            while (true) {
                // Refresh the prompt on the client
                sendCommand(CommandType.REFRESH_REQUEST);

                // Handle string input
                Object obj = in.readObject();
                if (!(obj instanceof String input)) continue;

                // Trim and blank check
                input = input.trim();
                if (input.isEmpty()) continue;

                // Log everything
                Logger.log(loggedUser.getUsername().toUpperCase(), input);

                // Handle command
                if (input.startsWith("/")) {
                    commandHandler.handle(input);
                    continue;
                }

                // --- Normal chat ---
                broadcastMessage(loggedUser.getUsername(), input);
            }
        } catch (IOException | ClassNotFoundException e) {
            Logger.log("SERVER","Client connection error: " + e.getMessage());
        } finally {
            disconnect();
            Logger.log("SERVER","Client disconnected: " + socketStr);
        }
    }

     void sendMessage(MessageType type, String content) {
         sendObject(new Message(type, content));
     }

    void sendCommand(CommandType type) {
        sendCommand(type, "");
    }

    void sendCommand(CommandType type, String args) {
        sendObject(new Command(type, args));
    }

    private void sendObject(Object obj) {
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            String name = loggedUser != null ? loggedUser.getUsername() : "unknown";
            System.err.println("Error sending object to " + name + ": " + e.getMessage());
        }
    }

    void broadcastMessage(String sender, String input) {
        if (input.isBlank()) return;
        String content = "[" + sender + "]: " + input;
        synchronized (Server.getClients()) {
            for (ClientHandler client : Server.getClients()) {
                if (client != this && client.getLoggedUser() != null) {
                    client.sendMessage(MessageType.BROADCAST, content);
                    client.sendCommand(CommandType.REFRESH_REQUEST);
                }
            }
        }
    }

    private void disconnect() {
        // Remove from the list and close socket/streams
        Server.getClients().remove(this);

        try {
            if (out != null) out.close();
        } catch (Exception ignored) {}
        try {
            if (in != null) in.close();
        } catch (Exception ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}

        // Notify to the other clients
        if (loggedUser != null) {
            broadcastMessage("server", loggedUser.getUsername() + " has left the chat :(");
        }
    }

    public User getLoggedUser() {
        return loggedUser;
    }
}