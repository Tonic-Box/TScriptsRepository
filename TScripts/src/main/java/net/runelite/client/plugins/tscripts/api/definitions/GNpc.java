package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TInventory;
import net.runelite.client.plugins.tscripts.api.library.TNpc;
import net.runelite.client.plugins.tscripts.api.enums.NpcFilter;
import net.runelite.client.plugins.tscripts.api.UserQueries;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;

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
        addMethod(methods, "npcAction",
                ImmutableMap.of(
                        0, Pair.of("npc", Type.ANY),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    NPC npc = TNpc.getNpc(identifier);
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
                    Item item = TInventory.getItem(_item);
                    if (item == null)
                        return;
                    NPC npc = TNpc.getNpc(_npc);
                    if (npc == null)
                        return;
                    item.useOn(npc);
                }, "Use an item on the nearest npc");
        addMethod(methods, "getNpc",
                Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("npc", Type.ANY)
                ),
                function ->
                {
                    Object _npc = function.getArg(0, manager);
                    return TNpc.getNpc(_npc);
                }, "Get an npc object");

        addMethod(methods, "getNpcByFilter",
                Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("identifier", Type.OBJECT),
                        1, Pair.of("npcFilters", Type.VARARGS)
                ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    String[] filters = new String[function.getArgs().length - 1];
                    for (int i = 1; i < function.getArgs().length; i++)
                    {
                        filters[i-1] = function.getArg(i, manager);
                    }
                    return UserQueries.getNpc(identifier, filters);
                }, "Get an npc object");
        addMethod(methods, "getNpcOverhead",
                Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("npc", Type.ANY)
                ),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    NPC npc = TNpc.getNpc(identifier);
                    if(npc == null)
                        return "null";

                    if(npc.getComposition().getOverheadIcon() == null)
                        return "null";

                    switch (npc.getComposition().getOverheadIcon())
                    {
                        case MELEE:
                            return "PROTECTION_FROM_MELEE";
                        case RANGED:
                            return "PROTECTION_FROM_RANGED";
                        case MAGIC:
                            return "PROTECTION_FROM_MAGIC";
                        default:
                            return "null";
                    }
                },
                "gets the overhead protection of a player"
        );

        return methods;
    }
}