package net.runelite.client.plugins.tscripts.adapter.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OperatorType
{
    ADD("+"),
    SUBTRACT("-"),
    DIVIDE("/"),
    MULTIPLY("*"),
    MODULO("%"),
    OR("|"),
    AND("&"),
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT(">>"),
    UNSIGNED_RIGHT_SHIFT(">>>");

    private final String symbol;

    public static OperatorType of(String operator)
    {
        switch (operator)
        {
            case "+":
                return ADD;
            case "-":
                return SUBTRACT;
            case "/":
                return DIVIDE;
            case "*":
                return MULTIPLY;
            case "%":
                return MODULO;
            case "|":
                return OR;
            case "&":
                return AND;
            case "<<":
                return LEFT_SHIFT;
            case ">>":
                return RIGHT_SHIFT;
            case ">>>":
                return UNSIGNED_RIGHT_SHIFT;
            default:
                return null;
        }
    }

    public static int compute(int left, OperatorType operator, int right)
    {
        int out;
        switch (operator)
        {
            case ADD:
                out = left + right;
                break;
            case SUBTRACT:
                out = left - right;
                break;
            case DIVIDE:
                out = left / right;
                break;
            case MULTIPLY:
                out = left * right;
                break;
            case MODULO:
                out = left % right;
                break;
            case OR:
                out = left | right;
                break;
            case AND:
                out = left & right;
                break;
            case LEFT_SHIFT:
                out = left << right;
                break;
            case RIGHT_SHIFT:
                out = left >> right;
                break;
            case UNSIGNED_RIGHT_SHIFT:
                out = left >>> right;
                break;
            default:
                out = 0;
        }
        return out;
    }
}
