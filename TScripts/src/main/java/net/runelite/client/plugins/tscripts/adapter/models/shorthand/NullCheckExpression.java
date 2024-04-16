package net.runelite.client.plugins.tscripts.adapter.models.shorthand;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NullCheckExpression
{
    private final Object value;
    private final boolean negated;
}
