package net.runelite.client.plugins.tscripts.lexer.Scope.condition;

import lombok.AllArgsConstructor;
import net.runelite.client.plugins.tscripts.lexer.models.TokenType;

import java.util.Objects;

/**
 * Enum representing the different types of comparators
 */
@AllArgsConstructor
public enum Comparator
{
    EQ,
    LT,
    GT,
    LTEQ,
    GTEQ,
    NEQ;

    /**
     * Converts a base token type to a comparator
     * @param type the base token type
     * @return the comparator
     */
    public static Comparator fromBaseTokenType(TokenType type)
    {
        switch (type)
        {
            case CONDITION_GT:
                return GT;
            case CONDITION_LT:
                return LT;
            case CONDITION_GTEQ:
                return GTEQ;
            case CONDITION_LTEQ:
                return LTEQ;
            case CONDITION_EQ:
                return EQ;
            case CONDITION_NEQ:
                return NEQ;
            default:
                return null;
        }
    }

    /**
     * Processes the comparison of two objects
     * @param left the left object
     * @param right the right object
     * @return the result of the comparison
     */
    public boolean process(Object left, Object right)
    {
        if (left instanceof Integer && right instanceof Integer )
        {
            Integer integer1 = (Integer) left;
            Integer integer2 = (Integer) right;
            switch (this)
            {
                case GT:
                    return integer1 > integer2;
                case LT :
                    return integer1 < integer2;
                case GTEQ:
                    return integer1 >= integer2;
                case LTEQ:
                    return integer1 <= integer2;
                case EQ:
                    return integer1.equals(integer2);
                case NEQ:
                    return !integer1.equals(integer2);
            };
        }
        return this == Comparator.NEQ ? !Objects.equals(left.getClass(), right.getClass()) : left.equals(right);
    }
}