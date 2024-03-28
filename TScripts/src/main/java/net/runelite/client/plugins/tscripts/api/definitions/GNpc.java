package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import java.util.ArrayList;
import java.util.List;

public class GNpc implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Npc";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "attack", ImmutableMap.of(0, Pair.of("identifier", Type.ANY)),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    NPC npc = Api.getFreeNpc(identifier);
                    if (npc == null)
                        return;
                    npc.interact("Attack");
                }, "Attack the nearest npc");
        addMethod(methods, "npcAction",
                ImmutableMap.of(
                        0, Pair.of("name", Type.ANY),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    NPC npc = Api.getFreeNpc(identifier);
                    if (npc == null)
                        return;

                    Object action = function.getArg(1, manager);
                    if (action instanceof Integer)
                    {
                        npc.interact((int)action);
                    }
                    else if (action instanceof String)
                    {
                        npc.interact((String)action);
                    }
                }, "Interact with the nearest npc");

        addMethod(methods, "itemOnNpc",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("npc", Type.ANY)
                ),
                function ->
                {
                    Object _item = function.getArg(0, manager);
                    Object _npc = function.getArg(1, manager);
                    Item item = Api.getItem(_item);
                    if (item == null)
                        return;
                    NPC npc = Api.getFreeNpc(_npc);
                    if (npc == null)
                        return;
                    item.useOn(npc);
                }, "Use an item on the nearest npc");
        return methods;
    }
}