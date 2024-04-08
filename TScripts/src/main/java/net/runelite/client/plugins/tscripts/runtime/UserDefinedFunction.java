package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import java.util.HashMap;
import java.util.Map;

@Getter
public class UserDefinedFunction
{
    private final String name;
    private final Scope scope;
    private final Map<Integer, String> arguments  = new HashMap<>();

    public UserDefinedFunction(String name, Scope scope)
    {
        this.name = name;
        this.scope = scope;
        int i = 0;
        for(Condition condition : scope.getConditions().getConditions().values())
        {
            if(condition.getLeft() != null)
                arguments.put(i++, condition.getLeft().toString());
        }
    }

    @Setter
    private Object returnValue = null;
}
