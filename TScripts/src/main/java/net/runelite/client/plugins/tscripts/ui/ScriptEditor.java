package net.runelite.client.plugins.tscripts.ui;

import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.eventbus.events.ScriptStateChanged;
import net.runelite.client.plugins.tscripts.lexer.Lexer;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Tokenizer;
import net.runelite.client.plugins.tscripts.ui.debug.DebugToolPanel;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.util.ImageUtil;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Script Editor
 */
class ScriptEditor extends JFrame implements ActionListener {
    private static ScriptEditor instance = null;
    private final RSyntaxTextArea textArea;
    private final JButton run;
    private final JLabel running = new JLabel();
    private final TScriptsPanel scriptPanel;
    private final TScriptsPlugin plugin;
    private DebugToolPanel debugToolPanel = null;
    private final JSplitPane splitPane; // Add a split pane
    private final JList<String> scriptList;
    private final DefaultListModel<String> scriptListModel = new DefaultListModel<>();
    private String name = null;
    private String profile = null;
    private DocumentListener documentListener;
    private boolean updatingList = false;

    public static ScriptEditor get(TScriptsPlugin plugin, String profile, String name, TScriptsPanel scriptPanel) throws IOException {
        if (instance == null) {
            instance = new ScriptEditor(plugin, profile, name, scriptPanel);
        }
        else
        {
            if(!Objects.equals(profile, instance.profile))
            {
                instance.profile = profile;
                instance.updateScriptList();
            }
            instance.changeScript(name);
        }

        instance.setVisible(true);
        return instance;
    }

    /**
     * Create a new script editor
     * @param plugin The plugin
     * @param profile The profile
     * @param name The name
     * @param scriptPanel The script panel
     * @throws IOException If an I/O error occurs
     */
    private ScriptEditor(TScriptsPlugin plugin, String profile, String name, TScriptsPanel scriptPanel) throws IOException {
        super(name);
        generateFrame();
        this.scriptPanel = scriptPanel;
        this.plugin = plugin;
        this.name = name;
        this.profile = profile;
        String path = plugin.getScriptPath(name, profile);
        this.textArea = generateTextArea(path);
        this.scriptList = new JList<>(scriptListModel);
        CompletionProvider provider = plugin.getBaseCompletion();
        generateAutoCompletion(provider).install(textArea);
        setTheme();
        this.run = generateButton("Run Script");
        generateMenu();
        debugToolPanel = new DebugToolPanel(plugin.getRuntime(), Paths.get(path), name);
        debugToolPanel.setPreferredSize(new Dimension(600, getHeight())); // Set the preferred width of the debug panel
        splitPane = generateSplitPane();
        TEventBus.register(this);
    }

    private void updateScriptList() {
        updatingList = true;
        scriptListModel.clear();
        File dir = new File(plugin.getProfilePath(profile));
        File[] directoryListing = dir.listFiles();
        assert directoryListing != null;
        String selected = "";
        for (File script : directoryListing) {
            if (script.getName().toLowerCase().contains(".script")) {
                String s = script.getName().split("\\.")[0];
                scriptListModel.addElement(s);
                if (s.equals(name)) { // Assuming 'name' is the variable holding the name of the currently editing script
                    selected = s;
                }
            }
        }
        scriptList.setSelectedValue(selected, true); // Select the matching entry and scroll to it
        updatingList = false;
    }

    private void changeScript(String name) throws IOException {
        this.name = name;
        setTitle(name);
        String path = plugin.getScriptPath(name, profile);
        debugToolPanel.update(Paths.get(path), name);
        textArea.getDocument().removeDocumentListener(documentListener);
        textArea.setText(Files.readString(Paths.get(path)));
        documentListener = createDocListener(path);
        textArea.getDocument().addDocumentListener(documentListener);
        updateScriptList();
    }

    private DocumentListener createDocListener(String path)
    {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {}

            public void removeUpdate(DocumentEvent e) {}

            @SneakyThrows
            public void changedUpdate(DocumentEvent e) {
                Writer fileWriter = new FileWriter(path, false);
                fileWriter.write(textArea.getText());
                fileWriter.close();
            }
        };
    }

    /**
     * Check if the script is running
     */
    @_Subscribe
    public void onScriptStateChanged(ScriptStateChanged event)
    {
        if(this.name.equals(event.getScriptName()) && event.getRunning())
        {
            ImageIcon running_icon = new ImageIcon(ImageUtil.loadImageResource(TScriptsPlugin.class, "running.gif"));
            running.setIcon(running_icon);
            getContentPane().repaint();
            this.run.setText("Stop Script");
        }
        else if(this.run.getText().equals("Stop Script"))
        {
            running.setIcon(null);
            getContentPane().repaint();
            this.run.setText("Run Script");
        }
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
                break;
            case "Stop Script":
                stop();
                break;
            case "Always on top  ":
                JCheckBox cb = (JCheckBox) e.getSource();
                setAlwaysOnTop(cb.isSelected());
                break;
            case "close":
                setVisible(false);
                break;
            case "Dev Tools":
                toggleDebugPanel();
                break;
        }
    }

    public void start()
    {
        try {
            if(!plugin.canIRun())
                return;
            Path path = Paths.get(plugin.getScriptPath(name, profile));
            String code = Files.readString(path);
            var tokens = Tokenizer.parse(code);
            Scope scope = Lexer.lex(tokens);
            plugin.getRuntime().execute(scope, name);
        } catch (Exception ex) {
            Logging.errorLog(ex);
        }
    }

    public void stop()
    {
        plugin.stopScript(name);
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

    private RSyntaxTextArea generateTextArea(String script) throws IOException {
        RSyntaxTextArea textArea = new RSyntaxTextArea(50, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        textArea.setCodeFoldingEnabled(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setText(Files.readString(Paths.get(script)));
        this.documentListener = createDocListener(script);
        textArea.getDocument().addDocumentListener(documentListener);
        return textArea;
    }

    private void setTheme()
    {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException ex) { // Never happens
            Logging.errorLog(ex);
        }
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

        //menu
        JMenuBar menu = new JMenuBar();
        menu.add(this.run);
        menu.add(this.running);
        menu.add(Box.createHorizontalGlue());
        menu.add(devTools);
        menu.add(alwaysOnTop);
        setJMenuBar(menu);
    }

    private JSplitPane generateSplitPane()
    {
        JScrollPane listScrollPane = new JScrollPane(scriptList);
        listScrollPane.setPreferredSize(new Dimension(100, getHeight()));
        updateScriptList();
        scriptList.addListSelectionListener(new ListSelectionListener() {
            @Override
            @SneakyThrows
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !updatingList) {
                    String selectedScript = scriptList.getSelectedValue();
                    changeScript(selectedScript);
                }
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setPreferredSize(new Dimension(550, getHeight()));
        leftPanel.add(sp, BorderLayout.CENTER);
        leftPanel.add(listScrollPane, BorderLayout.WEST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, debugToolPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(getWidth());
        add(splitPane, BorderLayout.CENTER);
        debugToolPanel.setVisible(false);
        return splitPane;
    }
}