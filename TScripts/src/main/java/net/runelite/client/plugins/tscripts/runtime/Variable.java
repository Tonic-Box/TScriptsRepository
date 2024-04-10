package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Variable
{
    private final String name;
    @Setter
    private Object value;
    private final String scopeHash;
    @Setter
    private boolean frozen;

    public Variable(String name, Object value, String scopeHash)
    {
        this.name = name;
        this.value = value;
        this.scopeHash = scopeHash;
    }
}
