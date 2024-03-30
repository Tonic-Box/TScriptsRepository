package net.runelite.client.plugins.tscripts.lexer.Scope.condition;

import lombok.Data;
import lombok.Setter;

/**
 * Represents a condition in a script
 */
@Data
public class Condition
{
    /**
     * Creates a new condition
     * @param left the left side of the condition
     * @param right the right side of the condition
     * @param comparator the comparator to use
     * @param type the type of the condition
     */
    public Condition(Object left, Object right, Comparator comparator, ConditionType type)
    {

        setType(type);
        setLeft(left);
        setRight(right);
        setComparator(comparator);
    }

    private ConditionType type;
    private Object left;
    private Object right;
    private Comparator comparator;
    private boolean current = false;

    /**
     * Clones the condition
     * @return a new condition with the same values
     */
    public Condition clone()
    {
        return new Condition(left, right, comparator, type);
    }

    @Override
    public String toString()
    {
        return "Condition{" +
                "type=" + type +
                ", left=" + left +
                ", right=" + right +
                ", comparator=" + comparator +
                '}';
    }
}