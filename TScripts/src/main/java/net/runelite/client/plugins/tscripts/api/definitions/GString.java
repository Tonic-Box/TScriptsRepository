package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;

import java.util.ArrayList;
import java.util.List;

public class GString implements GroupDefinition
{
    @Override
    public String groupName() {
        return "String";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "Concat", Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("elements", Type.VARARGS)
                ),
                function ->
                {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < function.getArgs().length; i++)
                    {
                        Object arg = function.getArg(i, manager);
                        sb.append(arg);
                    }
                    return sb.toString();
                }, "concat elements to a string");

        addMethod(methods, "stringContains", Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("text", Type.STRING),
                        1, Pair.of("search", Type.ANY)
                ),
                function ->
                {
                    String text = function.getArg(0, manager);
                    String search = function.getArg(1, manager);
                    return text.contains(search);
                }, "checks if a string contains a sub string");

        return methods;
    }
}
