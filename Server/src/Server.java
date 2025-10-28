import java.io.*;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int PORT = 8000;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    public static Set<ClientHandler> getClients() { return clients; }

    public static void main(String[] args) {
        Logger.log("SERVER", "Starting server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // A thread for each ClientHandler
                ClientHandler handler = new ClientHandler(serverSocket.accept());
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            Logger.log("SERVER", "Server error: " + e.getMessage());
        }
    }
}