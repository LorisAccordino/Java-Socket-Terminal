import model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    public static Client Instance = null;
    private static final String HOST = "localhost";
    private static final int PORT = 8000;

    private User loggedUser = null;
    private ClientIOHandler ioHandler;

    public static void main(String[] args) {
        Instance = new Client();
        Instance.start();
    }

    public void start() {
        while (true) {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            try (ServerConnection connection = new ServerConnection(HOST, PORT)) {
                ioHandler = new ClientIOHandler(consoleReader, connection, this);
                ioHandler.start();
            } catch (Exception e) {
                System.err.println("Connection error: " + e.getMessage());
            }

            // After logout or disconnection
            System.out.print("Do you want to reconnect? (yes/no): ");
            try {
                String choice = consoleReader.readLine();
                if (!"yes".equalsIgnoreCase(choice)) {
                    System.out.println("Shutting down...");
                    break;
                }
            } catch (Exception ignored) { }
        }
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(User user) {
        loggedUser = user;
    }

    public void exit() {
        ioHandler.stop();
    }

    public String getPrompt() {
        String loggedUsername = getLoggedUser() != null ? getLoggedUser().getUsername() : "guest";
        return loggedUsername + "@" + HOST + "> ";
    }
}