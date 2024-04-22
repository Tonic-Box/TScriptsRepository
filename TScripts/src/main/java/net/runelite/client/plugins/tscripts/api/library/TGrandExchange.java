package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.tscripts.api.enums.GrandExchangeSlot;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.client.Static;

import java.awt.*;

public class TGrandExchange
{
    /**
     * start a fully automated buy offer
     * @param id item id
     * @param quantity amount
     */
    public static void buy(int id, int quantity, boolean noted)
    {
        int slotNumber = freeSlot();
        if(slotNumber == -1)
            return;
        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
        {
            Logging.logToEditor("Failed to buy '" + id + "' from the ge. No free slots.", Color.YELLOW);
            return;
        }

        int percents = 5;
        while(!buyNow(id, quantity, slot, percents))
        {
            percents += 5;
        }
        TClientScript.closeNumericInputDialogue();
        TDelay.tick(1);
        collectFromSlot(slotNumber, noted, quantity);
        TDelay.tick(1);
    }

    private static boolean buyNow(int id, int quantity, GrandExchangeSlot slot, int percents)
    {
        startBuyOfferPercentage(id, quantity, percents, slot.getSlot());
        int timeout = 3;
        while(!slot.isDone() && timeout-- > 0)
        {
            bypassHighOfferWarning();
            TDelay.tick(1);
        }

        if(!slot.isDone())
        {
            cancel(slot);
            return false;
        }
        return true;
    }

    public static void cancel(GrandExchangeSlot slot)
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, slot.getId(), 2, -1);
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, 30474263, 0, -1);
        TDelay.tick(2);
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, 30474264, 2, 995);
    }

    public static void bypassHighOfferWarning()
    {
        if(TGame.invoke(() -> Static.getClient().getWidget(289, 8) != null && !Static.getClient().getWidget(289, 8).isHidden()))
        {
            TPackets.sendClickPacket();
            TPackets.sendResumeCountDialogue(1);
            TDelay.tick(1);
        }
    }

    public static int buy(int id, int quantity, int price, boolean noted)
    {
        GrandExchangeSlot slot = startBuyOffer(id, quantity, price);
        if(slot == null)
        {
            Logging.logToEditor("Failed to buy '" + id + "' fromt he ge. No free slots.", Color.YELLOW);
            return -1;
        }
        while(!slot.isDone())
        {
            bypassHighOfferWarning();
            TDelay.tick(1);
        }
        TClientScript.closeNumericInputDialogue();
        return slot.getSlot();
    }

    /**
     * invoke a buy offer
     * @param itemId item id
     * @param amount amount
     * @param price price
     * @return slot number
     */
    public static GrandExchangeSlot startBuyOffer(int itemId, int amount, int price)
    {
        int slotNumber = freeSlot();
        if(slotNumber == -1)
            return null;
        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
            return null;
        TGame.invoke(() -> {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, slot.getId(), slot.getBuyChild(), -1);
            TPackets.sendResumeObjectDialogue(itemId);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474265, 12, -1);
            TPackets.sendResumeCountDialogue(price);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474265, 7, -1);
            TPackets.sendResumeCountDialogue(amount);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474269, -1, -1);
        });
        return slot;
    }

    /**
     * start a buy offer priced by a # of 5%s
     * @param itemId item id
     * @param amount amount
     * @param FivePercents price
     * @param slotNumber slot
     * @return slot number
     */
    public static int startBuyOfferPercentage(int itemId, int amount, int FivePercents, int slotNumber)
    {
        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
            return -1;
        TGame.invoke(() -> {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, slot.getId(), slot.getBuyChild(), -1);
            TPackets.sendResumeObjectDialogue(itemId);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474265, 7, -1);
            TPackets.sendResumeCountDialogue(amount);
        });
        int ticker;
        if(FivePercents < 0) {
            ticker = 10;
            FivePercents = FivePercents * -1;
        }
        else {
            ticker = 13;
        }
        int finalFivePercents = FivePercents;
        TGame.invoke(() -> {
            for(int i = finalFivePercents; i > 0; i--) {
                TPackets.sendWidgetActionPacket(0, 30474265, ticker, 65535);
            }
            TPackets.sendWidgetActionPacket(0, 30474269, 65535, 65535);
        });
        TDelay.tick(finalFivePercents/10);
        //api.executeIn(1, () -> api.getCs2().closeNumericInputDialogue());
        return slotNumber;
    }

    /**
     * fully automated sell offer
     * @param id item id
     * @param quantity amount
     * @param immediate attempt to insta sell
     */
    public static void sell(int id, int quantity, boolean immediate)
    {
        int slotNumber = freeSlot();
        if(slotNumber == -1)
            return;

        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
        {
            Logging.logToEditor("Failed to sell '" + id + "' to the ge. No free slots.", Color.YELLOW);
            return;
        }

        startSellOfferPercentage(id, quantity, ((immediate)?-15:-1), slotNumber);
        if(immediate)
        {
            int timeout = 5;
            while(!slot.isDone() && timeout-- > 0)
            {
                bypassHighOfferWarning();
                TDelay.tick(1);
            }
            if(slot.isDone())
            {
                collectFromSlot(slotNumber, true, quantity);
            }
        }
    }

    public static int sell(int id, int quantity, int price)
    {
        int slotNumber = freeSlot();
        if(slotNumber == -1)
            return -1;

        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
        {
            Logging.logToEditor("Failed to sell '" + id + "' to the ge. No free slots.", Color.YELLOW);
            return -1;
        }

        return startSellOffer(id, quantity, price);
    }

    /**
     * start a sell offer priced by a # of 5%s
     * @param itemId item id
     * @param amount amount
     * @param FivePercents price
     * @param slotNumber slot
     */
    public static void startSellOfferPercentage(int itemId, int amount, int FivePercents, int slotNumber)
    {
        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
            return;
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, slot.getId(), slot.getSellChild(), -1);
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER.getId(), getItemSlot(itemId), itemId);
        //Delays.tick(client, 1);
        int ticker;
        if(FivePercents < 0) {
            ticker = 10;
            FivePercents = FivePercents * -1;
        }
        else {
            ticker = 13;
        }
        int finalFivePercents = FivePercents;
        TGame.invoke(() -> {
            for(int i = finalFivePercents; i > 0; i--) {
                TPackets.sendWidgetActionPacket(0, 30474265, ticker, 65535);
            }
            if(amount != -1)
            {
                TPackets.sendClickPacket();
                TPackets.sendWidgetActionPacket(0, 30474265, 7, 65535);
                TPackets.sendResumeCountDialogue(amount);
            }
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474269, 65535, 65535);
        });
        TClientScript.closeNumericInputDialogue();
    }

    /**
     * invoke a sell offer
     * @param itemId item id
     * @param amount amount
     * @param price price
     * @return slot number
     */
    public static int startSellOffer(int itemId, int amount, int price)
    {
        int slotNumber = freeSlot();
        if(slotNumber == -1)
            return slotNumber;
        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
            return -1;
        TGame.invoke(() -> {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, slot.getId(), slot.getSellChild(), 65535);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER.getId(), getItemSlot(itemId), itemId);

            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474265, 12, 65535);
            TPackets.sendResumeCountDialogue(price);
            if(amount != -1)
            {
                TPackets.sendClickPacket();
                TPackets.sendWidgetActionPacket(0, 30474265, 7, 65535);
                TPackets.sendResumeCountDialogue(price);
            }
            TPackets.sendWidgetActionPacket(0, 30474269, 65535, 65535);
        });
        return slotNumber;
    }

    /**
     * get slot by item id
     * @param id item id
     * @return slot
     */
    public static int getItemSlot(int id)
    {
        Item item = TInventory.getItem(id);
        if(item == null)
            return -1;
        return item.getSlot();
    }

    /**
     * get a free slot
     * @return slot
     */
    public static int freeSlot()
    {
        GrandExchangeOffer[] offers = TGame.invoke(Static.getClient()::getGrandExchangeOffers);
        for (int slot = 0; slot < 8; slot++) {
            if (offers[slot] == null || offers[slot].getState() == GrandExchangeOfferState.EMPTY)
            {
                return slot+1;
            }
        }

        return -1;
    }

    /**
     * collect results from a slot
     * @param slotNumber slot
     */
    public static void collectFromSlot(int slotNumber, boolean noted, int amount)
    {
        GrandExchangeSlot slot = GrandExchangeSlot.getBySlot(slotNumber);
        if(slot == null)
            return;
        TGame.invoke(() -> {
            int n = noted ? 0 : 1;
            if(amount == 1)
            {
                n = noted ? 1 : 0;
            }
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, slot.getId(), 2, 65535);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(n, 30474264, 2, 2572);
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 30474264, 3, 995);
        });
    }

    public static boolean isOpen()
    {
        return TGame.invoke(() -> {
            Widget setupWindow = Static.getClient().getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER);
            return setupWindow != null && !setupWindow.isHidden();
        });
    }

    public static void collectAll()
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, 30474246, 0, -1);
    }
}
