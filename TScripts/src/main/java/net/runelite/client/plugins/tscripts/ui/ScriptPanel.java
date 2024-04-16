package net.runelite.client.plugins.tscripts.ui;

import lombok.Getter;
import lombok.SneakyThrows;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.tscripts.TScriptsConfig;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.adapter.Adapter;
import net.runelite.client.plugins.tscripts.ui.editor.ScriptEditor;
import net.runelite.client.plugins.tscripts.util.eventbus.events.ScriptStateChanged;
import net.runelite.client.plugins.tscripts.adapter.Scope.Scope;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

/**
 * The panel that holds the script name and the buttons to run, stop, edit, and delete the script
 */
public class ScriptPanel extends JPanel
{
    private final Border NAME_BOTTOM_BORDER = new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
            BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));
    private final ImageIcon RUN_ICON;

    private final ImageIcon RUN_DISABLED_ICON;

    private final ImageIcon STOP_ICON;

    private final ImageIcon STOP_DISABLED_ICON;

    private final ImageIcon EDIT_ICON;

    private final ImageIcon EDIT_HOVER_ICON;

    private final ImageIcon DELETE_ICON;

    private final ImageIcon DELETE_HOVER_ICON;

    private TScriptsPlugin plugin;

    private TScriptsConfig config;

    private TScriptsPanel panel;

    private String script;

    @Getter
    private final JLabel runLabel = new JLabel();

    private final JLabel stopLabel = new JLabel();

    private final JButton bindkey = new JButton("...");

    private final JLabel editLabel = new JLabel();

    private final JLabel deleteLabel = new JLabel();

    private final FlatTextField nameInput = new FlatTextField();

    private final JLabel save = new JLabel("Save");

    private final JLabel cancel = new JLabel("Cancel");

    private final JLabel rename = new JLabel("Rename");

    private final JLabel minimizeLabel = new JLabel("--");

    private boolean minimized;

    JPanel bottomContainer;

    private String profile;

    private final Border blackline = BorderFactory.createLineBorder(Color.black);

    {
        BufferedImage runImg =  ImageUtil.loadImageResource(TScriptsPlugin.class, "run_icon.png");
        BufferedImage runImgGrayscale = ImageUtil.grayscaleImage(runImg);
        BufferedImage runImgDisabled = ImageUtil.luminanceOffset(runImgGrayscale, -60);
        RUN_ICON = new ImageIcon(runImg);
        RUN_DISABLED_ICON = new ImageIcon(runImgDisabled);
        BufferedImage stopImg = ImageUtil.loadImageResource(TScriptsPlugin.class, "stop_icon.png");
        BufferedImage stopImgGrayscale = ImageUtil.grayscaleImage(stopImg);
        BufferedImage stopImgDisabled = ImageUtil.luminanceOffset(stopImgGrayscale, -60);
        STOP_ICON = new ImageIcon(stopImg);
        STOP_DISABLED_ICON = new ImageIcon(stopImgDisabled);
        BufferedImage editImg = ImageUtil.loadImageResource(TScriptsPlugin.class, "edit_icon.png");
        BufferedImage editImgHover = ImageUtil.luminanceOffset(editImg, -150);
        EDIT_ICON = new ImageIcon(editImg);
        EDIT_HOVER_ICON = new ImageIcon(editImgHover);
        BufferedImage deleteImg = ImageUtil.loadImageResource(TScriptsPlugin.class, "delete_icon.png");
        DELETE_ICON = new ImageIcon(deleteImg);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));
    }

    private Keybind hke = Keybind.NOT_SET;

    /**
     * Creates a new script panel
     *
     * @param plugin the plugin
     * @param config the config
     * @param script the script name
     * @param panel the panel
     * @param minimized whether the panel is minimized
     * @param edit whether the panel is in edit mode
     * @param profile the profile
     */
    public ScriptPanel(TScriptsPlugin plugin, TScriptsConfig config, String script, TScriptsPanel panel, boolean minimized, boolean edit, String profile) {
        init(plugin, config, script, panel, minimized, edit, profile);
    }

    /**
     * Creates a new script panel
     *
     * @param plugin the plugin
     * @param config the config
     * @param script the script name
     * @param panel the panel
     * @param minimized whether the panel is minimized
     * @param edit whether the panel is in edit mode
     */
    private void init(TScriptsPlugin plugin, TScriptsConfig config, String script, TScriptsPanel panel, boolean minimized, boolean edit, String profile) {
        this.profile = plugin.getProfilePath(profile);
        this.config = config;
        this.plugin = plugin;
        this.script = script;
        this.bindkey.setText(hke.toString());
        this.minimized = minimized;
        if(this.minimized) {
            this.minimizeLabel.setText("+");
        }
        addRemoveListener(false);
        addRemoveListener(true);
        plugin.keyManager.registerKeyListener(getKeyListener());
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(blackline, new EmptyBorder(0, 8, 0, 8)));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JPanel nameWrapper = new JPanel(new BorderLayout());
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        nameWrapper.setBorder(NAME_BOTTOM_BORDER);
        JPanel nameActions = new JPanel(new BorderLayout(3, 0));
        nameActions.setBorder(new EmptyBorder(0, 0, 0, 8));
        nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.save.setFont(FontManager.getRunescapeSmallFont());
        this.save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        this.save.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if(running())
                    return;
                String input = nameInput.getText();
                input = input.replace(".", "");
                input = input.replace(" ", "_");
                nameInput.setText(input);
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Script name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else {
                    String oldName = getScriptName();
                    File file = new File(plugin.getProfilePath(profile) + oldName + ".script");
                    File file2 = new File(plugin.getProfilePath(profile) + input + ".script");
                    if (file2.exists()) {
                        JOptionPane.showMessageDialog(null, "A script with that name already exists", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    file.renameTo(file2);
                    file = new File(plugin.getProfilePath(profile) + "config\\" + oldName + ".cfg");
                    file2 = new File(plugin.getProfilePath(profile) + "config\\" + input + ".cfg");
                    file.renameTo(file2);
                    updateScriptName(input);
                    System.out.println("[TScript] Script '" + oldName + "' has been renamed to '" + input + "'");
                    try {
                        net.runelite.client.plugins.tscripts.ui.editor.ScriptEditor.changeTo(profile, input, oldName);
                    } catch (IOException ex) {
                        Logging.errorLog(ex);
                    }
                    panel.rebuild();
                }
                nameInput.setEditable(false);
                updateUI(false);
                requestFocusInWindow();
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
            }

            public void mouseExited(MouseEvent mouseEvent) {
                save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
            }
        });
        this.cancel.setFont(FontManager.getRunescapeSmallFont());
        this.cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        this.cancel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                panel.rebuild();
                nameInput.setEditable(false);
                nameInput.setText(getScriptName());
                updateUI(false);
                requestFocusInWindow();
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
            }

            public void mouseExited(MouseEvent mouseEvent) {
                cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }
        });
        this.rename.setFont(FontManager.getRunescapeSmallFont());
        this.rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        this.rename.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if(running())
                    return;
                panel.editName = script.split("\\.")[0];
                panel.rebuild();
                nameInput.setEditable(true);
                updateUI(true);
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }

            public void mouseExited(MouseEvent mouseEvent) {
                rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });
        this.minimizeLabel.setFont(new Font("Impact", Font.BOLD, 15));
        this.minimizeLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                plugin.configHandler.setMinimized(getScriptName(), !minimized);
                panel.rebuild();
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                minimizeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }

            public void mouseExited(MouseEvent mouseEvent) {
                minimizeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            }
        });
        if(edit) {
            nameActions.add(this.save, "East");
            nameActions.add(this.cancel, "West");
        }
        else {
            nameActions.add(this.rename, "West");
            nameActions.add(this.minimizeLabel, "East");
        }

        this.nameInput.setText(script);
        this.nameInput.setBorder(null);
        this.nameInput.setEditable(edit);
        this.nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.nameInput.setPreferredSize(new Dimension(0, 24));
        this.nameInput.getTextField().setForeground(Color.WHITE);
        this.nameInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 0));
        nameWrapper.add((Component)this.nameInput, "Center");
        nameWrapper.add(nameActions, "East");
        bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBorder(new EmptyBorder(8, 0, 8, 0));
        bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.stopLabel.setIcon(STOP_ICON);
        this.stopLabel.setDisabledIcon(STOP_DISABLED_ICON);
        this.stopLabel.setToolTipText("Stop script");
        this.stopLabel.setEnabled(running());
        this.stopLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                stop();
            }
        });
        this.runLabel.setIcon(RUN_ICON);
        this.runLabel.setDisabledIcon(RUN_DISABLED_ICON);
        this.runLabel.setToolTipText("Run script");
        this.runLabel.setEnabled(!running());
        this.runLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                start();
            }
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.editLabel.setIcon(EDIT_ICON);
        this.editLabel.setToolTipText("Edit script");
        this.editLabel.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            public void mousePressed(MouseEvent mouseEvent) {
                ScriptEditor.get(plugin, profile, getScriptName());
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                editLabel.setIcon(EDIT_HOVER_ICON);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                editLabel.setIcon(EDIT_ICON);
            }
        });
        this.deleteLabel.setIcon(DELETE_ICON);
        this.deleteLabel.setToolTipText("Delete script");
        this.deleteLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to permanently delete this script?", "Warning", JOptionPane.OK_CANCEL_OPTION);
                if (confirm == 0) {
                    File myObj = new File(plugin.getScriptPath(getScriptName(), profile));
                    try {
                        myObj.delete();
                    }
                    catch (Exception ignored) {}
                    myObj = new File(TScriptsPlugin.HOME_DIR + "config\\" + getScriptName() + ".cfg");
                    try {
                        myObj.delete();
                    }
                    catch (Exception ignored) {}
                    addRemoveListener(false);
                    System.out.println("[TScripts] Script '" + getScriptName() + "' deleted");
                    panel.rebuild();
                }
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                deleteLabel.setIcon(DELETE_HOVER_ICON);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                deleteLabel.setIcon(DELETE_ICON);
            }
        });
        hke = plugin.configHandler.getKeybind(getScriptName());
        this.bindkey.setText(hke.toString());
        bindkey.setToolTipText("Set Keybind");
        this.bindkey.addActionListener(e -> {
            plugin.unregister(getKeyListener());
            hke = Keybind.NOT_SET;
            bindkey.setText(hke.toString());
            plugin.configHandler.setKeycode(getScriptName(), 0, 0);
            this.bindkey.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    plugin.unregister(getKeyListener());
                    hke = new Keybind(e);
                    bindkey.setText(hke.toString());
                    bindkey.removeKeyListener(this);
                    plugin.configHandler.setKeycode(getScriptName(), hke.getKeyCode(), hke.getModifiers());
                    plugin.register(getKeyListener());
                }
            });
        });

        leftActions.add(this.runLabel);
        leftActions.add(this.stopLabel);
        rightActions.add(this.editLabel);
        rightActions.add(this.deleteLabel);

        JPanel southActions = new JPanel(new BorderLayout());
        southActions.setBorder(new EmptyBorder(0, 0, 0, 0));
        southActions.add(bindkey, BorderLayout.CENTER);
        southActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        bottomContainer.add(leftActions, "West");
        bottomContainer.add(rightActions, "East");
        bottomContainer.add(southActions, BorderLayout.SOUTH);

        //Context menu
        final JPanel layoutPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(boxLayout);
        add(layoutPanel, BorderLayout.NORTH);

        // Create reset all per hour menu
        final JMenuItem runScript = new JMenuItem("Run Script");

        // Create pause all menu
        final JMenuItem stopScript = new JMenuItem("Stop Script");


        // Create popup menu
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        //popupMenu.add(addToCanvas);
        popupMenu.add(runScript);
        popupMenu.add(stopScript);
        setComponentPopupMenu(popupMenu);

        //finish up
        add(nameWrapper, "North");
        if(!this.minimized) {
            add(bottomContainer, BorderLayout.SOUTH);
        }
        if (edit) {
            this.nameInput.getTextField().requestFocusInWindow();
            this.nameInput.getTextField().selectAll();
        }
    }

    /**
     * Checks if the script is running and updates the ui accordingly
     */
    public void checkIfRunning(ScriptStateChanged event) {
        if(this.getScriptName().equals(event.getScriptName()) && event.getRunning()) {
            runLabel.setEnabled(false);
            stopLabel.setEnabled(true);
        }
        else if(stopLabel.isEnabled())
        {
            runLabel.setEnabled(true);
            stopLabel.setEnabled(false);
        }
    }

    /**
     * Gets the script name
     *
     * @return the script name
     */
    public String getScriptName() {
        return this.script;
    }

    /**
     * Adds or removes the key listener
     *
     * @param add whether to add or remove the listener
     */
    private void addRemoveListener(boolean add) {
        //true = add;
        if(add) {
            plugin.hotKeyListeners.put(script, new HotkeyListener(() -> hke)
            {
                @Override
                public void hotkeyPressed() {
                    start();
                }
            });
        }
        else {
            plugin.hotKeyListeners.remove(script);
        }
    }

    /**
     * Gets the key listener
     *
     * @return the key listener
     */
    private net.runelite.client.input.KeyListener getKeyListener() {
        return plugin.hotKeyListeners.get(script);
    }

    /**
     * Updates the ui to reflect changes
     *
     * @param saveAndCancel whether to save and cancel
     */
    private void updateUI(boolean saveAndCancel) {
        removeAll();
        repaint();
        revalidate();
        init(this.plugin, this.config, this.script, this.panel, this.minimized, saveAndCancel, profile);
    }

    /**
     * Updates the script name
     *
     * @param name the new name
     */
    public void updateScriptName(String name) {
        this.script = name;
    }

    /**
     * Stops the script
     */
    public void stop()
    {
        //runLabel.setEnabled(true);
        //stopLabel.setEnabled(false);
        plugin.stopScript(getScriptName());
    }

    /**
     * Starts the script
     */
    public void start()
    {
        try {
            if(!plugin.canIRun())
                return;
            String path = profile + getScriptName() + ".script";
            String code = Files.readString(Paths.get(path));
            Scope scope = Adapter.parse(code);
            plugin.getRuntime().execute(scope, getScriptName(), plugin.getProfile());
        } catch (Exception ex) {
            Logging.errorLog(ex);
        }
    }

    /**
     * Checks if the script is running
     *
     * @return whether the script is running
     */
    public boolean running()
    {
        return plugin.amIRunning(getScriptName());
    }
}
