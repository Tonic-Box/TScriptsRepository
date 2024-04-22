package net.runelite.client.plugins.tscripts.ui.editor;

import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.adapter.Adapter;
import net.runelite.client.plugins.tscripts.sevices.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.sevices.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.BreakpointTripped;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.BreakpointUnTripped;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.ScriptStateChanged;
import net.runelite.client.plugins.tscripts.adapter.models.Scope.Scope;
import net.runelite.client.plugins.tscripts.types.BreakPoint;
import net.runelite.client.plugins.tscripts.ui.editor.debug.DebugToolPanel;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.util.ImageUtil;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Script Editor
 */
public class ScriptEditor extends JFrame implements ActionListener {
    private static ScriptEditor instance = null;
    private final ExRSyntaxTextArea textArea;
    private final JTextPane consoleArea;
    private final JScrollPane consoleScrollPane;
    private final JSplitPane verticalSplitPane;
    private final JButton run;
    private final JButton breakpoint;
    private final JLabel running = new JLabel();
    private final TScriptsPlugin plugin;
    private final DebugToolPanel debugToolPanel;
    private final JSplitPane splitPane; // Add a split pane
    private final JList<String> scriptList;
    private final DefaultListModel<String> scriptListModel = new DefaultListModel<>();
    private int runningIndex = -1;
    private String name = null;
    private String profile = null;
    private boolean updatingList = false;
    private final JMenuBar menu = new JMenuBar();

    public static ScriptEditor get()
    {
        return instance;
    }

    public static ScriptEditor get(TScriptsPlugin plugin, String profile, String name) throws IOException {
        if (instance == null) {
            instance = new ScriptEditor(plugin, profile, name);
        }
        else
        {
            if(!Objects.equals(profile, instance.profile))
                instance.profile = profile;

            instance.changeScript(name);
        }

        instance.setVisible(true);
        return instance;
    }

    public static void changeTo(String profile, String name, String oldName) throws IOException {
        if (instance == null || !instance.profile.equals(profile))
            return;

        if(!instance.name.equals(oldName))
        {
            instance.updateScriptList();
            return;
        }

        instance.changeScript(name);
    }

    /**
     * Create a new script editor
     * @param plugin The plugin
     * @param profile The profile
     * @param name The name
     * @throws IOException If an I/O error occurs
     */
    private ScriptEditor(TScriptsPlugin plugin, String profile, String name) throws IOException {
        super(name);
        generateFrame();
        this.plugin = plugin;
        this.name = name;
        this.profile = profile;
        String path = plugin.getScriptPath(name, profile);
        this.textArea = generateTextArea(path);
        this.scriptList = new JList<>(scriptListModel);
        this.scriptList.setCellRenderer(new CustomListCellRenderer());
        CompletionProvider provider = plugin.getBaseCompletion();
        generateAutoCompletion(provider).install(textArea);
        this.run = generateButton("Run Script");
        if(plugin.getRuntime().getScriptName().equals(name) && !plugin.getRuntime().isDone())
        {
            ImageIcon running_icon = new ImageIcon(ImageUtil.loadImageResource(TScriptsPlugin.class, "running.gif"));
            running.setIcon(running_icon);
            this.run.setText("Stop Script [" + plugin.getRuntime().getProfile() + "::" + plugin.getRuntime().getScriptName() + "]");
        }
        this.breakpoint = generateButton("Untrip Breakpoint");
        this.breakpoint.setForeground(Color.RED);
        this.breakpoint.setVisible(false);
        generateMenu();
        debugToolPanel = new DebugToolPanel(plugin.getRuntime(), Paths.get(path), name);
        debugToolPanel.setPreferredSize(new Dimension(600, getHeight()));
        splitPane = generateSplitPane();
        consoleArea = generateConsole();
        consoleScrollPane = generateConsoleScrollPane();
        verticalSplitPane = generateVerticalJSplitPane();
        add(verticalSplitPane, BorderLayout.CENTER);
        TEventBus.register(this);
    }

    private void updateScriptList() {
        updatingList = true;
        runningIndex = -1;
        scriptListModel.clear();
        File dir = new File(plugin.getProfilePath(profile));
        File[] directoryListing = dir.listFiles();
        assert directoryListing != null;
        String selected = "";
        int i = 0;
        for (File script : directoryListing) {
            if (script.getName().toLowerCase().endsWith(".script")) {
                String s = script.getName().split("\\.")[0];
                if (s.equals(name)) { // Assuming 'name' is the variable holding the name of the currently editing script
                    selected = s;
                }

                if (plugin.getRuntime().getScriptName().equals(s) && plugin.getRuntime().getProfile().equals(profile) && !plugin.getRuntime().isDone()) {
                    runningIndex = i;
                }
                scriptListModel.addElement(s);
                i++;
            }
        }
        scriptList.setSelectedValue(selected, true); // Select the matching entry and scroll to it
        updatingList = false;
    }

    private void changeScript(String name) throws IOException {
        this.name = name;
        setTitle("[" + profile + "] " + name);
        String path = plugin.getScriptPath(name, profile);
        Path scriptPath = Paths.get(path);
        debugToolPanel.update(scriptPath, name);
        textArea.setScript(path);
        updateScriptList();
    }

    private void toggleDebugPanel() {
        boolean isVisible = debugToolPanel.isVisible();
        if (isVisible) {
            debugToolPanel.setVisible(false);
            setSize(getWidth() - debugToolPanel.getPreferredSize().width, getHeight());
            splitPane.setDividerLocation(getWidth());
        } else {
            debugToolPanel.setVisible(true);
            setSize(getWidth() + debugToolPanel.getPreferredSize().width, getHeight());
            splitPane.setDividerLocation(getWidth() - debugToolPanel.getPreferredSize().width);
        }
    }

    /**
     * Handle an action
     * @param e The action
     */
    @SneakyThrows
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();

        switch (s) {
            case "Run Script":
                start();
                return;
            case "Always on top  ":
                JCheckBox cb = (JCheckBox) e.getSource();
                setAlwaysOnTop(cb.isSelected());
                return;
            case "close":
                setVisible(false);
                return;
            case "Dev Tools":
                toggleDebugPanel();
                return;
            case "Untrip Breakpoint":
                TEventBus.post(BreakpointUnTripped.get());
                breakpoint.setVisible(false);
                return;
        }

        if(s.startsWith("Stop Script ["))
        {
            stop();
        }
    }

    public void start()
    {
        try {
            if(!plugin.canIRun())
                return;
            Path path = Paths.get(plugin.getScriptPath(name, profile));
            String code = Files.readString(path);
            int offset = 0;
            for(BreakPoint breakPoint : textArea.getBreakpoints().values()) {
                code = insertTextAtOffset(code, "breakpoint();", breakPoint.getOffset() + offset);
                offset += 13;
            }

            Scope scope = Adapter.parse(code);
            plugin.getRuntime().execute(scope, name, profile);
        } catch (Exception ex) {
            Logging.errorLog(ex);
        }
    }

    public String insertTextAtOffset(String original, String textToInsert, int offset) {
        String beforeOffset = original.substring(0, offset);
        String afterOffset = original.substring(offset);
        return beforeOffset + textToInsert + afterOffset;
    }

    public void stop()
    {
        plugin.stopScript();
    }

    @_Subscribe
    public void onScriptStateChanged(ScriptStateChanged event)
    {
        if(event.getRunning())
        {
            ImageIcon running_icon = new ImageIcon(ImageUtil.loadImageResource(TScriptsPlugin.class, "running.gif"));
            running.setIcon(running_icon);
            getContentPane().repaint();
            this.run.setText("Stop Script [" + event.getProfile() + "::" + event.getScriptName() + "]");
        }
        else
        {
            running.setIcon(null);
            getContentPane().repaint();
            breakpoint.setVisible(false);
            this.run.setText("Run Script");
        }
        updateScriptList();
    }

    @_Subscribe
    public void onBreakpointTripped(BreakpointTripped event)
    {
        breakpoint.setVisible(true);
    }

    //*********** COMPONENT GENERATION ***********//

    private JButton generateButton(String name)
    {
        JButton button = new JButton(name);
        button.setToolTipText(name);
        button.addActionListener(this);
        return button;
    }

    private void generateFrame()
    {
        BufferedImage icon = ImageUtil.loadImageResource(TScriptsPlugin.class, "Test_icon.png");
        setIconImage(icon);
        setSize(650, 340);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    private ExRSyntaxTextArea generateTextArea(String script) throws IOException {
        ExRSyntaxTextArea textArea = new ExRSyntaxTextArea(50, 60);
        textArea.setScript(script);
        return textArea;
    }

    private AutoCompletion generateAutoCompletion(CompletionProvider provider)
    {
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(200);
        ac.setAutoActivationEnabled(true);
        ac.setAutoCompleteSingleChoices(false);
        ac.setAutoCompleteEnabled(true);
        return ac;
    }

    private void generateMenu()
    {
        //components
        JCheckBox alwaysOnTop = new JCheckBox("Always on top  ");
        alwaysOnTop.setSelected(true);
        alwaysOnTop.addActionListener(this);
        JButton devTools = generateButton("Dev Tools");
        JButton toggleConsoleItem = new JButton("Console");
        toggleConsoleItem.addActionListener(e -> toggleConsole());
        menu.add(toggleConsoleItem);

        //menu
        menu.add(this.run);
        menu.add(this.running);
        menu.add(this.breakpoint);
        menu.add(Box.createHorizontalGlue());
        menu.add(toggleConsoleItem);
        menu.add(devTools);
        menu.add(alwaysOnTop);
        setJMenuBar(menu);
    }

    private void toggleConsole() {
        boolean isVisible = consoleScrollPane.isVisible();
        if (isVisible) {
            // If the console is currently visible, hide it and move the divider to the bottom
            consoleScrollPane.setVisible(false);
            if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                setSize(getWidth(), getHeight() - consoleScrollPane.getPreferredSize().height);
            }
            verticalSplitPane.setDividerLocation(verticalSplitPane.getHeight());
        } else {
            // If the console is currently hidden, show it and move the divider to show the console
            consoleScrollPane.setVisible(true);
            if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                setSize(getWidth(), getHeight() + consoleScrollPane.getPreferredSize().height);
            }
            verticalSplitPane.setDividerLocation(getHeight() - consoleScrollPane.getPreferredSize().height);
        }
    }

    public void logToConsole(String message, Color color) {
        StyledDocument doc = consoleArea.getStyledDocument();
        Style style = consoleArea.addStyle("Style", null);
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException ex) {
            Logging.errorLog(ex);
        }

        // Scroll to the bottom
        consoleArea.setCaretPosition(doc.getLength());
    }

    private JSplitPane generateSplitPane()
    {
        JScrollPane listScrollPane = new JScrollPane(scriptList);
        listScrollPane.setPreferredSize(new Dimension(100, getHeight()));
        updateScriptList();
        scriptList.addListSelectionListener(e -> {
            try
            {
                if (!e.getValueIsAdjusting() && !updatingList) {
                    String selectedScript = scriptList.getSelectedValue();
                    changeScript(selectedScript);
                }
            }
            catch (Exception ex)
            {
                Logging.errorLog(ex);
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setLineNumbersEnabled(true);
        sp.getGutter().setVisible(true);
        sp.setIconRowHeaderEnabled(true);
        sp.setPreferredSize(new Dimension(550, getHeight()));
        leftPanel.add(sp, BorderLayout.CENTER);
        leftPanel.add(listScrollPane, BorderLayout.WEST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, debugToolPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(getWidth());
        debugToolPanel.setVisible(false);
        return splitPane;
    }

    private JSplitPane generateVerticalJSplitPane() {
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, consoleScrollPane);
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane.setDividerLocation(getHeight() - 100);
        add(verticalSplitPane, BorderLayout.CENTER);
        return verticalSplitPane;
    }

    private JTextPane generateConsole()
    {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");

        return pane;
    }

    private JScrollPane generateConsoleScrollPane()
    {
        JScrollPane pane = new JScrollPane(consoleArea);
        pane.setPreferredSize(new Dimension(getWidth(), 150));
        pane.setVisible(false);
        return pane;
    }

    class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == runningIndex) {
                label.setForeground(Color.GREEN);
            }
            return label;
        }
    }
}