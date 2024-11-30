package net.runelite.client.plugins.tscripts.util;

import net.runelite.client.plugins.tscripts.ui.editor.ScriptEditor;
import javax.swing.*;
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
        logToEditor(ex.getMessage(), Color.RED);
        ThreadPool.submit(() -> {
            try {
                Writer fileWriter = new FileWriter(HOME_DIR + "ErrorLogs.txt", true);
                fileWriter.write(ex + "\n");
                fileWriter.close();
            } catch (Exception ignored) {
            }
        });
        ex.printStackTrace();
    }

    /**
     * Logs an info message to the console.
     * @param message the message to log
     */
    public static void info(String message)
    {
        logToEditor(message, Color.YELLOW);
    }

    /**
     * Logs a message to the chat as trade request.
     *
     * @param message the message to log
     */
    public static void logToChat(String message) {
        System.out.println(message);
    }

    /**
     * Copies a string to the clipboard.
     * @param out the string to copy
     */
    public static void copyToClipboard(String out) {
        StringSelection selection = new StringSelection(out);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public static void logToEditor(String message, Color color) {
        ScriptEditor editor = ScriptEditor.get();
        if (editor == null)
            return;
        SwingUtilities.invokeLater(() -> editor.logToConsole(message, color));
    }
}
