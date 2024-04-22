package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.enums.GrandExchangeSlot;
import net.runelite.client.plugins.tscripts.api.library.TGrandExchange;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import java.util.ArrayList;
import java.util.List;

public class GGrandExchange implements GroupDefinition {
    @Override
    public String groupName() {
        return "Grand Exchange";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "buyGE", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("item", Type.INT),
                        1, Pair.of("quantity", Type.INT),
                        2, Pair.of("price", Type.INT),
                        3, Pair.of("noted", Type.BOOL)
                ),
                function ->
                {
                    int id = function.getArg(0, manager);
                    int quantity = function.getArg(1, manager);
                    int price = function.getArg(2, manager);
                    boolean noted = function.getArg(3, manager);

                    if(price == -1)
                    {
                        TGrandExchange.buy(id, quantity, noted);
                        return -1;
                    }
                    return TGrandExchange.buy(id, quantity, price, noted);
                },
                "Purchase an item from the grand exchange. If price is -1, it will \n" +
                        "just bump price until it buys and collect for you. otherwise will \n" +
                        "return the slot number."
        );

        addMethod(methods, "sellGE", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("id", Type.INT),
                        1, Pair.of("quantity", Type.INT),
                        2, Pair.of("price", Type.INT)
                ),
                function ->
                {
                    int id = function.getArg(0, manager);
                    int quantity = function.getArg(1, manager);
                    int price = function.getArg(2, manager);
                    if(price == -1)
                    {
                        TGrandExchange.sell(id, quantity, true);
                        return -1;
                    }
                    return TGrandExchange.sell(id, quantity, price);
                },
                "Purchase an item from the grand exchange. If price is -1, it will \n" +
                         "just bump price until it sells and collect for you. otherwise will \n" +
                        "return the slot number."
        );

        addMethod(methods, "checkSlotGE", Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("slotNumber", Type.INT)
                ),
                function ->
                {
                    int slotNumber = function.getArg(0, manager);
                    GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
                    if(slot == null)
                        return false;
                    return slot.isDone();
                },
                "Check if a slot is done. returns true if done, false if not done or if slot is not found."
        );

        addMethod(methods, "collectSlotGE",
                ImmutableMap.of(
                        0, Pair.of("slotNumber", Type.INT),
                        1, Pair.of("quantity", Type.INT),
                        2, Pair.of("noted", Type.BOOL)
                ),
                function ->
                {
                    int slotNumber = function.getArg(0, manager);
                    int quantity = function.getArg(1, manager);
                    boolean noted = function.getArg(2, manager);
                    GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
                    if(slot == null)
                        return;
                    TGrandExchange.collectFromSlot(slotNumber, noted, quantity);
                },
                "Collects an item from a slot in the grand exchange. If quantity is -1, it will collect all."
        );

        addMethod(methods, "collectAllGE",
                ImmutableMap.of(),
                function -> TGrandExchange.collectAll(),
                "Collects an item from a slot in the grand exchange. If quantity is -1, it will collect all."
        );

        addMethod(methods, "isOpenGE", Type.BOOL,
                ImmutableMap.of(),
                function -> TGrandExchange.isOpen(),
                "Collects an item from a slot in the grand exchange. If quantity is -1, it will collect all."
        );

        return methods;
    }
}
