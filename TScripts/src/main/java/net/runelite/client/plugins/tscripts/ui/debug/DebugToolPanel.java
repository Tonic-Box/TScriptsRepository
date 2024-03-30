package net.runelite.client.plugins.tscripts.ui.debug;

import lombok.Getter;
import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.lexer.Lexer;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Tokenizer;
import net.runelite.client.plugins.tscripts.runtime.Runtime;
import net.runelite.client.plugins.tscripts.util.Logging;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class DebugToolPanel extends JPanel {

    private final JPanel mainView;
    private final CardLayout cardLayout;
    private Path scriptPath;
    @Getter
    private final CFGVisualizer controlFlowGraphVisualizer;
    private final JList<String> toolingList = new JList<>(new String[]{"Control-Flow", "Variables", "Runtime"});

    public DebugToolPanel(Runtime runtime, Path scriptPath, String name) {
        setSize(800, 600);
        setLayout(new BorderLayout());
        this.scriptPath = scriptPath;

        // Create the button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        add(buttonPanel, BorderLayout.WEST);

        // Create the main view area with CardLayout
        mainView = new JPanel();
        cardLayout = new CardLayout();
        mainView.setLayout(cardLayout);
        add(mainView, BorderLayout.CENTER);

        // Add components to the main view
        controlFlowGraphVisualizer = CFGVisualizer.create(runtime, getScope().toJson(), name); // Placeholder for the actual visualizer
        mainView.add(controlFlowGraphVisualizer, "ControlFlowGraph");

        VariableInspector variableInspector = VariableInspector.getInstance(runtime); // Placeholder for the actual variable inspector
        mainView.add(variableInspector, "VariableInspector");

        RuntimeInspector runtimeInspector = RuntimeInspector.getInstance(runtime);
        mainView.add(runtimeInspector, "RuntimeInspector");

        toolingList.addListSelectionListener(e -> {
            String selectedScript = toolingList.getSelectedValue();
            switch (selectedScript) {
                case "Control-Flow":
                    controlFlowGraphVisualizer.updateGraph(getScope().toJson());
                    cardLayout.show(mainView, "ControlFlowGraph");
                    break;
                case "Variables":
                    cardLayout.show(mainView, "VariableInspector");
                    break;
                case "Runtime":
                    cardLayout.show(mainView, "RuntimeInspector");
                    RuntimeInspector.updateTelemetry();
                    break;
            }
        });

        buttonPanel.add(toolingList, BorderLayout.CENTER);
    }

    public void update(Path scriptPath, String name)
    {
        this.scriptPath = scriptPath;
        controlFlowGraphVisualizer.changeScript(name);
        controlFlowGraphVisualizer.updateGraph(getScope().toJson());
    }

    private Scope getScope()
    {
        try
        {
            var tokens = Tokenizer.parse(Files.readString(scriptPath));
            return Lexer.lex(tokens);
        }
        catch (Exception ex)
        {
            Logging.errorLog(ex);
        }
        return new Scope(new HashMap<>(),null);
    }
}