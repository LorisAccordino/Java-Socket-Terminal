import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ServerConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connected to server " + host + ":" + port);
    }

    public ObjectInputStream getInputStream() {
        return in;
    }

    public ObjectOutputStream getOutputStream() {
        return out;
    }

    public void sendObject(Object obj) throws IOException {
        out.writeObject(obj);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}