package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.GameState;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GGame implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Game";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "getVarbit", Type.INT, ImmutableMap.of(0, Pair.of("id", Type.INT)),
                function -> Api.invoke(() -> Static.getClient().getVarbitValue(Static.getClient().getVarps(), function.getArg(0, manager))),
                "fetches a varbit value"
        );
        addMethod(methods, "getGameState", Type.INT, ImmutableMap.of(),
                function -> Api.invoke(Static.getClient().getGameState()::getState),
                "fetches the game state"
        );
        addMethod(methods, "setUsername", ImmutableMap.of(0, Pair.of("username", Type.STRING)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setUsername(function.getArg(0, manager))),
                "sets the username"
        );
        addMethod(methods, "setPassword", ImmutableMap.of(0, Pair.of("password", Type.STRING)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setPassword(function.getArg(0, manager))),
                "sets the password"
        );
        addMethod(methods, "setGameState", ImmutableMap.of(0, Pair.of("state", Type.INT)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setGameState(GameState.of(function.getArg(0, manager)))),
                "sets the game state"
        );
        addMethod(methods, "setTab", ImmutableMap.of(0, Pair.of("tab", Type.INT)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().runScript(915, function.getArg(0, manager))),
                "sets current the tab"
        );
        return methods;
    }
}