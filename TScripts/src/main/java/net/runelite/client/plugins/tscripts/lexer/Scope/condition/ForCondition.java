package net.runelite.client.plugins.tscripts.lexer.Scope.condition;

import lombok.Data;
import net.runelite.client.plugins.tscripts.lexer.variable.VariableAssignment;

@Data
public class ForCondition
{
    private VariableAssignment variableAssignment;
    private VariableAssignment operation;
}
