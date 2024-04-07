package net.runelite.client.plugins.tscripts.util;

import net.runelite.client.config.Keybind;

import java.io.*;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles the configuration of the scripts
 */
public class ConfigHandler {
    private final String SCRIPTS_DIR;
    private final String CONFIG_DIR;

    /**
     * Creates a new ConfigHandler
     * @param profilePath The path to the profile directory
     */
    public ConfigHandler(String profilePath) {
        SCRIPTS_DIR = profilePath;
        CONFIG_DIR = profilePath + "config/";
    }

    /**
     * Validates the configuration files integrity
     */
    public void validateConfig() {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
        }
        catch(Exception ignored) {}

        try {
            File dir = new File(CONFIG_DIR);
            File[] directoryListing = dir.listFiles();
            if(directoryListing != null) {
                for(File cfg : directoryListing) {
                    if(cfg.getName().endsWith(".cfg")) {
                        File f = new File(SCRIPTS_DIR + cfg.getName().replace(".cfg", ".script"));
                        if (!f.exists()) {
                            cfg.delete();
                            continue;
                        }
                        Properties properties = new Properties();
                        try (FileInputStream input = new FileInputStream(CONFIG_DIR + cfg.getName())) {
                            properties.load(input);

                            if(properties.size() < 3) {
                                addNewScript(cfg.getName().replace(".cfg", ""));
                            }
                        } catch (IOException ex) {
                            Logging.errorLog(ex);
                        }
                    }
                }
            }
        }
        catch(Exception ex) {
            Logging.errorLog(ex);
        }
    }

    /**
     * Adds a new script configuration file
     * @param script The name of the script
     */
    public void addNewScript(String script) {
        try {
            Properties properties = new Properties();
            properties.setProperty("minimized", "false");
            properties.setProperty("keycode", "0");
            properties.setProperty("mod", "0");
            try (FileOutputStream output = new FileOutputStream(CONFIG_DIR + script + ".cfg")) {
                properties.store(output, "Script Configuration");
            } catch (IOException ex) {
                Logging.errorLog(ex);
            }
        }
        catch (Exception ex) {
            Logging.errorLog(ex);
        }
    }

    /**
     * Sets a property in the configuration file
     * @param script The name of the script
     * @param property The property to set
     * @param value The value to set
     */
    public void set(String script, String property, String value) {
        try {
            Properties properties = new Properties();
            try (FileInputStream input = new FileInputStream(CONFIG_DIR + script + ".cfg")) {
                properties.load(input);
            }
            properties.setProperty(property, value);
            try (FileOutputStream output = new FileOutputStream(CONFIG_DIR + script + ".cfg")) {
                properties.store(output, "Script Configuration");
            }
        }
        catch (Exception ex) {
            Logging.errorLog(ex);
        }
    }

    public String get(String script, String property) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_DIR + script + ".cfg")) {
            properties.load(input);
            return properties.getProperty(property);
        } catch (IOException ex) {
            Logging.errorLog(ex);
        }
        return "0";
    }

    /**
     * Sets the minimized state of the script
     * @param script The name of the script
     * @param minimized The minimized state
     */
    public void setMinimized(String script, boolean minimized) {
        set(script, "minimized", minimized ? "true" : "false");
    }

    /**
     * Gets the minimized state of the script
     * @param script The name of the script
     * @return The minimized state
     */
    public boolean getMinimized(String script) {
        return get(script, "minimized").equals("true");
    }

    /**
     * Sets the keybind for the script
     * @param script The name of the script
     * @param code The keycode
     * @param mod The modifier
     */
    public void setKeycode(String script, int code, int mod) {
        set(script, "keycode", String.valueOf(code));
        set(script, "mod", String.valueOf(mod));
    }

    /**
     * Gets the keybind for the script
     * @param script The name of the script
     * @return The keybind
     */
    public Keybind getKeybind(String script) {
        try
        {
            int keycode = Integer.parseInt(get(script, "keycode"));
            int mod = Integer.parseInt(get(script, "mod"));
            return new Keybind(keycode, mod);
        }
        catch (Exception ex) {
            Logging.errorLog(ex);
        }
        return Keybind.NOT_SET;
    }
}
