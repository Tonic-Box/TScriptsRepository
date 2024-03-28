package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;

import java.util.ArrayList;
import java.util.List;

public class GDelay implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Delays";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "tick", ImmutableMap.of(0, Pair.of("length", Type.INT)),
                function -> Api.tick(function.getArg(0, manager)),
                "Pauses script for a # of game ticks."
        );
        addMethod(methods, "tick", ImmutableMap.of(),
                function -> Api.tick(1),
                "Pauses script for 1 game tick."
        );
        addMethod(methods, "sleep", ImmutableMap.of(0, Pair.of("length", Type.INT)),
                function ->
                {
                    try
                    {
                        int length = function.getArg(0, manager);
                        Thread.sleep(length);
                    } catch (InterruptedException e)
                    {
                        //scriptFrame.logError(e.getMessage());
                    }
                }, "Pauses script for a # of milliseconds.");
        addMethod(methods, "waitUntilIdle", ImmutableMap.of(),
                function -> Api.waitUntilIdle(),
                "Pauses script until the local player is idle."
        );
        return methods;
    }
}
