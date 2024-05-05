package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Player;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TPlayer;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.entities.Players;
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
                    Object action = function.getArg(1, manager);

                    Player player = TPlayer.getPlayer(TPlayer.nameEquals(username));

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
                        0, Pair.of("username", Type.STRING)
                ),
                function ->
                {
                    String username = function.getArg(0, manager);
                    return TPlayer.getPlayer(TPlayer.nameEquals(username));
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
                        player = TPlayer.getPlayer(TPlayer.nameEquals((String) identifier));
                    }
                    else if(identifier instanceof Integer)
                    {
                        player = TPlayer.getPlayer(TPlayer.indexEquals((int) identifier));
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

        addMethod(methods, "getPlayerWithin", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("distance", Type.INT)
                ),
                function ->
                {
                    int distance = function.getArg(0, manager);
                    return TPlayer.getPlayer(TPlayer.withinDistance(distance));
                }, "get a random player within a distance");

        addMethod(methods, "isPlayerWithin", Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("distance", Type.INT)
                ),
                function ->
                {
                    int distance = function.getArg(0, manager);
                    Player player = TPlayer.getPlayer(TPlayer.withinDistance(distance));
                    if(player == null)
                    {
                        return false;
                    }
                    System.out.println("Player nearby: " + player.getName());
                    return true;
                }, "check if a player is within a distance");

        return methods;
    }
}