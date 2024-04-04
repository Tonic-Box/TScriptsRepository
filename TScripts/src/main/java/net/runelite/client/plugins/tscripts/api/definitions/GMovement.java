package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GMovement implements GroupDefinition
{

    @Override
    public String groupName()
    {
        return "Movement";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "walk",
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    if(function.getArg(0, manager) instanceof WorldPoint)
                    {
                        Movement.walkTo((WorldPoint) function.getArg(0, manager));
                        return;
                    }
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    Movement.walkTo(new WorldPoint(x, y, Static.getClient().getPlane()));
                }, "Sends a walk to the specified coordinates.");
        addMethod(methods, "pathfinder",
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    try
                    {
                        if(function.getArg(0, manager) instanceof WorldPoint)
                        {
                            WorldPoint destination = function.getArg(0, manager);
                            while(!destination.equals(Static.getClient().getLocalPlayer().getWorldLocation()))
                            {
                                Movement.walkTo(destination);
                                Api.tick(Rand.nextInt(1, 3));
                            }
                            return;
                        }

                        int x = function.getArg(0, manager);
                        int y = function.getArg(1, manager);
                        int floor = function.getArg(2, manager);
                        WorldPoint destination = new WorldPoint(x, y, floor);
                        while(!destination.equals(Static.getClient().getLocalPlayer().getWorldLocation()))
                        {
                            Movement.walkTo(destination);
                            Api.tick(Rand.nextInt(1, 3));
                        }
                    }
                    catch (Exception ex)
                    {
                        Logging.errorLog(ex);
                    }
                }, "Walks to the specified coordinates using the pathfinder.");
        addMethod(methods, "walkRelative",
                ImmutableMap.of(
                        0, Pair.of("relativeX", Type.INT),
                        1, Pair.of("relativeY", Type.INT)
                ),
                function ->
                {
                    WorldPoint current = Static.getClient().getLocalPlayer().getWorldLocation();
                    int rX = function.getArg(0, manager);
                    int ry = function.getArg(1, manager);
                    int worldX = current.getX() + rX;
                    int worldY = current.getY() + ry;
                    Movement.walkTo(new WorldPoint(worldX, worldY, Static.getClient().getPlane()));
                }, "Walks to the specified relative coordinates.");
        addMethod(methods, "getWorldPoint", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    int z = function.getArgs().length > 2 ? function.getArg(2, manager) : Static.getClient().getPlane();
                    return new WorldPoint(x, y, z);
                }, "Gets a world point object from the specified coordinates.");
        return methods;
    }
}