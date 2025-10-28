import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "resources/logs.txt";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Object lock = new Object();

    /**
     * Logs a message to console and file.
     *
     * @param source e.g., "SERVER", "CLIENT-1"
     * @param message the message text
     */
    public static void log(String source, String message) {
        String timestamp = LocalTime.now().format(TIME_FORMAT);
        String fullMessage = String.format("[%s] [%s] %s", timestamp, source, message);

        // Print to console
        System.out.println(fullMessage);

        // Write to file
        synchronized (lock) { // thread-safe
            try {
                File file = new File(LOG_FILE);
                file.getParentFile().mkdirs(); // Create resources/ if missing

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.write(fullMessage);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Logger error: " + e.getMessage());
            }
        }
    }
}