package net.runelite.client.plugins.tscripts.ui;

import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.eventbus.events.BreakpointTripped;
import net.runelite.client.plugins.tscripts.eventbus.events.BreakpointUnTripped;
import net.runelite.client.plugins.tscripts.eventbus.events.ScriptStateChanged;
import net.runelite.client.plugins.tscripts.lexer.Lexer;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Tokenizer;
import net.runelite.client.plugins.tscripts.types.BreakPoint;
import net.runelite.client.plugins.tscripts.ui.debug.DebugToolPanel;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.util.ImageUtil;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Script Editor
 */
class ScriptEditor extends JFrame implements ActionListener {
    private static ScriptEditor instance = null;
    private final RSyntaxTextArea textArea;
    private final JButton run;
    private final JButton breakpoint;
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
    private Map<Integer, BreakPoint> breakpoints = new HashMap<>();
    private final JMenuBar menu = new JMenuBar();

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
        if(plugin.getRuntime().getScriptName().equals(name))
        {
            ImageIcon running_icon = new ImageIcon(ImageUtil.loadImageResource(TScriptsPlugin.class, "running.gif"));
            running.setIcon(running_icon);
            this.run.setText("Stop Script");
        }
        this.breakpoint = generateButton("Untrip Breakpoint");
        this.breakpoint.setForeground(Color.RED);
        this.breakpoint.setVisible(false);
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
        clearBreakpoints();
        updateScriptList();
    }

    private DocumentListener createDocListener(String path)
    {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                clearBreakpoints();
            }

            public void removeUpdate(DocumentEvent e) {
                clearBreakpoints();
            }

            @SneakyThrows
            public void changedUpdate(DocumentEvent e) {
                Writer fileWriter = new FileWriter(path, false);
                fileWriter.write(textArea.getText());
                fileWriter.close();
            }
        };
    }

    private void clearBreakpoints()
    {
        Gutter gutter = ((RTextScrollPane) textArea.getParent().getParent()).getGutter();
        for (BreakPoint breakPoint : breakpoints.values()) {
            gutter.removeTrackingIcon(breakPoint.getIcon());
            textArea.removeLineHighlight(breakPoint.getTag());
        }
        breakpoints.clear();
        textArea.repaint();
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
            breakpoint.setVisible(false);
            this.run.setText("Run Script");
        }
    }

    @_Subscribe
    public void onBreakpointTripped(BreakpointTripped event)
    {
        breakpoint.setVisible(true);
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
            case "Untrip Breakpoint":
                TEventBus.post(BreakpointUnTripped.get());
                breakpoint.setVisible(false);
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
            for(BreakPoint breakPoint : breakpoints.values()) {
                code = insertTextAtOffset(code, "breakpoint();", breakPoint.getOffset());
            }
            var tokens = Tokenizer.parse(code);
            Scope scope = Lexer.lex(tokens);
            plugin.getRuntime().execute(scope, name);
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
        textArea.setHighlighter(new RSyntaxTextAreaHighlighter());
        this.documentListener = createDocListener(script);
        textArea.getDocument().addDocumentListener(documentListener);

        JMenuItem toggleBreakpointItem = new JMenuItem("Toggle Breakpoint");
        toggleBreakpointItem.addActionListener((ActionEvent e) -> {
            int line = textArea.getCaretLineNumber();
            int offset = getWordStartPosAtCaret(textArea);
            String word = getWordAtCaret(textArea);
            boolean isMethod = MethodManager.getInstance().getMethods().containsKey(word.toLowerCase());
            if(!isMethod)
                return;
            try {
                toggleBreakpoint(textArea, line, offset, word);
            } catch (BadLocationException ex) {
                Logging.errorLog(ex);
            }
            textArea.repaint();
        });

        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handleRightClick(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handleRightClick(e);
                }
            }

            private void handleRightClick(MouseEvent e) {
                // Move the caret to the mouse position only if no text is selected
                if (textArea.getSelectedText() == null) {
                    int offset = textArea.viewToModel2D(e.getPoint());
                    textArea.setCaretPosition(offset);
                }
                // Show the context menu (if you have one)
                // contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        JPopupMenu popupMenu = textArea.getPopupMenu();
        popupMenu.addSeparator();
        popupMenu.add(toggleBreakpointItem);

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
        menu.add(this.run);
        menu.add(this.running);
        menu.add(this.breakpoint);
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
        sp.setLineNumbersEnabled(true);
        sp.getGutter().setVisible(true);
        sp.setIconRowHeaderEnabled(true);
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

    private void toggleBreakpoint(RSyntaxTextArea textArea, int line, int offset, String word) throws BadLocationException {
        Gutter gutter = ((RTextScrollPane) textArea.getParent().getParent()).getGutter();

        if (breakpoints.containsKey(line)) {
            gutter.removeTrackingIcon(breakpoints.get(line).getIcon());
            textArea.removeLineHighlight(breakpoints.get(line).getTag());
            breakpoints.remove(line);
        } else {
            GutterIconInfo iconInfo = gutter.addLineTrackingIcon(line, new CircleIcon(Color.RED));
            Object tag = textArea.addLineHighlight(line, Color.DARK_GRAY);
            BreakPoint breakPoint = new BreakPoint(line, offset, word, iconInfo, tag);
            breakpoints.put(line, breakPoint);
        }
        textArea.repaint();
    }

    private String getWordAtCaret(RSyntaxTextArea textArea) {
        try {
            int caretPosition = textArea.getCaretPosition();
            String text = textArea.getText();

            // Find the start of the word
            int wordStart = caretPosition;
            while(wordStart > 0 && Character.isLetter(text.charAt(wordStart - 1))) {
                wordStart--;
            }

            // Find the end of the word
            int wordEnd = caretPosition;
            while (wordEnd < text.length() && Character.isLetter(text.charAt(wordEnd))) {
                wordEnd++;
            }

            // Extract and return the word
            return text.substring(wordStart, wordEnd);
        } catch (Exception ex) {
            Logging.errorLog(ex);
            return "";
        }
    }

    private int getWordStartPosAtCaret(RSyntaxTextArea textArea) {
        try {
            int caretPosition = textArea.getCaretPosition();
            String text = textArea.getText();

            // Find the start of the word
            int wordStart = caretPosition;
            while(wordStart > 0 && Character.isLetter(text.charAt(wordStart - 1))) {
                wordStart--;
            }
            return wordStart;
        } catch (Exception ex) {
            Logging.errorLog(ex);
            return 0;
        }
    }

    static class CircleIcon implements Icon {
        private final Color color;

        public CircleIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, getIconWidth(), getIconHeight());
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}