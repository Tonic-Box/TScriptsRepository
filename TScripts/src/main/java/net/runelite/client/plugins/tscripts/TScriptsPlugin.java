package net.runelite.client.plugins.tscripts;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.ui.TScriptsPanel;
import net.runelite.client.plugins.tscripts.util.*;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The main plugin class for TScripts.
 */
public class TScriptsPlugin extends JFrame {
    public static final String OPENOSRS = ".openosrs";
    public static final File RUNELITE_DIR = new File(System.getProperty("user.home"), OPENOSRS);
    @Getter
    @Setter
    private String profile = "[Default]";
    public ConfigHandler configHandler;
    @Getter
    private CompletionProvider baseCompletion;
    public static final String START_DIR = RUNELITE_DIR + File.separator + "HPQScripts" + File.separator;
    public static String HOME_DIR;
    //private MulticastReceiver multicastReceiver;
    @Getter
    private static TScriptsPanel panel;

    public static void main(String[] args) {
        FlatDarkLaf.setup();

        TScriptsPlugin plugin = new TScriptsPlugin();
        plugin.startUp();
        panel = new TScriptsPanel();
        panel.init(plugin);

        plugin.setTitle("TScripts: Standalone Scripting Environment");
        plugin.setSize(800, 600);
        plugin.setLayout(new BorderLayout());
        plugin.add(panel, BorderLayout.CENTER);
        plugin.setVisible(true);
    }

    /**
     * Called when the plugin is started up.
     */
    protected void startUp() {
        HOME_DIR = START_DIR;
        try {
            Files.createDirectories(Paths.get(RUNELITE_DIR + File.separator + "HPQScripts" + File.separator));
        }
        catch(Exception ex) {
            Logging.errorLog(ex);
        }

        profile = "[Default]";
        configHandler = new ConfigHandler(getProfilePath(profile));
        configHandler.validateConfig();
        new MethodManager(this);
        this.baseCompletion = CompletionSupplier.createBaseCompletionProvider();
    }

    /**
     * Gets the path to the profile directory.
     *
     * @param profile the profile name
     * @return the path to the profile directory
     */
    public String getProfilePath(String profile) {
        if(profile.equals("[Default]"))
            return  START_DIR;
        else
            return (START_DIR + profile + ".profile/").replace("\\", "/");
    }

    /**
     * Gets the path to a script.
     *
     * @param id the script ID
     * @param profile the profile name
     * @return the path to the script
     */
    public String getScriptPath(String id, String profile) {
        return getProfilePath(profile) + id + ".script";
    }
}
