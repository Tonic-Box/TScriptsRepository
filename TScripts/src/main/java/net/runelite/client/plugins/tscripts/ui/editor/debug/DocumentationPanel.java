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
        conditionalConstructs.add(new DefaultMutableTreeNode("if"));
        conditionalConstructs.add(new DefaultMutableTreeNode("while"));
        conditionalConstructs.add(new DefaultMutableTreeNode("continue"));
        conditionalConstructs.add(new DefaultMutableTreeNode("break"));
        conditionalConstructs.add(new DefaultMutableTreeNode("die"));
        conditionalConstructs.add(new DefaultMutableTreeNode("function"));
        conditionalConstructs.add(new DefaultMutableTreeNode("variables"));
        root.add(conditionalConstructs);

        DefaultMutableTreeNode events = new DefaultMutableTreeNode("Event Subscribers");
        events.add(new DefaultMutableTreeNode("subscribe"));
        events.add(new DefaultMutableTreeNode("events"));
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
            if (selectedNode != null && selectedNode.isLeaf() && selectedNode instanceof ExTreeNode) {
                ExTreeNode exTreeNode = (ExTreeNode) selectedNode;
                MethodDefinition methodDefinition = exTreeNode.getMethod();
                updateDocumentation(methodDefinition);
            }
        });

        return tree;
    }

    public void updateDocumentation(MethodDefinition methodDefinition) {
        codeTextPane.setText("//" + methodDefinition.getDescription() + "\n" + generateUsage(methodDefinition));
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
