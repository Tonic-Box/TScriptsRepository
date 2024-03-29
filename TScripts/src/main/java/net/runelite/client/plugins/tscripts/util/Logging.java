package net.runelite.client.plugins.tscripts.util;

import lombok.SneakyThrows;
import net.runelite.api.ChatMessageType;
import net.unethicalite.client.Static;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.io.Writer;
import static net.runelite.client.plugins.tscripts.TScriptsPlugin.*;

/**
 * Contains methods for logging errors and messages.
 */
public class Logging {
    /**
     * Logs an error to the error log file.
     *
     * @param ex the exception to log
     */
    public static void errorLog(Exception ex) {
        Thread thread = new Thread(new Runnable() {
            @SneakyThrows
            public void run() {
                Writer fileWriter = new FileWriter(HOME_DIR + "ErrorLogs.txt", true);
                fileWriter.write(ex + "\n");
                fileWriter.close();
            }
        });
        ex.printStackTrace();
        thread.start();
    }

    /**
     * Logs a message to the chat as trade request.
     *
     * @param message the message to log
     */
    public static void logToChat(String message) {
        Static.getClientThread().invoke(() -> {
            try {
                Static.getClient().addChatMessage(ChatMessageType.TRADEREQ, "TSCRIPTS_LOGGER:" + message, message, "", true);
            }
            catch(Exception ignored) {}
        });
    }

    /**
     * Copies a string to the clipboard.
     * @param out the string to copy
     */
    public static void copyToClipboard(String out) {
        StringSelection selection = new StringSelection(out);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}
