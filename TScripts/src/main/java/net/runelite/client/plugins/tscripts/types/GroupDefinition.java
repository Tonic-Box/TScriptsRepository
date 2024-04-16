package net.runelite.client.plugins.tscripts.types;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.adapter.models.method.MethodCall;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a group of methods that can be called from the script.
 */
public interface GroupDefinition
{
    /**
     * The name of the group.
     *
     * @return the name of the group
     */
    String groupName();

    /**
     * The methods in the group.
     *
     * @param manager the method manager
     * @return the methods
     */
    List<MethodDefinition> methods(MethodManager manager);

    /**
     * Add a method with a return type to the group.
     *
     * @param methods the list of methods
     * @param name the name of the method
     * @param returnType the return type of the method
     * @param parameters the parameters of the method
     * @param function the function to call
     * @param description the description of the method
     */
    default void addMethod(List<MethodDefinition> methods, String name, Type returnType, ImmutableMap<Integer, Pair<String, Type>> parameters, Function<MethodCall, Object> function, String description)
    {
        methods.add(new MethodDefinition(name, returnType, parameters, function, description));
    }

    /**
     * Add a method without a return type to the group.
     *
     * @param methods the list of methods
     * @param name the name of the method
     * @param parameters the parameters of the method
     * @param function the function to call
     * @param description the description of the method
     */
    default void addMethod(List<MethodDefinition> methods, String name, ImmutableMap<Integer, Pair<String, Type>> parameters, Consumer<MethodCall> function, String description)
    {
        methods.add(new MethodDefinition(name, Type.VOID, parameters, function, description));
    }
}
