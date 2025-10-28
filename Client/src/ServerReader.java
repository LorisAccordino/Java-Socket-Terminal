import java.io.ObjectInputStream;

public class ServerReader implements Runnable {
    private final ObjectInputStream in;
    private final ServerMessageHandler handler;
    private volatile boolean running = true;

    public interface ServerMessageHandler {
        void handle(Object obj);
    }

    public ServerReader(ObjectInputStream in, ServerMessageHandler handler) {
        this.in = in;
        this.handler = handler;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            Object obj;
            while (running && (obj = in.readObject()) != null) {
                handler.handle(obj);
            }
        } catch (Exception e) {
            if (running)
                System.err.println("Connection closed: " + e.getMessage());
        } finally {
            running = false;
            try {
                in.close();
            } catch (Exception ignored) {}
        }
    }
}