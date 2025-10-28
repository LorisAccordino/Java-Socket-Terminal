import model.Command;
import model.Message;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Handles all client-side I/O:
 *  - Reads user input from the console and sends it to the server
 *  - Listens for messages from the server and passes them to a handler
 */
public class ClientIOHandler {
    private final BufferedReader consoleReader;
    private final ServerConnection connection;
    private final Client client;

    private Thread inputThread;
    private Thread readerThread;
    private volatile boolean running = false;

    /** Interface used to handle messages received from the server */
    public interface ServerMessageHandler {
        void handle(Object message);
    }

    public ClientIOHandler(BufferedReader consoleReader, ServerConnection connection,
                           Client client) {
        this.consoleReader = consoleReader;
        this.connection = connection;
        this.client = client;
    }

    /** Starts both the input and reader threads */
    public void start() {
        if (running) return;
        running = true;

        inputThread = new Thread(new InputHandler());
        readerThread = new Thread(new ServerReader());

        inputThread.start();
        readerThread.start();
        try {
            inputThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Stops everything gracefully */
    public void stop() {
        running = false;

        System.out.print("Shutting down... \nPress ENTER to exit...");

        try {
            connection.close();
        } catch (IOException ignored) {}

        try {
            if (inputThread != null) inputThread.interrupt();
            if (readerThread != null) readerThread.interrupt();
        } catch (Exception ignored) {}
    }

    // ========================================================
    // === Inner thread: reads user input from the console =====
    // ========================================================
    private class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                ObjectOutputStream out = connection.getOutputStream();
                String input;

                while (running && (input = consoleReader.readLine()) != null) {
                    input = input.trim();

                    // Send only non-empty lines
                    if (!input.isEmpty()) {
                        out.writeObject(input);
                        out.flush();
                    }
                }
            } catch (IOException e) {
                if (running)
                    System.err.println("InputHandler stopped: " + e.getMessage());
            } finally {
                running = false;
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ========================================================
    // === Inner thread: listens for messages from server ======
    // ========================================================
    private class ServerReader implements Runnable {
        @Override
        public void run() {
            try {
                ObjectInputStream in = connection.getInputStream();
                Object obj;

                while (running && (obj = in.readObject()) != null) {
                    handleServerMessage(obj);
                }
            } catch (Exception e) {
                if (running)
                    System.err.println("Connection closed: " + e.getMessage());
            } finally {
                running = false;
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }

        private void handleServerMessage(Object obj) {
            if (obj instanceof Message msg) {
                MessageHandler.handleMessage(msg);
            } else if (obj instanceof Command cmd) {
                CommandHandler.handleCommand(cmd);
            }else if (obj instanceof User user) {
                client.setLoggedUser(user);
            }
        }
    }
}