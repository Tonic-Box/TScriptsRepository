package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TMovement;
import net.runelite.client.plugins.tscripts.api.library.TWorldPoint;
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
                        WorldPoint destination = function.getArg(0, manager);
                        TMovement.walkTo(destination);
                        return;
                    }
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    TMovement.walkTo(x, y);
                }, "Sends a walk to the specified coordinates. coords can be\n" +
                            "//either a worldpoint object or x, y.");
        addMethod(methods, "pathfinder",
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    try
                    {
                        WorldPoint destination;
                        if(function.getArg(0, manager) instanceof WorldPoint)
                        {
                            destination = function.getArg(0, manager);
                        }
                        else
                        {
                            int x = function.getArg(0, manager);
                            int y = function.getArg(1, manager);
                            int floor = function.getArg(2, manager);
                            destination = new WorldPoint(x, y, floor);
                        }

                        while(!destination.equals(Static.getClient().getLocalPlayer().getWorldLocation()))
                        {
                            Movement.walkTo(destination);
                            TDelay.tick(Rand.nextInt(1, 3));
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
                    TMovement.walkTo(worldX, worldY);
                }, "Walks to the specified relative coordinates.");
        addMethod(methods, "walkRelativeOf",
                ImmutableMap.of(
                        0, Pair.of("relativeX", Type.INT),
                        1, Pair.of("relativeY", Type.INT),
                        2, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    WorldPoint current = Static.getClient().getLocalPlayer().getWorldLocation();
                    int rX = function.getArg(0, manager);
                    int ry = function.getArg(1, manager);
                    int worldX = current.getX() + rX;
                    int worldY = current.getY() + ry;
                    TMovement.walkTo(worldX, worldY);
                }, "Walks to the specified relative coordinates of the specified\n" +
                            "//coordinates. The corrds can be either a worldpoint object or x, y.");
        addMethod(methods, "getWorldPoint", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    int z = function.getArgs().length > 2 ? function.getArg(2, manager) : Static.getClient().getPlane();
                    return TWorldPoint.get(new WorldPoint(x, y, z));
                }, "Gets a world point object from the specified coordinates.");
        addMethod(methods, "walkHere",
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    Tile tile = Static.getClient().getSelectedSceneTile();
                    if(tile == null)
                        return;

                    TMovement.walkTo(tile.getWorldLocation());
                }, "Walks to the currently selected/hovered tile.");
        addMethod(methods, "getRegion",
                Type.INT,
                ImmutableMap.of(),
                function ->
                {
                    Player player = Static.getClient().getLocalPlayer();
                    if(player == null)
                        return -1;

                    return player.getWorldLocation().getRegionID();
                }, "Gets the current region ID.");
        addMethod(methods, "inRegion",
                Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("regionID", Type.INT)
                ),
                function ->
                {
                    Player player = Static.getClient().getLocalPlayer();
                    if(player == null)
                        return -1;

                    int regionID = function.getArg(0, manager);

                    return player.getWorldLocation().getRegionID() == regionID;
                }, "Checks if you're in a region");
        addMethod(methods, "inArea",
                Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    Object arg = function.getArg(0, manager);
                    Object arg2 = function.getArg(1, manager);

                    if(arg instanceof WorldPoint && arg2 instanceof WorldPoint)
                    {
                        WorldPoint p1 = (WorldPoint) arg;
                        WorldPoint p2 = (WorldPoint) arg2;
                        return TMovement.inArea(p1, p2);
                    }

                    int x1 = function.getArg(0, manager);
                    int y1 = function.getArg(1, manager);
                    int x2 = function.getArg(2, manager);
                    int y2 = function.getArg(3, manager);
                    return TMovement.inArea(x1, y1, x2, y2);
                }, "Checks if you're in an area. Takes either 2 worldpoint\n" +
                            "//objects, or x, y, x, y.");
        return methods;
    }
}