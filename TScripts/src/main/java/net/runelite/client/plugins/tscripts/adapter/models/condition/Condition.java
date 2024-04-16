package net.runelite.client.plugins.tscripts.adapter.models.condition;

import lombok.Data;

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
     */
    public Condition(Object left, Object right, Comparator comparator)
    {
        setLeft(left);
        setRight(right);
        setComparator(comparator);
    }

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
        return new Condition(left, right, comparator);
    }

    @Override
    public String toString()
    {
        String compString = "";
        if(comparator != null)
        {
            switch (comparator) {
                case GT:
                    compString = " > ";
                    break;
                case LT:
                    compString = " < ";
                    break;
                case GTEQ:
                    compString = " >= ";
                    break;
                case LTEQ:
                    compString = " <= ";
                    break;
                case EQ:
                    compString = " == ";
                    break;
                case NEQ:
                    compString = " != ";
                    break;
            }
        }
        return (left != null ? left.toString() : "") +
                " " + compString +
                " " + (right != null ? right.toString() : "");
    }
}