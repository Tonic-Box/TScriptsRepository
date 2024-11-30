package net.runelite.client.plugins.tscripts.types;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.client.plugins.tscripts.adapter.models.method.MethodCall;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a method definition.
 */
@Getter
public class MethodDefinition
{
    private final String name;
    private final ImmutableMap<Integer, Pair<String, Type>> parameters;
    private final Type returnType;
    private final Function<MethodCall,Object> function;
    private final String description;

    /**
     * Creates a new method definition.
     *
     * @param name the name of the method
     * @param returnType the return type of the method
     * @param parameters the parameters of the method
     * @param function the function that is called when the method is invoked
     * @param description the description of the method
     */
    public MethodDefinition(String name, Type returnType, ImmutableMap<Integer, Pair<String,Type>> parameters, Function<MethodCall,Object> function, String description)
    {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.function = function;
        this.description = description;
    }

    /**
     * Creates a new method definition.
     *
     * @param name the name of the method
     * @param returnType the return type of the method
     * @param parameters the parameters of the method
     * @param function the function that is called when the method is invoked
     * @param description the description of the method
     */
    public MethodDefinition(String name, Type returnType, ImmutableMap<Integer, Pair<String,Type>> parameters, Consumer<MethodCall> function, String description)
    {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.function = func ->
        {
            function.accept(func);
            return null;
        };
        this.description = description;
    }
}