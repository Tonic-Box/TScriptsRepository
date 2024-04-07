package net.runelite.client.plugins.tscripts.lexer.variable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.models.ElementType;

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
    public VariableAssignment(String var, List<Object> values, AssignmentType type)
    {
        this.var = var;
        this.values = values;
        this.assignmentType = type;
        setType(ElementType.VARIABLE_ASSIGNMENT);
    }

    private String var;
    private List<Object> values;
    private AssignmentType assignmentType;
}
