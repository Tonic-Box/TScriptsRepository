package net.runelite.client.plugins.tscripts.runtime.variables;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Variable
{
    private final String name;
    @Setter
    private Object value;
    @Setter
    private boolean frozen;

    public Variable(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }
}
