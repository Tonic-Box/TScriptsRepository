package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.List;

public class TBank
{
    public static final int WITHDRAW_X_AMOUNT = 3960;
    public static final int BANK_NOTED_MODE = 3958;
    public static void depositAllInventory()
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, WidgetInfo.BANK_DEPOSIT_INVENTORY.getId(), -1, -1);
    }

    public static void depositAllEquipment()
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, WidgetInfo.BANK_DEPOSIT_EQUIPMENT.getId(), -1, -1);
    }

    /**
     * set the withdraw-x amount
     * @param amount amount
     */
    public static void setX(int amount)
    {
        Client client = Static.getClient();
        Static.getClientThread().invoke(() -> {
            if(client.getVarbitValue(WITHDRAW_X_AMOUNT) != amount && amount != 1 && amount != 5 && amount != 10 && amount != -1)
            {
                TPackets.sendWidgetActionPacket(1, 786466, -1, -1);
                TPackets.sendResumeCountDialoguePacket(amount);
                TClientScript.closeNumericInputDialogue();
            }
            else if(client.getVarbitValue(6590) != 3)
            {
                TPackets.sendWidgetActionPacket(1, 786466, -1, -1);
                TPackets.sendResumeCountDialoguePacket(1);
                TClientScript.closeNumericInputDialogue();
            }
        });
    }

    /**
     * Toggle weather or not to withdraw as note
     * @param b bool
     */
    public static void setWithdrawMode(boolean b) {
        Client client = Static.getClient();
        int noted = TGame.invoke(() -> client.getVarbitValue(BANK_NOTED_MODE));
        if(b && noted != 1) {
            Static.getClientThread().invoke(() -> {
                TPackets.sendClickPacket();
                TPackets.sendWidgetActionPacket(0, 786456, -1, -1);
            });
        }
        else if(!b && noted != 0) {
            Static.getClientThread().invoke(() -> {
                TPackets.sendClickPacket();
                TPackets.sendWidgetActionPacket(0, 786454, -1, -1);
            });
        }
    }

    public static boolean isOpen()
    {
        return TGame.invoke(() -> Static.getClient().getItemContainer(InventoryID.BANK) != null);
    }

    public static boolean contains(int itemId)
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
            {
                return false;
            }
            for(Item item : getItems())
            {
                if(item.getId() == itemId)
                {
                    return true;
                }
            }
            return false;
        });
    }

    public static int getSlot(int itemId)
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
            {
                return -1;
            }
            for(Item item : getItems())
            {
                if(item.getId() == itemId)
                {
                    return item.getSlot();
                }
            }
            return -1;
        });
    }

    public static boolean contains(String itemName)
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
                return false;
            for(Item item : getItems())
            {
                if(item.getName().toLowerCase().contains(itemName.toLowerCase()))
                {
                    return true;
                }
            }
            return false;
        });
    }

    public static int getSlot(String itemName)
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
                return -1;
            for(Item item : getItems())
            {
                if(item.getName().toLowerCase().contains(itemName.toLowerCase()))
                {
                    return item.getSlot();
                }
            }
            return -1;
        });
    }

    public static List<Item> getItems()
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
            {
                return new ArrayList<>();
            }

            List<Item> items = new ArrayList<>();
            for(Item item : bank.getItems())
            {
                if(item != null && item.getId() != -1)
                {
                    items.add(item);
                }
            }

            return items;
        });
    }

    public static List<Item> getItemsInventory()
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.INVENTORY);
            if(bank == null)
            {
                return new ArrayList<>();
            }

            List<Item> items = new ArrayList<>();
            for(Item item : bank.getItems())
            {
                if(item != null && item.getId() != -1)
                {
                    items.add(item);
                }
            }

            return items;
        });
    }

    /**
     * check how many of an item exists in the bank by its item ID
     * @param itemId item ID
     * @return boolean
     */
    public static int count(int itemId)
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
                return 0;
            int count = 0;
            for(Item item : getItems())
            {
                if(item != null && item.getId() == itemId)
                {
                    count += item.getQuantity();
                }
            }
            return count;
        });
    }

    /**
     * check how many of an item exists in the bank by its item name (allows partial matching)
     * @param itemName item name
     * @return boolean
     */
    public static int count(String itemName)
    {
        return TGame.invoke(() -> {
            ItemContainer bank = Static.getClient().getItemContainer(InventoryID.BANK);
            if(bank == null)
                return 0;
            int count = 0;
            for(Item item : getItems())
            {
                if(item != null && item.getName().toLowerCase().contains(itemName.toLowerCase()))
                {
                    count += item.getQuantity();
                }
            }
            return count;
        });
    }

    public static void withdraw(int itemId, int amount, int slot)
    {
        TPackets.sendClickPacket();
        if(amount == 1) {
            TPackets.sendWidgetActionPacket(1, WidgetInfo.BANK_ITEM_CONTAINER.getId(), slot, itemId);
        }
        else if(amount == 5) {
            TPackets.sendWidgetActionPacket(2, WidgetInfo.BANK_ITEM_CONTAINER.getId(), slot, itemId);
        }
        else if(amount == 10) {
            TPackets.sendWidgetActionPacket(3, WidgetInfo.BANK_ITEM_CONTAINER.getId(), slot, itemId);
        }
        else if(amount == -1) {
            TPackets.sendWidgetActionPacket(7, WidgetInfo.BANK_ITEM_CONTAINER.getId(), slot, itemId);
        }
        else {
            setX(amount);
            TPackets.sendWidgetActionPacket(4, WidgetInfo.BANK_ITEM_CONTAINER.getId(), slot, itemId);
        }
    }

    public static void deposit(int itemId, int amount, int slot)
    {
        TPackets.sendClickPacket();
        if(amount == 1) {
            TPackets.sendWidgetActionPacket(2, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), slot, itemId);
        }
        else if(amount == 5) {
            TPackets.sendWidgetActionPacket(3, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), slot, itemId);
        }
        else if(amount == 10) {
            TPackets.sendWidgetActionPacket(4, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), slot, itemId);
        }
        else if(amount == -1) {
            TPackets.sendWidgetActionPacket(7, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), slot, itemId);
        }
        else {
            TPackets.sendWidgetActionPacket(6, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), slot, itemId);
            TPackets.sendResumeCountDialoguePacket(amount);
            TClientScript.closeNumericInputDialogue();
        }
    }

    public static void deposit(String itemName, int amount) {
        List<Item> inventoryItems = TGame.invoke(() -> {
            ItemContainer inventory = Static.getClient().getItemContainer(InventoryID.INVENTORY);
            if(inventory == null)
                return null;

            return getItemsInventory();
        });

        if(inventoryItems == null)
            return;

        for (int i = 0; i < inventoryItems.size(); i++) {
            if (inventoryItems.get(i) == null ||
                    inventoryItems.get(i).getId() == 6512 ||
                    inventoryItems.get(i).getId() == -1 ||
                    !inventoryItems.get(i).getName().contains(itemName))
                continue;
            deposit(inventoryItems.get(i).getId(), amount, inventoryItems.get(i).getSlot());
            break;
        }
    }

    public static void deposit(int itemId, int amount) {
        List<Item> inventoryItems = TGame.invoke(() -> {
            ItemContainer inventory = Static.getClient().getItemContainer(InventoryID.INVENTORY);
            if(inventory == null)
                return null;

            return getItemsInventory();
        });

        if(inventoryItems == null)
            return;

        for (int i = 0; i < inventoryItems.size(); i++) {
            if (inventoryItems.get(i) == null ||
                    inventoryItems.get(i).getId() == 6512 ||
                    inventoryItems.get(i).getId() == -1 ||
                    inventoryItems.get(i).getId() != itemId)
                continue;
            deposit(inventoryItems.get(i).getId(), amount, inventoryItems.get(i).getSlot());
            break;
        }
    }

    public static void withdraw(int id, int amount, boolean noted) {
        setWithdrawMode(noted);
        List<Item> bankItems = getItems();

        if(bankItems == null || bankItems.isEmpty())
            return;

        for (int i = 0; i < bankItems.size(); i++) {
            if (bankItems.get(i) == null ||
                    bankItems.get(i).getId() == 6512 ||
                    bankItems.get(i).getId() == -1 ||
                    bankItems.get(i).getId() != id)
                continue;

            withdraw(bankItems.get(i).getId(), amount, i);
            break;
        }
    }

    public static void withdraw(String itemName, int amount, boolean noted) {
        setWithdrawMode(noted);
        List<Item> bankItems = getItems();

        if(bankItems == null || bankItems.isEmpty())
            return;

        for (int i = 0; i < bankItems.size(); i++) {
            if (bankItems.get(i) == null ||
                    bankItems.get(i).getId() == 6512 ||
                    bankItems.get(i).getId() == -1 ||
                    !bankItems.get(i).getName().contains(itemName))
                continue;

            withdraw(bankItems.get(i).getId(), amount, i);
            break;
        }
    }
}
