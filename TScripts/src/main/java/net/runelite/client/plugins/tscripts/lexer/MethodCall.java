package net.runelite.client.plugins.tscripts.lexer;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.models.ElementType;

/**
 * Represents a method call in the script
 */
@Getter
public class MethodCall extends Element
{
    /**
     * Creates a new method call
     * @param name The name of the method
     * @param args The arguments of the method
     * @param negated Whether the method is negated
     */
    public MethodCall(String name, Object[] args, boolean negated)
    {
        this.negate = negated;
        this.name = name;
        this.args = args;
        setType(ElementType.FUNCTION_CALL);
    }

    /**
     * Gets the argument at the specified index and processes its value
     * @param index The index of the argument
     * @param methodManager The method manager to call the method with
     * @param <T> The type of the argument
     * @return The argument
     */
    public <T> T getArg(int index, MethodManager methodManager)
    {
        if(index >= args.length)
            return null;

        if (args[index] instanceof MethodCall)
        {
            return (T) methodManager.call((MethodCall)args[index]);
        }

        return (T) args[index];
    }

    private final boolean negate;
    private final String name;
    private final Object[] args;

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder(name + "(");
        int i = 0;
        String str = "";
        for (Object arg : args)
        {
            out.append(arg.toString()).append(", ");
        }
        out.append(")");
        return out.toString().replace(", )", ")");
    }
}
