package net.runelite.client.plugins.tscripts.adapter.Scope.condition;

import lombok.Data;
import net.runelite.client.plugins.tscripts.adapter.variable.VariableAssignment;

@Data
public class ForCondition
{
    private VariableAssignment variableAssignment;
    private VariableAssignment operation;
}
