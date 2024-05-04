package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TInventory;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class GInventory implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Inventory";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "inventoryContains", Type.BOOL, ImmutableMap.of(0, Pair.of("item", Type.VARARGS)),
                function ->
                {
                    for (Object object : function.getArgs())
                    {
                        if (object instanceof Integer)
                        {
                            if (Inventory.contains((int) object))
                                return true;
                        }
                        else if (object instanceof String)
                        {
                            if (Inventory.contains((String) object))
                                return true;
                        }
                        else if (object instanceof Item)
                        {
                            if (Inventory.contains(((Item) object).getName()))
                                return true;
                        }
                    }
                    return false;
                }, "Checks if the inventory contains the item");
        addMethod(methods, "itemAction",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("action", Type.ANY)
                ),
                function ->
                {
                    if (function.getArgs().length != 2)
                        return;
                    Object identifier = function.getArg(0, manager);

                    Item item = TInventory.getItem(identifier);

                    if (item == null)
                        return;

                    Object operation = function.getArg(1, manager);
                    if (operation instanceof Integer)
                    {
                        TInventory.interact(item, (int) operation);
                    }
                    else if (operation instanceof String)
                    {
                        TInventory.interact(item, (String) operation);
                    }
                }, "Interacts with the item in the inventory");
        addMethod(methods, "consume",
                ImmutableMap.of(
                        0, Pair.of("items", Type.VARARGS)
                ),
                function ->
                {
                    Item item = null;

                    for(Object identifier : function.getArgs())
                    {
                        if (identifier instanceof Item)
                        {
                            item = (Item) identifier;
                        }
                        else
                        {
                            item = TInventory.getItem(identifier);
                        }

                        if(item != null)
                            break;
                    }

                    if (item == null)
                        return;

                    if(ArrayUtils.contains(item.getActions(), "Eat"))
                    {
                        TInventory.interact(item, "Eat");
                    }
                    else if(ArrayUtils.contains(item.getActions(), "Sip"))
                    {
                        TInventory.interact(item, "Sip");
                    }
                    else if(ArrayUtils.contains(item.getActions(), "Drink"))
                    {
                        TInventory.interact(item, "Drink");
                    }

                }, "Consumes the first occurrence of any of the given items in the inventory");
        addMethod(methods, "dropAll", ImmutableMap.of(0, Pair.of("items", Type.VARARGS)),
                function ->
                {
                    if(function.getArgs().length == 0)
                    {
                        for(Item item : Inventory.getAll())
                        {
                            TInventory.interact(item, "Drop");
                        }
                        return;
                    }
                    Object _item = function.getArg(0, manager);
                    List<Item> items = new ArrayList<>();
                    for (Object identifier : function.getArgs())
                    {
                        if (identifier instanceof Integer)
                        {
                            items.addAll(Inventory.getAll((int) identifier));
                        }
                        else if (identifier instanceof String)
                        {
                            items.addAll(Inventory.getAll((String) identifier));
                        }
                        else if (identifier instanceof Item)
                        {
                            items.addAll(Inventory.getAll(((Item) _item).getName()));
                        }
                    }

                    if(items.isEmpty())
                        return;

                    for(Item item : items)
                    {
                        TInventory.interact(item, "Drop");
                    }
                }, "Drops all items with the given names and/or id's");
        addMethod(methods, "itemOnItem",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY),
                        1, Pair.of("targetItems", Type.VARARGS)
                ),
                function ->
                {
                    Object _item = function.getArg(0, manager);
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return;

                    Item item = TInventory.getItem(_item);
                    if (item == null)
                        return;

                    Item item2 = null;

                    for(int i = 1; i < function.getArgs().length; i++)
                    {
                        Object identifier = function.getArg(i, manager);
                        if (identifier instanceof Integer)
                        {
                            item2 = Inventory.getFirst((int) identifier);
                        }
                        else if (identifier instanceof String)
                        {
                            item2 = Inventory.getFirst((String) identifier);
                        }
                        else if (identifier instanceof Item)
                        {
                            item2 = (Item) identifier;
                        }

                        if(item2 != null)
                            break;
                    }

                    if (item2 == null)
                        return;

                    TInventory.useOn(item, item2);
                }, "Uses the first item on the first occurrence of any of the other items listed");
        addMethod(methods, "getItem", Type.OBJECT,
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object _item1 = function.getArg(0, manager);
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return null;

                    return TInventory.getItem(_item1);
                }, "gets and item from the inventory");
        addMethod(methods, "equip", ImmutableMap.of(0, Pair.of("item", Type.ANY)),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return;
                    Item item = TInventory.getItem(identifier);
                    if (item == null)
                        return;
                    TInventory.interact(item, 2);
                }, "Equips the item");
        addMethod(methods, "countInventory", Type.INT, ImmutableMap.of(0, Pair.of("items", Type.VARARGS)),
                function ->
                {
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return 0;
                    return  TInventory.count(function.getArgs());
                }, "Counts the number of items in the inventory");
        addMethod(methods, "inventoryEmptySlots", Type.INT, ImmutableMap.of(),
                function ->
                {
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return 0;
                    return  TInventory.emptySlots();
                }, "Counts the number of empty slots in the inventory left");
        return methods;
    }
}