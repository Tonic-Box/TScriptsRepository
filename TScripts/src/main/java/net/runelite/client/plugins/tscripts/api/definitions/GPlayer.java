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
        return methods;
    }
}