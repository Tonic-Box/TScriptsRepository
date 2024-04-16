package net.runelite.client.plugins.tscripts.adapter.Scope.condition;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class Conditions
{
    private final Map<Integer, Condition> conditions = new HashMap<>();
    private final Map<Integer, Glue> glues = new HashMap<>();
    private ConditionType type;
    private String userFunctionName = null;
    private ForCondition forCondition = null;
    private boolean current = false;

    @Override
    public Conditions clone()
    {
        Conditions newConditions = new Conditions();
        for (Map.Entry<Integer, Condition> entry : getConditions().entrySet())
        {
            newConditions.getConditions().put(entry.getKey(), entry.getValue().clone());
        }
        for (Map.Entry<Integer, Glue> entry : getGlues().entrySet())
        {
            newConditions.getGlues().put(entry.getKey(), entry.getValue());
        }
        newConditions.setType(getType());
        newConditions.setUserFunctionName(getUserFunctionName());
        newConditions.setForCondition(getForCondition());
        return newConditions;
    }

    @Override
    public String toString()
    {
        StringBuilder conditionString = new StringBuilder();
        for (Map.Entry<Integer, Condition> entry : getConditions().entrySet())
        {
            conditionString.append(entry.getValue().toString());
            if (getGlues().containsKey(entry.getKey()))
            {
                switch (getGlues().get(entry.getKey()))
                {
                    case AND:
                        conditionString.append(" && ");
                        break;
                    case OR:
                        conditionString.append(" || ");
                        break;
                }
            }
        }
        return conditionString.toString();
    }
}
