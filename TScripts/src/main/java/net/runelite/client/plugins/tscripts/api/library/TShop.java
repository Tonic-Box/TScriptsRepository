package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Item;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.tscripts.sevices.ItemContainerQuery;
import net.runelite.client.plugins.tscripts.types.ShopID;

/**
 * API for interacting with npc shops
 */
public class TShop {
    /**
     * buy 1 of an item
     * @param itemId item id
     */
    public static void buy1(int itemId)
    {
        Item item = getShopItem(itemId);
        if(item == null)
            return;
        buy_1(item);
    }

    /**
     * buy 5 of an item
     * @param itemId item id
     */
    public static void buy5(int itemId)
    {
        Item item = getShopItem(itemId);
        if(item == null)
            return;
        buy_5(item);
    }

    /**
     * buy 10 of an item
     * @param itemId item id
     */
    public static void buy10(int itemId)
    {
        Item item = getShopItem(itemId);
        if(item == null)
            return;
        buy_10(item);
    }

    /**
     * buy 50 of an item
     * @param itemId item id
     */
    public static void buy50(int itemId)
    {
        Item item = getShopItem(itemId);
        if(item == null)
            return;
        buy_50(item);
    }

    /**
     * buy 1 of an item
     * @param itemName item name
     */
    public static void buy1(String itemName)
    {
        Item item = getShopItem(itemName);
        if(item == null)
            return;
        buy_1(item);
    }

    /**
     * buy 5 of an item
     * @param itemName item name
     */
    public static void buy5(String itemName)
    {
        Item item = getShopItem(itemName);
        if(item == null)
            return;
        buy_5(item);
    }

    /**
     * buy 10 of an item
     * @param itemName item name
     */
    public static void buy10(String itemName)
    {
        Item item = getShopItem(itemName);
        if(item == null)
            return;
        buy_10(item);
    }

    /**
     * buy 50 of an item
     * @param itemName item name
     */
    public static void buy50(String itemName)
    {
        Item item = getShopItem(itemName);
        if(item == null)
            return;
        buy_50(item);
    }

    /**
     * get an item by its name from the shop container
     * @param itemName item name
     * @return item
     */
    public static Item getShopItem(String itemName)
    {
        return new ItemContainerQuery<>(ShopID.getCurrent()).withName(itemName).findFirst();
    }

    /**
     * get an item by its id from the shop container
     * @param itemId item id
     * @return item
     */
    public static Item getShopItem(int itemId)
    {
        return new ItemContainerQuery<>(ShopID.getCurrent()).withId(itemId).findFirst();
    }

    /**
     * get the shops current quantity of an item
     * @param itemId item id
     * @return quantity
     */
    public static int getStockQuantity(int itemId)
    {
        return new ItemContainerQuery<>(ShopID.getCurrent()).withId(itemId).getQuantity();
    }

    /**
     * get the shops current quantity of an item
     * @param itemName item name
     * @return quantity
     */
    public static int getStockQuantity(String itemName)
    {
        return new ItemContainerQuery<>(ShopID.getCurrent()).withName(itemName).getQuantity();
    }

    /**
     * check if a shop currently has an item in stock
     * @param itemId item id
     * @return boolean
     */
    public static boolean shopContains(int itemId)
    {
        return getStockQuantity(itemId) != 0;
    }

    /**
     * check if a shop currently has an item in stock
     * @param itemName item name
     * @return boolean
     */
    public static boolean shopContains(String itemName)
    {
        return getStockQuantity(itemName) != 0;
    }

    /**
     * buy 1 of an item
     * @param item item
     */
    public static void buy_1(Item item)
    {
        interactShop(item, 1);
    }

    /**
     * buy 5 of an item
     * @param item item
     */
    public static void buy_5(Item item)
    {
        interactShop(item, 2);
    }

    /**
     * buy 10 of an item
     * @param item item
     */
    public static void buy_10(Item item)
    {
        interactShop(item, 3);
    }

    /**
     * buy 50 of an item
     * @param item item
     */
    public static void buy_50(Item item)
    {
        interactShop(item, 4);
    }

    /**
     * interactShop with a shop
     * @param item item
     * @param action action
     */
    public static void interactShop(Item item, int action)
    {
        shopAction(item.getId(), item.getSlot(), action);
    }

    /**
     * send a raw shop item menu action
     * @param itemId item id
     * @param slot slot
     * @param action action
     */
    public static void shopAction(int itemId, int slot, int action)
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(action, ShopID.SHOP_ID, slot + 1, itemId);
    }

    /**
     * interact with an item in the inventory
     * @param item item
     * @param action action
     */
    public static void interactInventory(Item item, int action)
    {
        inventoryAction(item.getId(), item.getSlot(), action);
    }

    /**
     * interact with an item in the inventory
     * @param itemId item id
     * @param slot slot
     * @param action action
     */
    public static void inventoryAction(int itemId, int slot, int action)
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(action, WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER.getId(), slot, itemId);
    }
}