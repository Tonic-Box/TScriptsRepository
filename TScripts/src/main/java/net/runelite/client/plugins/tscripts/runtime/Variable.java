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

    public Variable(String name, Object value, String scopeHash)
    {
        System.out.println("Variable created: " + name + " " + value + " " + scopeHash);
        this.name = name;
        this.value = value;
        this.scopeHash = scopeHash;
    }
}
