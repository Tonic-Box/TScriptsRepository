package net.runelite.client.plugins.tscripts.ui;
import lombok.SneakyThrows;
import static net.runelite.client.plugins.tscripts.TScriptsPlugin.*;
import net.runelite.client.plugins.tscripts.TScriptsConfig;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.util.CompletionSupplier;
import net.runelite.client.plugins.tscripts.util.ConfigHandler;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * The TScriptsPanel class contains the UI for the TScripts plugin side panel.
 */
public class TScriptsPanel extends PluginPanel
{
    private ImageIcon ADD_ICON;
    private ImageIcon ADD_HOVER_ICON;
    private final List<ScriptPanel> scriptPanels = new ArrayList<>();
    private TScriptsConfig config;
    private TScriptsPlugin plugin;
    public JCheckBox logActions = new JCheckBox("Menu Actions");
    public JCheckBox logPackets = new JCheckBox("Packets");
    public ToggleSwitch hotkeys = new ToggleSwitch();
    public ToggleSwitch copyMenu = new ToggleSwitch();
    private final Border blackline = BorderFactory.createLineBorder(Color.black);
    public String editName = "";
    public String current_profile = "[Default]";
    private java.util.Timer timer = null;

    /**
     * Initializes the TScriptsPanel
     * @param config The TScriptsConfig
     * @param plugin The TScriptsPlugin
     */
    public void init(TScriptsConfig config, TScriptsPlugin plugin)
    {
        try {
            this.plugin = plugin;
            this.config = config;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
            titlePanel.setBorder(new CompoundBorder(blackline, new EmptyBorder(10, 8, 8, 8)));
            BufferedImage addIcon = ImageUtil.loadImageResource(TScriptsPlugin.class, "add_icon.png");
            ADD_ICON = new ImageIcon(addIcon);
            ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53F));
            final JLabel addScript = new JLabel(ADD_ICON);
            addScript.setBorder(new EmptyBorder(0, 0, 8, 0));
            JLabel title = new JLabel();
            title.setForeground(Color.LIGHT_GRAY);
            title.setText("TScripts Panel");
            title.setFont(new Font("Impact", Font.BOLD, 20));
            title.setBorder(new EmptyBorder(0, 0, 8, 0));
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(0, 0));
            panel.setBorder(new EmptyBorder(1, 10, 1, 10));

            JPanel loggingPanel = new JPanel(new BorderLayout());
            loggingPanel.setBorder(new EmptyBorder(8, 0, 3, 0));
            logActions.setSelected(config.melog());
            logActions.addActionListener(e -> config.setMelog(logActions.isSelected()));
            logActions.setToolTipText("Log Menu Actions");

            logPackets.setSelected(config.packetLogger());
            logPackets.addActionListener(e -> config.setPacketLogger(logPackets.isSelected()));
            logPackets.setToolTipText("Log Packets");

            JLabel logging_title = new JLabel();
            logging_title.setForeground(Color.LIGHT_GRAY);
            logging_title.setText("Logging Options");
            logging_title.setFont(new Font("Impact", Font.PLAIN, 16));

            JPanel panel4 = new JPanel();
            panel4.setLayout(new BorderLayout(2, 0));
            panel4.setBorder(new EmptyBorder(5, 10, 1, 10));

            panel4.add(logging_title, BorderLayout.WEST);

            loggingPanel.add(panel4, BorderLayout.NORTH);
            //loggingPanel.add(logPackets, BorderLayout.WEST);
            loggingPanel.add(logActions, BorderLayout.WEST);
            loggingPanel.add(logPackets, BorderLayout.EAST);
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            topPanel.add(title, BorderLayout.WEST);
            topPanel.add(addScript, BorderLayout.EAST);
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

            JPanel hotkeyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,0));
            hotkeyPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            JLabel hktext = new JLabel("Hotkeys");
            hktext.setBorder(new EmptyBorder(0, 5, 0, 8));
            hotkeys.setBorder(new EmptyBorder(1, 10, 1, 10));
            hotkeys.setActivated(config.keybindsEnabled());
            hotkeyPanel.add(hotkeys);
            hotkeyPanel.add(hktext);

            JPanel copyMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,0));
            copyMenuPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            JLabel cmtext = new JLabel("Ext Menus");
            cmtext.setBorder(new EmptyBorder(0, 5, 0, 8));
            copyMenu.setBorder(new EmptyBorder(1, 10, 1, 10));
            copyMenu.setActivated(config.copyMenus());
            copyMenuPanel.add(copyMenu);
            copyMenuPanel.add(cmtext);

            JPanel renderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,0));
            renderPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

            bottomPanel.add(hotkeyPanel, BorderLayout.WEST);
            bottomPanel.add(copyMenuPanel, BorderLayout.EAST);

            southPanel.add(bottomPanel, BorderLayout.NORTH);

            JPanel xPanel = new JPanel(new BorderLayout());
            xPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

            xPanel.add(southPanel, BorderLayout.NORTH);
            xPanel.add(renderPanel, BorderLayout.SOUTH);

            JComboBox<?> profile = getjComboBox();
            JLabel pftext = new JLabel("Profiles");
            pftext.setFont(new Font("Impact", Font.PLAIN, 16));
            final JLabel addProfile = new JLabel(ADD_ICON);
            JPanel panel2= new JPanel();
            panel2.setLayout(new BorderLayout(2, 0));
            panel2.setBorder(new EmptyBorder(5, 10, 1, 10));

            File f = new File(plugin.getProfilePath(current_profile));
            if(f.exists()) {
                profile.setSelectedItem(current_profile);
            }

            profile.addActionListener(e -> {
                String s = (String) profile.getSelectedItem();
                if(s != null && !current_profile.equals(s)) {
                    current_profile = s;
                    plugin.profile = s;
                    HOME_DIR = plugin.getProfilePath(s);
                    plugin.configHandler = new ConfigHandler(plugin.getProfilePath(current_profile));
                    rebuild();
                }
            });

            addProfile.addMouseListener(new MouseAdapter() {
                @SneakyThrows
                public void mousePressed(MouseEvent mouseEvent) {
                    String name = JOptionPane.showInputDialog("Choose a profile name:");
                    if (name == null)
                        return;
                    //new script
                    name = name.replace(".", "");
                    name = name.replace(" ", "_");
                    File f = new File(plugin.getProfilePath(name));
                    if(name.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "A profile can't have a blank name.");
                    }
                    else if(!f.exists()) {
                        if(f.mkdir()) {
                            //plugin.configHandler.addNewScript(name);
                            System.out.println("[TScripts] new profile `" + name + "` created");
                            current_profile = name;
                            plugin.profile = name;
                            plugin.configHandler = new ConfigHandler(plugin.getProfilePath(current_profile));
                            rebuild();
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "There was an error while creating the new profile.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "A profile with that name already exists");
                    }
                }

                public void mouseEntered(MouseEvent mouseEvent) {
                    addProfile.setIcon(ADD_HOVER_ICON);
                }

                public void mouseExited(MouseEvent mouseEvent) {
                    addProfile.setIcon(ADD_ICON);
                }
            });

            panel2.add(pftext, BorderLayout.NORTH);
            panel2.add(profile, BorderLayout.CENTER);
            panel2.add(addProfile, BorderLayout.EAST);
            if(!current_profile.equals("[Default]")) {
                JButton delete_profile = new JButton("Delete Profile");
                panel2.add(delete_profile, BorderLayout.SOUTH);
                delete_profile.addMouseListener(new MouseAdapter() {
                    @SneakyThrows
                    public void mousePressed(MouseEvent mouseEvent) {
                        if(current_profile.equals("[Default]"))
                            return;
                        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to permanently delete this profile?", "Warning", JOptionPane.OK_CANCEL_OPTION);
                        if (confirm != 0)
                            return;
                        File f = new File(plugin.getProfilePath(current_profile));
                        if(f.exists()) {
                            deleteDirectory(f);
                            current_profile = "[Default]";
                            plugin.profile = "[Default]";
                            plugin.configHandler = new ConfigHandler(plugin.getProfilePath(current_profile));
                            rebuild();
                        }
                    }

                    public void mouseEntered(MouseEvent mouseEvent) {
                        addScript.setIcon(ADD_HOVER_ICON);
                    }

                    public void mouseExited(MouseEvent mouseEvent) {
                        addScript.setIcon(ADD_ICON);
                    }
                });
            }

            JLabel misc_title = new JLabel();
            misc_title.setForeground(Color.LIGHT_GRAY);
            misc_title.setText("Misc Options");
            misc_title.setFont(new Font("Impact", Font.PLAIN, 16));
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BorderLayout(2, 0));
            panel3.setBorder(new EmptyBorder(5, 10, 1, 10));

            panel3.add(misc_title, BorderLayout.WEST);

            titlePanel.add(topPanel);
            titlePanel.add(panel);
            titlePanel.add(loggingPanel);
            titlePanel.add(panel3);
            titlePanel.add(xPanel);
            titlePanel.add(panel2);
            addScript.setToolTipText("Add new script");
            addScript.addMouseListener(new MouseAdapter() {
                @SneakyThrows
                public void mousePressed(MouseEvent mouseEvent) {
                    String name = JOptionPane.showInputDialog("Choose a script name:");
                    if (name == null)
                        return;
                    //new script
                    name = name.replace(".", "");
                    name = name.replace(" ", "_");
                    File f = new File(plugin.getScriptPath(name, current_profile));
                    if(name.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "A script can't have a blank name.");
                    }
                    else if(!f.exists()) {
                        if(f.createNewFile()) {
                            plugin.configHandler.addNewScript(name);
                            System.out.println("[TScripts] new script `" + name + "` created");
                            rebuild();
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "There was an error while creating the new script.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "A script with that name already exists");
                    }
                }

                public void mouseEntered(MouseEvent mouseEvent) {
                    addScript.setIcon(ADD_HOVER_ICON);
                }

                public void mouseExited(MouseEvent mouseEvent) {
                    addScript.setIcon(ADD_ICON);
                }
            });
            JPanel scriptsView = new JPanel(new GridBagLayout());
            scriptsView.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            scriptsView.setBorder(new EmptyBorder(10, 0, 0, 0));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = 2;
            constraints.weightx = 1.0D;
            constraints.gridx = 0;
            constraints.gridy = 0;
            scriptPanels.clear();
            plugin.configHandler.validateConfig();
            plugin.unregAllKeyListeners();
            File dir = new File(plugin.getProfilePath(current_profile));
            File[] directoryListing = dir.listFiles();
            assert directoryListing != null;
            for (File script : directoryListing) {
                if(script.getName().toLowerCase().contains(".script")) {
                    String s = script.getName().split("\\.")[0];
                    ScriptPanel scriptPanel;
                    if(s.equals(editName)) {
                        scriptPanel = new ScriptPanel(this.plugin, this.config, s, this, plugin.configHandler.getMinimized(s), true, current_profile);
                        this.editName = "";
                    }
                    else {
                        scriptPanel = new ScriptPanel(this.plugin, this.config, s, this, plugin.configHandler.getMinimized(s), false, current_profile);
                    }

                    this.scriptPanels.add(scriptPanel);
                    scriptsView.add(scriptPanel, constraints);
                    constraints.gridy++;
                    scriptsView.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
                    constraints.gridy++;
                }
            }
            JButton docs = new JButton("Documentation");
            docs.addMouseListener(new MouseAdapter() {
                @SneakyThrows
                public void mousePressed(MouseEvent mouseEvent) {
                    try {
                        // Create a temporary file
                        File tempFile = File.createTempFile("tempFile", ".txt");
                        tempFile.deleteOnExit(); // Delete the file when the program exits

                        // Write some text to the temporary file
                        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                        writer.write(CompletionSupplier.genDocs(plugin.getRuntime().getMethodManager()));
                        writer.close();

                        // Open the temporary file in Notepad
                        Runtime.getRuntime().exec("notepad " + tempFile.getAbsolutePath());
                    } catch (IOException ex) {
                        Logging.errorLog(ex);
                    }
                }
            });
            add(titlePanel, BorderLayout.NORTH);
            add(scriptsView, BorderLayout.CENTER);
            add(docs, BorderLayout.SOUTH);
        }

        catch(Exception ex) {
            Logging.errorLog(ex);
        }

        plugin.setListenersToggle(config.keybindsEnabled());

        if(this.timer == null)
        {
            timer = new java.util.Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkToggle();
                    updatePanels();
                }
            }, 0, 600);
        }
    }

    /**
     * Polls the script panels to see if they are running and update their ui accordingly
     */
    private void updatePanels()
    {
        for (ScriptPanel scriptPanel : this.scriptPanels) {
            scriptPanel.checkIfRunning();
        }
    }

    /**
     * Checks if the hotkeys toggle has been changed and updates the config and plugin accordingly
     */
    private void checkToggle()
    {
        if(hotkeys.isActivated() != config.keybindsEnabled()) {
            config.setkeybindsEnabled(hotkeys.isActivated());
            plugin.setListenersToggle(hotkeys.isActivated());
        }
    }

    /**
     * Returns a JComboBox with the profiles in the plugin's home directory
     * @return The JComboBox
     */
    @NotNull
    private JComboBox<?> getjComboBox()
    {
        File d = new File(START_DIR);
        File[] dirs = d.listFiles();
        java.util.List<String> profiles = new ArrayList<>();
        profiles.add("[Default]");
        assert dirs != null;
        for (File p : dirs) {
            if(p.isDirectory()) {
                if(p.getName().endsWith(".profile"))
                    profiles.add(p.getName().replace(".profile", ""));
            }
        }


        return new JComboBox<>(profiles.toArray());
    }

    /**
     * Rebuilds the TScriptsPanel
     */
    public void rebuild()
    {
        removeAll();
        repaint();
        revalidate();
        init(this.config, this.plugin);
    }

    /**
     * Deletes a directory and all of its contents
     * @param directoryToBeDeleted The directory to be deleted
     */
    private void deleteDirectory(File directoryToBeDeleted)
    {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
