import model.CommandType;
import model.MessageType;
import model.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class CommandHandler {

    private final ClientHandler client;
    private final LoginSystem loginSystem = LoginSystem.getInstance();

    public CommandHandler(ClientHandler client) {
        this.client = client;
    }

    private boolean checkAdmin() {
        if (client.getLoggedUser().getRole() != Role.ADMIN) {
            client.sendMessage(MessageType.INFO, "You are not an admin!");
            return false;
        }
        return true;
    }

    public void handle(String input) {
        if (!input.startsWith("/")) return;
        input = input.substring(1);

        String[] parts = input.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help":
                showHelp();
                break;

            case "list":
                listUsers();
                break;
            case "exit":
                client.sendCommand(CommandType.EXIT);
                break;

            case "register":
                if (!checkAdmin()) return;
                if (parts.length < 3) {
                    client.sendMessage(MessageType.INFO, "Usage: /register username password");
                    break;
                }
                addUser(parts[1], parts[2]);
                break;

            case "unregister":
                if (!checkAdmin()) return;
                if (parts.length < 2) {
                    client.sendMessage(MessageType.INFO, "Usage: /unregister username");
                    break;
                }
                removeUser(parts[1]);
                break;
            case "kick":
                if (!checkAdmin()) return;
                if (parts.length < 2) {
                    client.sendMessage(MessageType.INFO, "Usage: /kick username");
                    break;
                }
                kickUser(parts[1]);
                break;
            case "sudo":
                if (!checkAdmin()) return;
                if (parts.length < 2) {
                    client.sendMessage(MessageType.INFO, "Usage: /sudo username");
                    break;
                }
                makeAdmin(parts[1]);
                break;
            case "cmd":
                if (!checkAdmin()) return;
                executeCommand(parts[1] + parts[2], client);
                break;
            default:
                client.sendMessage(MessageType.INFO, "Unknown command: " + input);
        }
    }

    private void showHelp() {
        client.sendMessage(MessageType.INFO,
                "/help - Show this help message\n" +
                        "/list - List online users\n" +
                        "/register username password - Add a new user (admin only)\n" +
                        "/unregister username - Remove a user (admin only)\n" +
                        "/kick username - Temporarily kick a user (admin only)\n" +
                        "/sudo username - Promote a user to admin (admin only)\n" +
                        "/cmd command - Execute a system command\n" +
                        "/exit - Disconnect from server\n");
    }

    private void listUsers() {
        StringBuilder sb = new StringBuilder("Users online:\n");
        for (ClientHandler c : Server.getClients()) {
            sb.append("- ").append(c.getLoggedUser().getUsername()).append("\n");
        }
        client.sendMessage(MessageType.INFO, sb.toString());
    }

    private void addUser(String username, String password) {
        boolean success = loginSystem.register(username, password, Role.USER);
        if (success) {
            client.sendMessage(MessageType.INFO, "User '" + username + "' added successfully.");
        } else {
            client.sendMessage(MessageType.INFO, "User '" + username + "' already exists.");
        }
    }

    private void removeUser(String username) {
        boolean success = loginSystem.removeUser(username);
        if (success) {
            client.sendMessage(MessageType.INFO, "User '" + username + "' removed successfully.");
        } else {
            client.sendMessage(MessageType.INFO, "User '" + username + "' does not exist.");
        }
    }

    private void kickUser(String username) {
        boolean success = false;
        synchronized (Server.getClients()) {
            for (ClientHandler client : Server.getClients()) {
                if (client != this.client && client.getLoggedUser() != null) {
                    if (Objects.equals(client.getLoggedUser().getUsername(), username)) {
                        client.sendCommand(CommandType.EXIT);
                        success = true;
                        break;
                    }
                }
            }
        }

        if (success) {
            client.sendMessage(MessageType.INFO, "User '" + username + "' kicked successfully.");
        } else {
            client.sendMessage(MessageType.INFO, "User '" + username + "' is not online.");
        }
    }

    private void makeAdmin(String username) {
        boolean success = loginSystem.setRole(username, Role.ADMIN);
        if (success) {
            client.sendMessage(MessageType.INFO, "User '" + username + "' is now an admin.");
        } else {
            client.sendMessage(MessageType.INFO, "User '" + username + "' does not exist or is already admin.");
        }
    }

    public void executeCommand(String command, ClientHandler client) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("sh", "-c", command);
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    client.sendMessage(MessageType.INFO, line);
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            client.sendMessage(MessageType.INFO, "Error executing command: " + e.getMessage());
        }
    }
}
