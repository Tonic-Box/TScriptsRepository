package net.runelite.client.plugins.tscripts.ui;

import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.util.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.util.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.util.eventbus.events.BreakpointTripped;
import net.runelite.client.plugins.tscripts.util.eventbus.events.BreakpointUnTripped;
import net.runelite.client.plugins.tscripts.util.eventbus.events.ScriptStateChanged;
import net.runelite.client.plugins.tscripts.lexer.Lexer;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Tokenizer;
import net.runelite.client.plugins.tscripts.types.BreakPoint;
import net.runelite.client.plugins.tscripts.ui.debug.DebugToolPanel;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.util.ImageUtil;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
import java.util.logging.Logger;

/**
 * Script Editor
 */
public class ScriptEditor extends JFrame implements ActionListener {
    private static ScriptEditor instance = null;
    private final RSyntaxTextArea textArea;
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
    private DocumentListener documentListener;
    private boolean updatingList = false;
    private final Map<Integer, BreakPoint> breakpoints = new HashMap<>();
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
        setTheme();
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
        textArea.getDocument().removeDocumentListener(documentListener);
        textArea.setText(Files.readString(scriptPath));
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
            for(BreakPoint breakPoint : breakpoints.values()) {
                code = insertTextAtOffset(code, "breakpoint();", breakPoint.getOffset() + offset);
                offset += 13;
            }

            var tokens = Tokenizer.parse(code);
            Scope scope = Lexer.lex(tokens);
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