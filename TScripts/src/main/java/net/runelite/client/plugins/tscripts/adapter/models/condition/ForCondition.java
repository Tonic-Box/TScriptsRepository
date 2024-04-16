package net.runelite.client.plugins.tscripts.adapter.models.condition;

import lombok.Data;
import net.runelite.client.plugins.tscripts.adapter.models.variable.VariableAssignment;

@Data
public class ForCondition
{
    private VariableAssignment variableAssignment;
    private VariableAssignment operation;
}
