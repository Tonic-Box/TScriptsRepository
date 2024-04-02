package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.util.Logging;

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
                function ->
                {
                    int length = function.getArgs().length > 0 ? function.getArg(0, manager) : 1;
                    Api.tick(length);
                },
                "Pauses script for a # of game ticks. defaults to 1 tick."
        );
        addMethod(methods, "sleep", ImmutableMap.of(0, Pair.of("length", Type.INT)),
                function ->
                {
                    try
                    {
                        int length = function.getArg(0, manager);
                        Thread.sleep(length);
                    } catch (InterruptedException ex)
                    {
                        Logging.errorLog(ex);
                    }
                }, "Pauses script for a # of milliseconds.");
        addMethod(methods, "waitUntilIdle", ImmutableMap.of(),
                function -> Api.waitUntilIdle(),
                "Pauses script until the local player is idle."
        );
        addMethod(methods, "sleep", ImmutableMap.of(0, Pair.of("length", Type.INT)),
                function ->
                {
                    try
                    {
                        int length = function.getArg(0, manager);
                        Thread.sleep(length);
                    } catch (InterruptedException ex)
                    {
                        Logging.errorLog(ex);
                    }
                }, "Pauses script for a # of milliseconds.");
        addMethod(methods, "waitUntilOnTile", ImmutableMap.of(
                0, Pair.of("worldX", Type.INT),
                1, Pair.of("worldY", Type.INT)
                ),
                function ->
                {
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    Api.waitUntilOnTile(x, y);
                },
                "Pauses script until the local player is on a given tile"
        );
        return methods;
    }
}
