package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TInventory
{
    public static void interact(Item item, int action)
    {
        if (item == null)
            return;
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendItemActionPacket(item.getSlot(), item.getId(), action);
    }

    public static void interact(Item item, String action)
    {
        if (item == null)
            return;
        int actionIndex = Arrays.asList(item.getActions()).indexOf(action);
        if (actionIndex == -1)
            return;
        interact(item, actionIndex);
    }
    public static void useOn(Item item, TileObject object)
    {
        if (item == null || object == null)
            return;
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendWidgetOnObjectPacket(WidgetInfo.INVENTORY.getId(), item.getId(), item.getSlot(), object.getId(), object.getWorldLocation().getX(), object.getWorldLocation().getY(), false);
    }

    public static void useOn(Item item, Item item2)
    {
        if (item == null || item2 == null)
            return;
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendWidgetOnWidgetPacket(WidgetInfo.INVENTORY.getId(), item.getId(), item.getSlot(), WidgetInfo.INVENTORY.getId(), item2.getId(), item2.getSlot());
    }

    public static int count(Object[] Identifiers)
    {
        int count = 0;
        for (Object identifier : Identifiers)
        {
            if (identifier instanceof Integer)
            {
                count += count((int) identifier);
            }
            else if (identifier instanceof String)
            {
                count += count((String) identifier);
            }
            else if (identifier instanceof Item)
            {
                count += count(((Item) identifier).getId());
            }
        }
        return count;
    }

    public static int count(int itemId)
    {
        Integer value = TGame.invoke(() -> {
            ItemContainer inventory = Static.getClient().getItemContainer(InventoryID.INVENTORY);
            if(inventory == null)
                return 0;
            int count = 0;
            for(Item item : inventory.getItems())
            {
                if(item != null && item.getId() == itemId)
                {
                    count += item.getQuantity();
                }
            }
            return count;
        });
        return value == null ? 0 : value;
    }

    public static int count(String itemName)
    {
        Integer value = TGame.invoke(() ->
        {
            ItemContainer inventory = Static.getClient().getItemContainer(InventoryID.INVENTORY);
            if(inventory == null)
                return 0;
            int count = 0;
            for(Item item : inventory.getItems())
            {
                if(item != null && item.getName().contains(itemName))
                {
                    count += item.getQuantity();
                }
            }
            return count;
        });
        return value == null ? 0 : value;
    }

    public static Item getItem(Object identifier)
    {
        if(identifier instanceof Item)
        {
            return (Item) identifier;
        }
        ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
        if(container == null)
            return null;
        Item item = null;
        if (identifier instanceof Integer)
        {
            item = Inventory.getFirst((int)identifier);
        }
        else if (identifier instanceof String)
        {
            item = Inventory.getFirst((String)identifier);
            if(item == null)
            {
                //idk why this jank is needed for contains, but, cba
                item = Arrays.stream(container.getItems()).filter(i -> i.getName().contains(identifier.toString())).findFirst().orElse(null);
                if(item != null)
                {
                    item = Inventory.getFirst(item.getId());
                }
            }
        }
        return item;
    }

    public static int emptySlots()
    {
        Integer value = TGame.invoke(() -> {
            ItemContainer inventory = Static.getClient().getItemContainer(InventoryID.INVENTORY);
            if(inventory == null)
                return 0;
            int count = 0;

            for(Item item : inventory.getItems())
            {
                if(item != null && item.getId() != -1 && item.getQuantity() != 0)
                {
                    count++;
                }
            }

            return 28 - count;
        });
        return value == null ? 0 : value;
    }
}
