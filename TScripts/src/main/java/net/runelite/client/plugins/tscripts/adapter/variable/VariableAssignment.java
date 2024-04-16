package net.runelite.client.plugins.tscripts.adapter.variable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.client.plugins.tscripts.adapter.models.ElementType;
import net.runelite.client.plugins.tscripts.adapter.models.Element;

import java.util.List;

/**
 * Represents a variable assignment in the script
 */

@EqualsAndHashCode(callSuper = false)
@Data
public class VariableAssignment extends Element
{
    /**
     * Create a new variable assignment
     * @param var The variable name
     * @param values The values to assign
     * @param type The type of assignment
     */
    public VariableAssignment(Object var, List<Object> values, AssignmentType type)
    {
        this.var = var;
        this.values = values;
        this.assignmentType = type;
        setType(ElementType.VARIABLE_ASSIGNMENT);
    }

    private Object var;
    private List<Object> values;
    private AssignmentType assignmentType;

    public boolean isArray()
    {
        return var instanceof ArrayAccess;
    }
}
