package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Player;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GPlayer implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Player";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "playerAction",
                ImmutableMap.of(
                        0, Pair.of("username", Type.STRING),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    String username = function.getArg(0, manager);

                    Player player = new PlayerQuery().filter(p -> p.getName().equals(username)).result(Static.getClient()).first();

                    Object action = function.getArg(1, manager);

                    if(player == null)
                        return;

                    if(action instanceof Integer)
                    {
                        player.interact((int) action);
                    }
                    else if(action instanceof String)
                    {
                        player.interact((String) action);
                    }
                }, "Interact with a player");
        addMethod(methods, "getPlayer", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("username", Type.STRING),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    String username = function.getArg(0, manager);
                    return new PlayerQuery().filter(p -> p.getName().equals(username)).result(Static.getClient()).first();
                }, "get a player");
        addMethod(methods, "getPlayerOverhead",
                Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("player", Type.ANY)
                ),
                function ->
                {
                    Player player = null;
                    Object identifier = function.getArg(0, manager);
                    if(identifier instanceof String)
                    {
                        player = new PlayerQuery().filter(p -> p.getName().equals(identifier)).result(Static.getClient()).first();
                    }
                    else if(identifier instanceof Integer)
                    {
                        player = new PlayerQuery().filter(p -> p.getIndex() == (int) identifier).result(Static.getClient()).first();
                    }
                    else if(identifier instanceof Player)
                    {
                        player = (Player) identifier;
                    }
                    if(player == null)
                        return "null";

                    if(player.getOverheadIcon() == null)
                        return "null";

                    switch (player.getOverheadIcon())
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