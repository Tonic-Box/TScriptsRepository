package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TTrade;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GTrade implements GroupDefinition
{
    @Override
    public String groupName() {
        return "Trade";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "firstTradeWindowOpen", Type.BOOL,
                ImmutableMap.of(),
                function -> TTrade.firstWindowOpen(),
                "Checks if the first trade window is open"
        );
        addMethod(methods, "secondTradeWindowOpen", Type.BOOL,
                ImmutableMap.of(),
                function -> TTrade.secondWindowOpen(),
                "Checks if the second trade window is open"
        );
        addMethod(methods, "acceptTrade",
                ImmutableMap.of(),
                function -> TTrade.accept(),
                "Accepts the trade"
        );
        addMethod(methods, "declineTrade",
                ImmutableMap.of(),
                function -> TTrade.decline(),
                "Declines the trade"
        );
        addMethod(methods, "offerTrade",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("amount", Type.INT)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    int amount = function.getArg(1, manager);
                    TTrade.offer(item, amount);
                },
                "Offers an item in the trade"
        );
        addMethod(methods, "removeTrade",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    int amount = function.getArg(1, manager);
                    TTrade.remove(item, amount);
                },
                "Removes an item from the trade"
        );

        return methods;
    }
}
