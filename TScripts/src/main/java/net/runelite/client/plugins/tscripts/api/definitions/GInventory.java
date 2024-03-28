package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;
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
        addMethod(methods, "inventoryContains", Type.BOOL, ImmutableMap.of(0, Pair.of("item", Type.INT)),
                function ->
                {
                    Object object = function.getArg(0, manager);
                    return Api.getItem(object) != null;
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

                    Item item = Api.getItem(identifier);

                    if (item == null)
                        return;

                    Object operation = function.getArg(1, manager);
                    if (operation instanceof Integer)
                    {
                        item.interact((int) operation);
                    }
                    else if (operation instanceof String)
                    {
                        item.interact((String) operation);
                    }
                }, "Interacts with the item in the inventory");
        addMethod(methods, "dropAll", ImmutableMap.of(0, Pair.of("item", Type.ANY)),
                function ->
                {
                    Object _item = function.getArg(0, manager);
                    List<Item> items = null;
                    if(_item instanceof Integer)
                    {
                        items = new ArrayList<>(Inventory.getAll((int) _item));
                    }
                    else if(_item instanceof String)
                    {
                        items = new ArrayList<>(Inventory.getAll((String) _item));
                    }

                    if(items == null)
                        return;

                    for(Item item : items)
                    {
                        item.drop();
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                        }
                    }
                }, "Drops all items with the given name or id");
        addMethod(methods, "itemOnItem",
                ImmutableMap.of(
                        0, Pair.of("item1", Type.ANY),
                        1, Pair.of("item2", Type.ANY)
                ),
                function ->
                {
                    Object _item1 = function.getArg(0, manager);
                    Object _item2 = function.getArg(1, manager);
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return;

                    Item item1 = Api.getItem(_item1);
                    if (item1 == null)
                        return;

                    Item item2 = Api.getItem(_item2);
                    if (item2 == null)
                        return;

                    item1.useOn(item2);
                }, "Uses the first item on the second item");
        addMethod(methods, "equip", ImmutableMap.of(0, Pair.of("item", Type.ANY)),
                function ->
                {
                    Object identifier = function.getArg(0, manager);
                    ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
                    if(container == null)
                        return;
                    Item item = Api.getItem(identifier);
                    if (item == null)
                        return;

                    item.interact(2);

                }, "Equips the item");
        return methods;
    }
}