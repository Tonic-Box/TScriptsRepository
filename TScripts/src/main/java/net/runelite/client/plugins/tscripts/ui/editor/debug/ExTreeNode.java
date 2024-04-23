package net.runelite.client.plugins.tscripts.ui.editor.debug;

import net.runelite.client.plugins.tscripts.types.EventData;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExTreeNode extends DefaultMutableTreeNode {
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

    public ExTreeNode clone()
    {
        if (methodDefinition != null)
        {
            return new ExTreeNode(methodDefinition);
        }
        else if (eventData != null)
        {
            return new ExTreeNode(eventData);
        }
        return null;
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

    public String getQueriable() {
        if (methodDefinition != null) {
            return generateUsage(methodDefinition) + " (" + methodDefinition.getDescription() + ")";
        } else if (eventData != null) {
            return generateUsage(eventData);
        }
        return null;
    }

    private String generateUsage()
    {
        if (methodDefinition != null)
        {
            return generateUsage(methodDefinition) + " (" + methodDefinition.getDescription() + ")";
        }
        else if (eventData != null && eventData.getKeys() != null && !eventData.getKeys().isEmpty())
        {
            return generateUsage(eventData);
        }
        return "";
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

    private String generateUsage(EventData event) {
        StringBuilder params = new StringBuilder();
        if(event.getKeys() != null && !event.getKeys().isEmpty())
        {
            for (String key : event.getKeys()) {
                params.append("$event[\"").append(key).append("\"];\n");
            }
        }
        return params.toString();
    }
}
