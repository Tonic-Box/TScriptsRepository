package net.runelite.client.plugins.tscripts.adapter.models;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.types.TriPair;

@Getter
public class Expression extends Element
{
    private final TriPair<Object,OperatorType,Object> data;

    public Expression(Object left, OperatorType operator, Object right)
    {
        data = TriPair.of(left, operator, right);
        setType(ElementType.EXPRESSION);
    }
}
