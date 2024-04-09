package net.runelite.client.plugins.tscripts.ui.editor.debug;

import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.ui.editor.ExRSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;

public class DocumentationPanel extends JPanel
{
    private static DocumentationPanel instance;

    public static DocumentationPanel getInstance() {
        if (instance == null)
            instance = new DocumentationPanel();
        return instance;
    }

    private final JTree categoryTree;
    private final ExRSyntaxTextArea codeTextPane;

    private DocumentationPanel() {
        setLayout(new BorderLayout());

        categoryTree = createTree();
        JScrollPane treeScrollPane = new JScrollPane(categoryTree);
        treeScrollPane.setPreferredSize(new Dimension(150, getHeight()));

        // Create the documentation view panel
        JPanel documentationPanel = new JPanel();
        documentationPanel.setLayout(new BoxLayout(documentationPanel, BoxLayout.Y_AXIS));

        // Code text pane for syntax highlighting
        codeTextPane = new ExRSyntaxTextArea(50, 60);
        codeTextPane.setEditable(false);
        JScrollPane codeScrollPane = new JScrollPane(codeTextPane);
        codeScrollPane.setPreferredSize(new Dimension(300, 200));
        documentationPanel.add(codeScrollPane);

        JScrollPane documentationScrollPane = new JScrollPane(documentationPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, documentationScrollPane);
        splitPane.setDividerLocation(150); // You can adjust this value to set the initial divider location
        add(splitPane, BorderLayout.CENTER);
    }

    private JTree createTree() {
        // Create the tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categories");

        DefaultMutableTreeNode conditionalConstructs = new DefaultMutableTreeNode("Language Constructs");
        conditionalConstructs.add(new DefaultMutableTreeNode("if/else"));
        conditionalConstructs.add(new DefaultMutableTreeNode("while"));
        conditionalConstructs.add(new DefaultMutableTreeNode("continue"));
        conditionalConstructs.add(new DefaultMutableTreeNode("break"));
        conditionalConstructs.add(new DefaultMutableTreeNode("die"));
        conditionalConstructs.add(new DefaultMutableTreeNode("function"));
        conditionalConstructs.add(new DefaultMutableTreeNode("variables"));
        root.add(conditionalConstructs);

        DefaultMutableTreeNode events = new DefaultMutableTreeNode("Event Subscribers");
        events.add(new DefaultMutableTreeNode("subscribe"));
        root.add(events);

        MethodManager methodManager = MethodManager.getInstance();
        List<GroupDefinition> groups = methodManager.getMethodClasses();
        for (GroupDefinition groupDefinition : groups)
        {
            DefaultMutableTreeNode group = new DefaultMutableTreeNode(groupDefinition.groupName());
            List<MethodDefinition> methods = groupDefinition.methods(methodManager);
            for (MethodDefinition method : methods)
            {
                group.add(new ExTreeNode(method));
            }
            root.add(group);
        }

        JTree tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.isLeaf()) {
                if (selectedNode instanceof ExTreeNode) {
                    ExTreeNode exTreeNode = (ExTreeNode) selectedNode;
                    MethodDefinition methodDefinition = exTreeNode.getMethod();
                    updateDocumentation(methodDefinition);
                    return;
                }
                StringBuilder usage;
                switch (selectedNode.toString())
                {
                    case "if/else":
                        usage = new StringBuilder("/*\n * The condition can be any expression that evaluates to a boolean value\n *\n" +
                                "* The code block will only execute if the condition is true.\n * you can optionally follow if statements with an else { ... } block\n */\n" +
                                "if(condition) {\n\t//code\n}\n\n//Examples\nif(true) {\n\t//code\n}\n\n$var = 7;\nif($var <= 10) {\n\t//code\n}\n\n" +
                                "// Conditional Operators:\n" +
                                "// ==, !=, <, >, <=, >=, &&, ||, !");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "while":
                        usage = new StringBuilder("/*\n * The condition can be any expression that evaluates to a boolean value\n *\n" +
                                "* The code block will execute until the condition is false\n */\n" +
                                "while(condition) {\n\tcode\n}\n\n//Examples\nwhile(true) {\n\tcode\n}\n\n$var = 7;\nwhile($var <= 10) {\n\t//code\n}\n\n" +
                                "// Conditional Operators:\n" +
                                "// ==, !=, <, >, <=, >=, &&, ||, !");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "continue":
                        usage = new StringBuilder("/*\n * The continue statement is used to skip the rest of the code block and continue to the next iteration of the loop\n */" +
                                "\n$i = 0;\nwhile(true) {\n\tif($i == 5) {\n\t\tcontinue();\n\t}\n\t$i++;\n\t//code\n}\n\n" +
                                "// The above code will skip the code block when $i is 5");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "break":
                        usage = new StringBuilder("/*\n * The break statement is used to exit the loop\n */" +
                                "\n$i = 0;\nwhile(true) {\n\tif($i == 5) {\n\t\tbreak();\n\t}\n\t$i++;\n\t//code\n}\n\n" +
                                "// The above code will exit the loop when $i is 5");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "die":
                        usage = new StringBuilder("/*\n * The die statement is used to stop the script\n */" +
                                "\ndie();\n\n" +
                                "// The above code will stop the script");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "function":
                        usage = new StringBuilder("/*\n * The function statement is used to define a function\n *\n" +
                                "* The function can be called by its name\n" +
                                "* The function can have parameters\n" +
                                "* The function can return a value\n */" +
                                "\nfunction name() {\n\t//code\n}\n\n//Examples\nfunction test() {\n\t//code\n}\n\nfunction test2($a) {\n\treturn($a);\n}\n\n" +
                                "// Calling a function\nname();\n\n// Calling a function with parameters\n$num = test2(5);" +
                                "\n\n// you can use return(); to return a function at any point with no return value, or\n" +
                                "// return($value); to return a value");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "variables":
                        usage = new StringBuilder("/*\n * Variables are used to store data\n *\n" +
                                "* Variables can be of any type\n" +
                                "* Variables can be assigned a value\n" +
                                "* Variables can be used in expressions\n */" +
                                "\n$var = 5;\n\n// The above code assigns the value 5 to the variable $var\n\n");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "subscribe":
                        usage = new StringBuilder("/*\n * The subscribe statement is used to subscribe to an event\n *\n" +
                                "* The event can be any event defined in the client\n" +
                                "* The event can be handled by a function\n */\n" +
                                "subscribe(\"MenuOptionClicked\") {\n\t//code to do things\n}\n" +
                                "// The above code will run the code when the MenuOptionClicked event is triggered" +
                                "\n\n/* Available Events:\n");
                        List<Class<?>> eventClasses = MethodManager.getInstance().getEventClasses();
                        for (Class<?> event : eventClasses)
                        {
                            usage.append(" * ").append(event.getSimpleName()).append("\n");
                        }
                        usage.append(" */");
                        codeTextPane.setText(usage.toString());
                        codeTextPane.setCaretPosition(0);
                        break;

                }
            }
        });

        return tree;
    }

    public void updateDocumentation(MethodDefinition methodDefinition) {
        codeTextPane.setText("//" + methodDefinition.getDescription() + "\n" + generateUsage(methodDefinition));
        codeTextPane.setCaretPosition(0);
    }

    private String generateUsage(MethodDefinition method) {
        StringBuilder params = new StringBuilder();
        int i = 0;
        while (method.getParameters().containsKey(i)) {
            var param = method.getParameters().get(i);
            if (param == null)
                break;
            if (i > 0) {
                params.append(", ");
            }
            params.append(param.getValue().name().toLowerCase()).append(" ").append(param.getKey());
            i++;
        }
        String returnType = method.getReturnType() == null || method.getReturnType().equals(Type.VOID) ? "" : "<" + method.getReturnType().name().toLowerCase() + "> ";
        String name = method.getName() + "(";
        return returnType + name + params + ");";
    }
}
