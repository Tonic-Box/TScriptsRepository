package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Item;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TBank;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;

import java.util.ArrayList;
import java.util.List;

public class GBank implements GroupDefinition
{
    @Override
    public String groupName() {
        return "Bank";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "deposit",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("amount", Type.INT)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    int amount = function.getArg(1, manager);
                    if(item instanceof Item)
                    {
                        TBank.deposit(((Item) item).getId(), amount);
                    }
                    else if(item instanceof Integer)
                    {
                        TBank.deposit((int) item, amount);
                    }
                    else
                    {
                        TBank.deposit((String) item, amount);
                    }
                }, "Deposit an item into the bank");
        addMethod(methods, "withdraw",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("amount", Type.INT),
                        2, Pair.of("noted", Type.BOOL)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    int amount = function.getArg(1, manager);
                    boolean noted = function.getArg(2, manager);
                    if(item instanceof Item)
                    {
                        TBank.withdraw(((Item) item).getId(), amount, noted);
                    }
                    else if(item instanceof Integer)
                    {
                        TBank.withdraw((int) item, amount, noted);
                    }
                    else
                    {
                        TBank.withdraw((String) item, amount, noted);
                    }
                }, "withdraw an item from the bank");
        addMethod(methods, "countBank", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Item)
                    {
                        return TBank.count(((Item) item).getId());
                    }
                    else if(item instanceof Integer)
                    {
                        return TBank.count((int) item);
                    }
                    else
                    {
                        return TBank.count((String) item);
                    }
                }, "Count the number of an item in the bank");
        addMethod(methods, "bankContains", Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Item)
                    {
                        return TBank.contains(((Item) item).getId());
                    }
                    else if(item instanceof Integer)
                    {
                        return TBank.contains((int) item);
                    }
                    else
                    {
                        return TBank.contains((String) item);
                    }
                }, "Check if the bank contains an item");
        addMethod(methods, "bankGetSlot", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Item)
                    {
                        return TBank.getSlot(((Item) item).getId());
                    }
                    else if(item instanceof Integer)
                    {
                        return TBank.getSlot((int) item);
                    }
                    else
                    {
                        return TBank.getSlot((String) item);
                    }
                }, "gets the slot of an item in the bank");
        addMethod(methods, "depositAllInventory",
                ImmutableMap.of(),
                function -> TBank.depositAllInventory(), "Deposit all items in the inventory into the bank");
        addMethod(methods, "depositAllEquipment",
                ImmutableMap.of(),
                function -> TBank.depositAllEquipment(), "Deposit all items in the equipment into the bank");
        addMethod(methods, "bankIsOpen", Type.BOOL,
                ImmutableMap.of(),
                function -> TBank.isOpen(), "Check if the bank is open");

        return methods;
    }
}
