package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TItem;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.client.Static;

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
                0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    if(function.getArg(0, manager) instanceof WorldPoint)
                    {
                        WorldPoint destination = function.getArg(0, manager);
                        Api.waitUntilOnTile(destination.getX(), destination.getY());
                        return;
                    }
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    Api.waitUntilOnTile(x, y);
                },
                "Pauses script until the local player is on a given tile"
        );
        addMethod(methods, "waitForResource", ImmutableMap.of(
                0, Pair.of("items", Type.VARARGS)
                ),
                function ->
                {
                    int count = TItem.count(function.getArgs());
                    int current = count;
                    while (count == current)
                    {
                        if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
                        {
                            return;
                        }
                        Api.tick(1);
                        count = TItem.count(function.getArgs());
                    }
                },
                "Pauses script until th inventory has gained one of the chosen item(s)"
        );
        return methods;
    }
}
