import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class InputHandler implements Runnable {
    private final BufferedReader consoleReader;
    private final ServerConnection connection;
    private final Client client;
    private volatile boolean running = true;

    public InputHandler(BufferedReader consoleReader, ServerConnection connection, Client client) {
        this.consoleReader = consoleReader;
        this.connection = connection;
        this.client = client;
    }

    public void stop() {
        running = false;
        try {
            consoleReader.close(); // Force readLine() closing if it is blocked
        } catch (IOException ignored) {}
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = connection.getOutputStream();
            String input;

            while (running && (input = consoleReader.readLine()) != null) {
                out.writeObject(input);
                out.flush();
            }
        } catch (IOException e) {
            if (running)
                System.err.println("InputHandler stopped: " + e.getMessage());
        }
    }
}