package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.TileObjectQuery;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GGameObject implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Game Object";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "objectAction",
                ImmutableMap.of(
                        0, Pair.of("object", Type.ANY),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    TileObject object = Api.getObject(identifier);

                    if(object == null)
                        return;

                    Object interaction = function.getArg(1, manager);
                    if(interaction instanceof Integer)
                    {
                        int action = (int) interaction;
                        object.interact(action);
                    }
                    else if(interaction instanceof String)
                    {
                        String action = (String) interaction;
                        object.interact(action);
                    }
                }, "Interacts with the object");
        addMethod(methods, "itemOnObject",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("object", Type.ANY)
                ),
                function ->
                {
                    Object _item = function.getArg(0, manager);
                    Object _object = function.getArg(1, manager);
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return;

                    Item item = Api.getItem(_item);
                    if (item == null)
                        return;

                    TileObject object = Api.getObject(_object);
                    if(object == null)
                        return;

                    item.useOn(object);
                }, "Uses an item on an object");
        addMethod(methods, "getObject", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("identifier", Type.ANY)
                        ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    return Api.getObject(identifier);
                }, "Gets an object");
        addMethod(methods, "getObjectAt", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("identifier", Type.ANY),
                        1, Pair.of("worldX", Type.ANY),
                        2, Pair.of("worldY", Type.ANY)
                        ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    int worldX = function.getArg(1, manager);
                    int worldY = function.getArg(2, manager);
                    return Api.getObjectAt(identifier, worldX, worldY);
                }, "Gets a game object");
        return methods;
    }
}