package net.runelite.client.plugins.tscripts.lexer.variable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArrayAccess
{
    @Getter
    private final String variable;
    private final Object index;
    @Getter
    private final boolean negated;

    public String getIndex()
    {
        return index == null ? "" : index.toString();
    }
}
