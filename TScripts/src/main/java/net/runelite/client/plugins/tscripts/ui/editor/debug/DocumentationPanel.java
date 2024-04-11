package net.runelite.client.plugins.tscripts.ui.editor.debug;

import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.ui.editor.ExRSyntaxTextArea;
import net.runelite.client.plugins.tscripts.util.TextUtil;
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
        conditionalConstructs.add(new DefaultMutableTreeNode("for"));
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
                        usage = new StringBuilder("LyoKICogVGhlIGNvbmRpdGlvbiBjYW4gYmUgYW55IGV4cHJlc3Npb24gdGhhdCBldmFsdWF0ZXMgdG8gYSBib29sZWFuIHZhbHVlCiAqCiogVGhlIGNvZGUgYmxvY2sgd2lsbCBvbmx5IGV4ZWN1dGUgaWYgdGhlIGNvbmRpdGlvbiBpcyB0cnVlLgogKiB5b3UgY2FuIG9wdGlvbmFsbHkgZm9sbG93IGlmIHN0YXRlbWVudHMgd2l0aCBhbiBlbHNlIHsgLi4uIH0gYmxvY2sKICogd2hpY2ggd2lsbCBleGVjdXRlIGlmIHRoZSBpZiBibG9jayBkb2VzbnQgcGFzcy4KICogCiAqIENvbmRpdGlvbmFsIE9wZXJhdG9yczoKICogPT0sICE9LCA8LCA+LCA8PSwgPj0sICYmLCB8fCwgIQogKi8KCmlmKGNvbmRpdGlvbikgCnsKCS8vY29kZQp9CmVsc2UKewoJLy9jb2RlCn0=");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "while":
                        usage = new StringBuilder("LyoKICogVGhlIGNvbmRpdGlvbiBjYW4gYmUgYW55IGV4cHJlc3Npb24gdGhhdCBldmFsdWF0ZXMgdG8gYSBib29sZWFuIHZhbHVlCiAqCiogVGhlIGNvZGUgYmxvY2sgd2lsbCBleGVjdXRlIHVudGlsIHRoZSBjb25kaXRpb24gaXMgZmFsc2UKKiAKKiBDb25kaXRpb25hbCBPcGVyYXRvcnM6CiogPT0sICE9LCA8LCA+LCA8PSwgPj0sICYmLCB8fCwgIQogKi8KIAp3aGlsZShjb25kaXRpb24pIAp7CgkvL2NvZGUKfQ==");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "for":
                        usage = new StringBuilder("LyoKICogRm9yIGxvb3BzIGNvbnRhaW4gYSBkZWNsYXJhdGlvbiwgYSBjb25kaXRpb24sIGFuZCBhbiBvcHBvcmF0aW9uLgogKi8KCmZvcihkZWNsYXJhdGlvbjsgY29uZGl0aW9uOyBvcHBvcmF0aW9uKQp7CgkvL2NvZGUgdG8gZG8gdGhpbmdzCn0KCi8vZXhhbXBsZQpmb3IoJGkgPSAwOyAkaSA8PSAxMDsgJGkrKykKewoJZGVidWcoJGkpOwp9Cg==");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "continue":
                        usage = new StringBuilder("LyoKICogVGhlIGNvbnRpbnVlIHN0YXRlbWVudCBpcyB1c2VkIHRvIHNraXAgdGhlIHJlc3Qgb2YgdGhlIAogKiBjb2RlIGJsb2NrIGFuZCBjb250aW51ZSB0byB0aGUgbmV4dCBpdGVyYXRpb24gb2YgdGhlIGxvb3AuCiAqLwokaSA9IDA7CndoaWxlKCRpIDw9IDEwKSAKewoJJGkrKzsKCWlmKCRpID09IDUpIAoJewoJCWRlYnVnKCJTa2lwcGluZyIpOwoJCS8vIHNraXAgdGhlIGNvZGUgYmxvY2sgd2hlbiAkaSBpcyA1CgkJY29udGludWUoKTsKCX0KCWRlYnVnKCJDb3VudDogIiwgJGkpOwp9Cg==");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "break":
                        usage = new StringBuilder("LyoKICogVGhlIGJyZWFrIHN0YXRlbWVudCBpcyB1c2VkIHRvIGV4aXQgdGhlIGxvb3BzIHNjb3BlCiAqLwokaSA9IDA7CndoaWxlKHRydWUpIAp7CglpZigkaSA9PSA1KSAKCXsKCQkvL2JyZWFrIG91dCBvZiB0aGUgbG9vcAoJCWJyZWFrKCk7Cgl9CgkkaSsrOwoJLy9jb2RlCn0=");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "die":
                        usage = new StringBuilder("LyoKICogVGhlIGRpZSBzdGF0ZW1lbnQgaXMgdXNlZCB0byBzdG9wIHRoZSBzY3JpcHQKICovCgpkaWUoKTs=");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "function":
                        usage = new StringBuilder("LyoKICogVGhlIGZ1bmN0aW9uIHN0YXRlbWVudCBpcyB1c2VkIHRvIGRlZmluZSBhIGZ1bmN0aW9uCiAqCiogVGhlIGZ1bmN0aW9uIGNhbiBiZSBjYWxsZWQgYnkgaXRzIG5hbWUKKiBUaGUgZnVuY3Rpb24gY2FuIGhhdmUgcGFyYW1ldGVycwoqIFRoZSBmdW5jdGlvbiBjYW4gcmV0dXJuIGEgdmFsdWUKICovCmZ1bmN0aW9uIG5hbWUoKSAKewoJLy9jb2RlCn0KCi8vRXhhbXBsZXMKZnVuY3Rpb24gdGVzdCgpIAp7CgkvL2NvZGUKfQoKZnVuY3Rpb24gdGVzdDIoJGEpIAp7CglyZXR1cm4oJGEpOwp9CgovLyBDYWxsaW5nIGEgZnVuY3Rpb24KbmFtZSgpOwoKLy8gQ2FsbGluZyBhIGZ1bmN0aW9uIHdpdGggcGFyYW1ldGVycwokbnVtID0gdGVzdDIoNSk7CgovLyB5b3UgY2FuIHVzZSByZXR1cm4oKTsgdG8gcmV0dXJuIGEgZnVuY3Rpb24gYXQgYW55IHBvaW50IHdpdGggbm8gcmV0dXJuIHZhbHVlLCBvcgovLyByZXR1cm4oJHZhbHVlKTsgdG8gcmV0dXJuIGEgdmFsdWU=");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "variables":
                        usage = new StringBuilder("LyoKICogVmFyaWFibGVzIGFyZSB1c2VkIHRvIHN0b3JlIGRhdGEKICoKKiBWYXJpYWJsZXMgY2FuIGJlIG9mIGFueSB0eXBlCiogVmFyaWFibGVzIGNhbiBiZSBhc3NpZ25lZCBhIHZhbHVlCiogVmFyaWFibGVzIGNhbiBiZSB1c2VkIGluIGV4cHJlc3Npb25zCiAqLwoKIC8vQXNzaWducyB0aGUgdmFsdWUgNSB0byB0aGUgdmFyaWFibGUgJHZhcgokdmFyID0gNTsKCi8qCiAqIEFycmF5cyBhcmUgYW5vdGhlciB0eXBlIG9mIHZhcmlhYmxlIHRoYXQgY2FuIHN0b3JlIG11bHRpcGxlIHZhbHVlcwogKiBhbmQgYWNjZXNzIHRoZW0gdmlhIGFuIGluZGV4LiBhcnJheXMgY2FuIGJlIGRlY2xhcmVkIHVzaW5nIHRoZQogKiBhcnJheSh2YXJhcmdzIHZhbHVlcykgZnVuY3Rpb24uIFNPbWUgbWV0aG9kcyBtYXkgYWxzbyByZXR1cm4gYXJyYXlzLgogKi8KCi8vY3JlYXRlIGFuIGFycmF5IHdpdGggc29tZSBpbmljaWFsIHZhbHVlcyBvZiBpbmRleGVzIDAtMwogJGFycmF5W10gPSBhcnJheSgib25lIiwgMTIsICJpZGsiLCBmYWxzZSk7CgogLy9wcmludHMgb3V0IHRoZSB2YWx1ZSBhdCBpbmRleCAxICgxMikKIGRlYnVnKCRhcnJheVsxXSk7CgogLy95b3UgY2FuIHNldCBuZXcgaW5kZXhlcwogJGFycmF5WzRdID0gIm5ldyB2YWx1ZSI7");
                        codeTextPane.setText(TextUtil.decodeBase64(usage.toString()));
                        codeTextPane.setCaretPosition(0);
                        break;
                    case "subscribe":
                        usage = new StringBuilder(TextUtil.decodeBase64("LyoKICogVGhlIHN1YnNjcmliZSBzdGF0ZW1lbnQgaXMgdXNlZCB0byBzdWJzY3JpYmUgdG8gYW4gZXZlbnQKICoKKiBUaGUgZXZlbnQgY2FuIGJlIGFueSBldmVudCBkZWZpbmVkIGluIHRoZSBjbGllbnQKKiBUaGUgZXZlbnQgY2FuIGJlIGhhbmRsZWQgYnkgYSBmdW5jdGlvbgogKi8KCi8vcnVucyB0aGUgY29kZSB3aGVuIHRoZSBjbGllbnQgcG9zdHMgdGhlIGBNZW51T3B0aW9uQ2xpY2tlZGAgZXZlbnQuCnN1YnNjcmliZSgiTWVudU9wdGlvbkNsaWNrZWQiKQp7CgkvL2NvZGUgdG8gZG8gdGhpbmdzCn0="));
                        List<Class<?>> eventClasses = MethodManager.getInstance().getEventClasses();
                        usage.append("\n\n/* Available events:\n");
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
