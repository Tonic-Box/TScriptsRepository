package net.runelite.client.plugins.tscripts.ui.editor.debug;

import net.runelite.client.plugins.tscripts.types.EventData;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExTreeNode extends DefaultMutableTreeNode
{
    private MethodDefinition methodDefinition;
    private EventData eventData;

    public ExTreeNode(MethodDefinition methodDefinition) {
        super(methodDefinition.getName());
        this.methodDefinition = methodDefinition;
    }

    public ExTreeNode(EventData eventData) {
        super(eventData.getEventName());
        this.eventData = eventData;
    }

    public EventData getEvent() {
        return eventData;
    }

    public void setEvent(EventData eventData) {
        this.eventData = eventData;
    }

    public MethodDefinition getMethod() {
        return methodDefinition;
    }

    public void setMethod(MethodDefinition methodDefinition) {
        this.methodDefinition = methodDefinition;
    }
}
