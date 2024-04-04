package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GGroundItem implements GroupDefinition
{
    @Override
    public String groupName() {
        return "Ground Item";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "getGroundItem", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("identifiers", Type.VARARGS)
                ),
                function ->
                {
                    Object identifier;
                    for(int i = 0; i < function.getArgs().length; i++)
                    {
                        identifier = function.getArg(i, manager);
                        TileItem item = Api.getTileItem(identifier);
                        if(item != null)
                            return item;
                    }

                    return null;
                }, "Gets a ground item at the specified location");
        addMethod(methods, "getGroundItemAt", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("identifier", Type.VARARGS),
                        1, Pair.of("coords", Type.VARARGS)
                ),
                function ->
                {
                    int size = function.getArgs().length;
                    int end;
                    WorldPoint point;
                    if(function.getArg(size -1, manager) instanceof WorldPoint)
                    {
                        point = function.getArg(size - 1, manager);
                        end = size - 1;
                    }
                    else
                    {
                        point = new WorldPoint(function.getArg(size - 2, manager), function.getArg(size - 1, manager), Static.getClient().getPlane());
                        end = size - 2;
                    }

                    Object identifier;
                    for(int i = 0; i < end; i++)
                    {
                        identifier = function.getArg(i, manager);
                        TileItem item = Api.getTileItemAt(identifier, point.getX(), point.getY());
                        if(item != null)
                            return item;
                    }

                    return null;
                }, "Gets a ground item at the specified location");
        addMethod(methods, "groundItemAction",
                ImmutableMap.of(
                        0, Pair.of("identifier", Type.ANY),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    TileItem item;
                    if(identifier instanceof TileItem)
                    {
                        item = (TileItem) identifier;
                    }
                    else
                    {
                        item = Api.getTileItem(identifier);
                    }
                    if(item == null)
                        return;

                    Object action = function.getArg(1, manager);
                    if(action instanceof Integer)
                    {
                        item.interact((int) action);
                    }
                    else if(action instanceof String)
                    {
                        item.interact((String) action);
                    }
                }, "Interacts with the ground item");

        return methods;
    }
}
