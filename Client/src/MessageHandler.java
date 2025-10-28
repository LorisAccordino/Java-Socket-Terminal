import model.Message;

public class MessageHandler {
    public static void handleMessage(Message msg) {
        // Return carriage
        System.out.print("\r");

        // Switch based on message type
        switch (msg.type()) {
            case INFO, BROADCAST -> // Overwrite the current line and new line
                    System.out.println(msg.content());
            case INPUT_REQUEST -> // New line, before printing the request
                    System.out.print(msg.content());
        }
    }
}