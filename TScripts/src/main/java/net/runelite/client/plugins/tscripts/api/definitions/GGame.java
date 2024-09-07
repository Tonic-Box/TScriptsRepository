package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.GameState;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TGame;
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
                function -> TGame.invoke(() -> Static.getClient().getVarbitValue(Static.getClient().getVarps(), function.getArg(0, manager))),
                "fetches a varbit value"
        );
        addMethod(methods, "getVarPlayer", Type.INT, ImmutableMap.of(0, Pair.of("id", Type.INT)),
                function -> TGame.invoke(() -> Static.getClient().getVarpValue(Static.getClient().getVarps(),function.getArg(0, manager))),
                "fetches a varplayer value"
        );
        addMethod(methods, "getGameState", Type.INT, ImmutableMap.of(),
                function -> TGame.invoke(Static.getClient().getGameState()::getState),
                "fetches the game state", false
        );
        addMethod(methods, "setUsername", ImmutableMap.of(0, Pair.of("username", Type.STRING)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setUsername(function.getArg(0, manager))),
                "sets the username", false
        );
        addMethod(methods, "setPassword", ImmutableMap.of(0, Pair.of("password", Type.STRING)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setPassword(function.getArg(0, manager))),
                "sets the password", false
        );
        addMethod(methods, "setGameState", ImmutableMap.of(0, Pair.of("state", Type.INT)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setGameState(GameState.of(function.getArg(0, manager)))),
                "sets the game state", false
        );
        addMethod(methods, "setIdleTimeout", ImmutableMap.of(0, Pair.of("timeout", Type.INT)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().setIdleTimeout(function.getArg(0, manager))),
                "sets the idle timeout", false
        );
        addMethod(methods, "setTab", ImmutableMap.of(0, Pair.of("tab", Type.INT)),
                function -> Static.getClientThread().invoke(() -> Static.getClient().runScript(915, function.getArg(0, manager))),
                "sets current the tab"
        );
        addMethod(methods, "menuAction",
                ImmutableMap.<Integer, Pair<String,Type>>builder()
                        .put(0, Pair.of("option", Type.STRING))
                        .put(1, Pair.of("target", Type.STRING))
                        .put(2, Pair.of("identifier", Type.INT))
                        .put(3, Pair.of("opcode", Type.INT))
                        .put(4, Pair.of("param0", Type.INT))
                        .put(5, Pair.of("param1", Type.INT))
                        .build(),
                function -> TGame.invoke(() -> Static.getClient().invokeMenuAction(
                        function.getArg(0, manager),
                        function.getArg(1, manager),
                        function.getArg(2, manager),
                        function.getArg(3, manager),
                        function.getArg(4, manager),
                        function.getArg(5, manager)
                )),
                "Invokes a menu action"
        );
        return methods;
    }
}
