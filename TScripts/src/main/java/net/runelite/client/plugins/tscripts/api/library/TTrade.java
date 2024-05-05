package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.List;

public class TTrade
{
    public static void offer(Object identifier, int amount)
    {
        Item item = TInventory.getItem(identifier);
        if(item == null)
            return;

        int type;

        switch(amount)
        {
            case 1:
                type = 0;
                break;
            case 5:
                type = 1;
                break;
            case 10:
                type = 2;
                break;
            case -1:
                type = 3;
                break;
            default:
                type = 4;
                break;
        }

        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(type, 22020096, item.getSlot(), item.getId());

        if(type == 4)
        {
            TDelay.tick(1);
            TPackets.sendResumeCountDialoguePacket(amount);
            TClientScript.closeNumericInputDialogue();
        }
    }

    public static void remove(Object identifier, int amount)
    {
        Item item = getOfferedItem(identifier);
        if(item == null)
            return;

        int type;

        switch(amount)
        {
            case 1:
                type = 0;
                break;
            case 5:
                type = 1;
                break;
            case 10:
                type = 2;
                break;
            case -1:
                type = 3;
                break;
            default:
                type = 4;
                break;
        }

        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(type, 21954585, item.getSlot(), item.getId());

        if(type == 4)
        {
            TDelay.tick(1);
            TPackets.sendResumeCountDialoguePacket(amount);
            TClientScript.closeNumericInputDialogue();
        }
    }

    public static Item getOfferedItem(Object identifier)
    {
        int id = -1;
        String name = null;
        if(identifier instanceof Item)
        {
            id = ((Item) identifier).getId();
        }
        else if(identifier instanceof Integer)
        {
            id = (int) identifier;
        }
        else if(identifier instanceof String)
        {
            name = (String) identifier;
        }

        List<Item> items = getOfferedItems();
        int i = 0;
        ItemComposition composition;
        for(Item item : items)
        {
            item.setSlot(i++);
            if(item.getId() == -1)
                continue;

            if(id != -1 && item.getId() == id)
                return item;
            else if (name != null)
            {
                composition = Static.getItemManager().getItemComposition(item.getId());
                if(composition.getName().equals(name))
                    return item;
            }
        }
        return null;
    }

    public static List<Item> getOfferedItems()
    {
        return TGame.invoke(() -> {
            List<Item> items = new ArrayList<>();
            Widget tradeWindow = Static.getClient().getWidget(335, 25);
            if(tradeWindow == null)
                return items;
            for(Widget widget : tradeWindow.getDynamicChildren())
            {
                if(widget.getItemId() != -1)
                {
                    items.add(new Item(widget.getItemId(), widget.getItemQuantity()));
                }
            }
            return items;
        });
    }

    public static void accept()
    {
        if(firstWindowOpen())
        {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 21954570, -1, -1);
        }
        else if(secondWindowOpen())
        {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 21889037, -1, -1);
        }
    }

    public static void decline()
    {
        if(firstWindowOpen())
        {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 21954573, -1, -1);
        }
        else if(secondWindowOpen())
        {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 21889038, -1, -1);
        }
    }

    public static boolean firstWindowOpen()
    {
        return TGame.invoke(() -> {
            Widget tradeWindow = Static.getClient().getWidget(WidgetInfo.TRADE_WINDOW_HEADER);
            return tradeWindow != null && !tradeWindow.isHidden();
        });
    }

    public static boolean secondWindowOpen()
    {
        return TGame.invoke(() -> {
            Widget tradeWindow = Static.getClient().getWidget(334, 4);
            return tradeWindow != null && !tradeWindow.isHidden();
        });
    }
}
