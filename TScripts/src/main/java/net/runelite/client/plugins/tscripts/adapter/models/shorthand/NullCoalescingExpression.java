package net.runelite.client.plugins.tscripts.adapter.models.shorthand;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NullCoalescingExpression
{
    private final Object left;
    private final Object right;
    private final boolean negated;
}
