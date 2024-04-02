package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;

@RequiredArgsConstructor
@Getter
public class UserDefinedFunction
{
    private final String name;
    private final Scope scope;

    @Setter
    private Object returnValue = null;

    public boolean hasReturnValue()
    {
        return returnValue != null;
    }
}
