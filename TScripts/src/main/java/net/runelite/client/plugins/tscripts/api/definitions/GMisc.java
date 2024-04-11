package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.client.Static;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

public class GMisc implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Miscellaneous";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "isEven", Type.BOOL, ImmutableMap.of(0, Pair.of("number", Type.INT)),
                function -> ((int) function.getArg(0, manager)) % 2 == 0,
                "Returns true if the number is even, false otherwise"
        );
        addMethod(methods, "isOdd", Type.BOOL, ImmutableMap.of(0, Pair.of("number", Type.INT)),
                function -> ((int) function.getArg(0, manager)) % 2 != 0,
                "Returns true if the number is odd, false otherwise"
        );
        addMethod(methods, "click",
                ImmutableMap.of(),
                function -> {
                    try {
                        Robot bot = new Robot();
                        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    } catch (AWTException ignored) { }
                },
                "Clicks the mouse"
        );
        addMethod(methods, "debug", ImmutableMap.of(0, Pair.of("args", Type.VARARGS)),
                function ->
                {
                    StringBuilder output = new StringBuilder();
                    for (Object object : function.getArgs())
                    {
                        output.append(object);
                    }
                    String msg = output.toString();
                    Static.getClientThread().invoke(() -> {
                        if(Static.getClient().getGameState().equals(GameState.LOGGED_IN) || Static.getClient().getGameState().equals(GameState.LOADING) || Static.getClient().getGameState().equals(GameState.HOPPING))
                            Static.getClient().addChatMessage(ChatMessageType.GAMEMESSAGE, msg, msg, "", true);
                        Logging.logToEditor(msg, Color.WHITE);
                    });
                }, "Prints the arguments to the console and chatbox");
        addMethod(methods, "rand", Type.INT,
                ImmutableMap.of(),
                function -> (int) (Math.random() * Integer.MAX_VALUE),
                "Clicks the mouse"
        );
        addMethod(methods, "getTickCount", Type.INT,
                ImmutableMap.of(),
                function -> Static.getClient().getTickCount(),
                "Clicks the mouse"
        );
        addMethod(methods, "array", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("values", Type.VARARGS)
                ),
                function ->
                {
                    Object[] values = new Object[function.getArgs().length];
                    for (int i = 0; i < function.getArgs().length; i++)
                    {
                        values[i] = function.getArg(i, manager);
                    }
                    return values;

                },
                "Creates a new array with the given values"
        );

        return methods;
    }
}