import model.Command;

public class CommandHandler {
    public static void handleCommand(Command cmd) {
        // Switch based on command type
        switch (cmd.type()) {
            case REFRESH_REQUEST -> // Always print the prompt after the message
                    System.out.print(Client.Instance.getPrompt());
            case EXIT -> {
                Client.Instance.exit();
            }
        }
    }
}