package net.runelite.client.plugins.tscripts.ui.editor.debug;

import net.runelite.client.plugins.tscripts.types.MethodDefinition;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExTreeNode extends DefaultMutableTreeNode
{
    private MethodDefinition methodDefinition;

    public ExTreeNode(MethodDefinition methodDefinition) {
        super(methodDefinition.getName());
        this.methodDefinition = methodDefinition;
    }

    public MethodDefinition getMethod() {
        return methodDefinition;
    }

    public void setMethod(MethodDefinition methodDefinition) {
        this.methodDefinition = methodDefinition;
    }
}
