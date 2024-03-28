package net.runelite.client.plugins.tscripts.ui;

import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.lexer.Lexer;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Tokenizer;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimerTask;

/**
 * Script Editor
 */
class ScriptEditor extends JFrame implements ActionListener {
    private final RSyntaxTextArea textArea;
    private final JFrame frame;
    private final JButton run = new JButton("Run Script");
    private final JLabel running = new JLabel();
    private final ScriptPanel spanel;
    private boolean is_running = false;

    /**
     * Create a new script editor
     * @param plugin The plugin
     * @param script The script
     * @param name The name
     * @param spanel The script panel
     * @throws IOException If an I/O error occurs
     */
    public ScriptEditor(TScriptsPlugin plugin, String script, String name, ScriptPanel spanel) throws IOException {
        // Create a frame
        frame = new JFrame(name);
        this.spanel = spanel;

        // Text component
        textArea = new RSyntaxTextArea(50, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        textArea.setCodeFoldingEnabled(true);
        CompletionProvider provider = plugin.getBaseCompletion();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(200);
        ac.setAutoActivationEnabled(true);
        ac.setAutoCompleteSingleChoices(false);
        ac.setAutoCompleteEnabled(true);
        ac.install(textArea);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        frame.add(sp);
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException ex) { // Never happens
            Logging.errorLog(ex);
        }


        //set script
        textArea.setText(Files.readString(Paths.get(script)));

        // Create amenu for menu
        JMenu m1 = new JMenu("Script");

        // Create menu items
        JMenuItem mi3 = new JMenuItem("Save");

        // Add action listener
        mi3.addActionListener(this);
        m1.add(mi3);

        // Create amenu for menu
        JMenu m2 = new JMenu("Edit");

        // Create menu items
        JMenuItem mi4 = new JMenuItem("cut");
        JMenuItem mi5 = new JMenuItem("copy");
        JMenuItem mi6 = new JMenuItem("paste");

        // Add action listener
        mi4.addActionListener(this);
        mi5.addActionListener(this);
        mi6.addActionListener(this);

        m2.add(mi4);
        m2.add(mi5);
        m2.add(mi6);

        JMenuItem mc = new JMenuItem("close");

        mc.addActionListener(this);


        textArea.setCodeFoldingEnabled(true);
        frame.add(new RTextScrollPane(textArea));

        textArea.setAnimateBracketMatching(true);
        textArea.setAutoIndentEnabled(true);


        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {}

            public void removeUpdate(DocumentEvent e) {}

            @SneakyThrows
            public void changedUpdate(DocumentEvent e) {
                Writer fileWriter = new FileWriter(script, false);
                fileWriter.write(textArea.getText());
                fileWriter.close();
            }
        });
        //icons
        BufferedImage icon = ImageUtil.loadImageResource(TScriptsPlugin.class, "Test_icon.png");
        //The menu
        run.addActionListener(this);
        JCheckBox alwaysOnTop = new JCheckBox("Always on top  ");
        JButton button = new JButton("CFG Visualizer");
        button.setToolTipText("Control-Flow Graph");
        button.addActionListener(e -> {
            new Thread(() -> {
                try
                {
                    Lexer lexer = new Lexer();
                    lexer.setVerify(false);
                    Scope scope = lexer.parse(Tokenizer.parse(textArea.getText()));
                    String json = scope.toJson();
                    CFGVisualizer.create(json);
                }
                catch (Exception ex)
                {
                    Logging.errorLog(ex);
                }
            }).start();
        });
        alwaysOnTop.addActionListener(this);
        //Add them
        JMenuBar menu = new JMenuBar();
        menu.add(run);
        menu.add(running);
        menu.add(Box.createHorizontalGlue());
        menu.add(button);
        menu.add(alwaysOnTop);
        //Apply the menu
        frame.setJMenuBar(menu);

        frame.setIconImage(icon);
        frame.setSize(550, 300);
        frame.setVisible(true);
        alwaysOnTop.setSelected(true);
        frame.setAlwaysOnTop(true);

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isRunning();
            }
        }, 0, 600);
    }

    /**
     * Check if the script is running
     */
    public void isRunning() {
        if(spanel.running && !is_running) {
            ImageIcon running_icon = new ImageIcon(ImageUtil.loadImageResource(TScriptsPlugin.class, "running.gif"));
            running.setIcon(running_icon);
            frame.getContentPane().repaint();
            this.is_running = true;
            this.run.setText("Stop Script");
        }
        else if(!spanel.running && is_running) {
            running.setIcon(null);
            frame.getContentPane().repaint();
            this.is_running = false;
            this.run.setText("Run Script");
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
                spanel.start();
                break;
            case "Stop Script":
                spanel.stop();
                break;
            case "Always on top  ":
                JCheckBox cb = (JCheckBox) e.getSource();
                this.frame.setAlwaysOnTop(cb.isSelected());
                break;
            case "close":
                frame.setVisible(false);
                break;
        }
    }
}