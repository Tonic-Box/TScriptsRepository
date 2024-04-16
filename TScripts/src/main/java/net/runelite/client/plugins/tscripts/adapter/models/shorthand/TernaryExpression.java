package net.runelite.client.plugins.tscripts.adapter.models.shorthand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Conditions;

@AllArgsConstructor
@Getter
public class TernaryExpression
{
    private final Conditions conditions;
    private final Object trueValue;
    private final Object falseValue;
    private final boolean negated;
}
