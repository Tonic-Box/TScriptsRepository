package net.runelite.client.plugins.tscripts.adapter.models.variable;

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
        return index == null ? null : index.toString();
    }
}
