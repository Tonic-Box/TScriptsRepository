package net.runelite.client.plugins.tscripts.api;

import net.runelite.api.*;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class contains a variety of helper methods for use in out method definitions
 */
public class Api
{

    public static <T> T invoke(Supplier<T> supplier)
    {
        if (!Static.getClient().isClientThread())
        {
            CompletableFuture<T> future = new CompletableFuture<>();
            Runnable runnable = () -> future.complete(supplier.get());
            Static.getClientThread().invoke(runnable);
            return future.join();
        } else
        {
            return supplier.get();
        }
    }

    public static void tick(int length)
    {
        int next = Static.getClient().getTickCount() + length;
        while(Static.getClient().getTickCount() < next && Static.getClient().getTickCount() != 0)
        {
            if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
            {
                return;
            }
            try
            {
                Thread.sleep(50);
            }
            catch (Exception ex) {
                Logging.errorLog(ex);
            }
        }
    }

    public static void waitUntilIdle()
    {
        if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
        {
            return;
        }
        tick(1);
        Player player = Static.getClient().getLocalPlayer();
        while(!invoke(player::isIdle))
        {
            if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
            {
                return;
            }
            tick(1);
        }
    }

    public static void waitUntilOnTile(int worldX, int worldY)
    {
        Player player = Static.getClient().getLocalPlayer();
        while(player.getWorldLocation().getX() != worldX || player.getWorldLocation().getY() != worldY)
        {
            if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
            {
                return;
            }
            tick(1);
        }
    }

    public static Item getItem(Object identifier)
    {
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

    public static NPC getNpc(Object identifier)
    {
        NPC npc = null;
        if (identifier instanceof Integer)
        {
            npc = NPCs.getNearest((int)identifier);
        }
        else if (identifier instanceof String)
        {
            npc = NPCs.getNearest((String)identifier);
        }
        return npc;
    }

    public static NPC getFreeNpc(Object identifier)
    {
        NPC npc = null;
        if (identifier instanceof Integer)
        {
            npc = new NPCQuery().filter(n -> !n.isInteracting() || !(n.getIdlePoseAnimation() == n.getPoseAnimation() && n.getAnimation() == -1)
                    || (n.getInteracting() != null && n.getInteracting().getHealthScale() != -1))
                    .filter(n -> n.getId() == (int)identifier)
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());
        }
        else if (identifier instanceof String)
        {
            npc = new NPCQuery().filter(n -> !n.isInteracting() || !(n.getIdlePoseAnimation() == n.getPoseAnimation() && n.getAnimation() == -1)
                            || (n.getInteracting() != null && n.getInteracting().getHealthScale() != -1))
                    .filter(n -> n.getName().equals(identifier))
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());
        }
        return npc;
    }

    /**
     * Generic continue any pause dialogue
     * @return true if there was a dialogue to continue
     */
    public static boolean continueDialogue() {
        return invoke(() -> {
            if (Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 5) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.PACK(WidgetID.DIALOG_NPC_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(633, 0) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.PACK(633, 0), -1);
                return true;
            }
            else if (Widgets.get(WidgetID.DIALOG_PLAYER_GROUP_ID, 5) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.PACK(WidgetID.DIALOG_PLAYER_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG_SPRITE) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_SPRITE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG2_SPRITE) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.DIALOG2_SPRITE_CONTINUE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    TPackets.sendResumePauseWidget(WidgetInfo.MINIGAME_DIALOG_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    TPackets.sendResumePauseWidget(WidgetInfo.LEVEL_UP_CONTINUE.getId(), -1);
                    return true;
                }
            }
            return false;
        });
    }

    public static boolean isDialogueOpen() {
        if (Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 5) != null) {
            return true;
        }
        else if (Widgets.get(633, 0) != null) {
            return true;
        }
        else if (Widgets.get(WidgetID.DIALOG_PLAYER_GROUP_ID, 5) != null) {
            return true;
        }
        else if (Widgets.get(WidgetInfo.DIALOG_SPRITE) != null) {
            return true;
        }
        else if (Widgets.get(WidgetInfo.DIALOG2_SPRITE) != null) {
            return true;
        }
        else if (Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE) != null) {
            Widget w = Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE);
            return w != null && w.getText() != null && w.getText().equals("Click here to continue");
        }
        else if (Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE) != null) {
            Widget w = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
            return w != null && w.getText() != null && w.getText().equals("Click here to continue");
        }
        else if (Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE) != null) {
            Widget w = Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE);
            return w != null && w.getText() != null && w.getText().equals("Click here to continue");
        }
        return false;
    }

    public static Spell getSpell(String spellName) {
        SpellBook spellbook = SpellBook.getCurrent();
        if(spellbook == null)
            return null;

        Spell spell;

        switch(spellbook)
        {
            case STANDARD:
                spell = Api.getSpell(SpellBook.Standard.class, spellName);
                break;
            case ANCIENT:
                spell = Api.getSpell(SpellBook.Ancient.class, spellName);
                break;
            case LUNAR:
                spell = Api.getSpell(SpellBook.Lunar.class, spellName);
                break;
            default:
                return null;
        }
        return spell;
    }

    private static <T extends Enum<T> & Spell> Spell getSpell(Class<T> spellEnum, String spellName) {
        spellName = spellName.toUpperCase().replace(" ", "_");
        for (T spell : spellEnum.getEnumConstants()) {
            if (spell.name().equalsIgnoreCase(spellName)) {
                return spell;
            }
        }
        return null;
    }

    public static TileObject getObject(Object identifier)
    {
        if(identifier instanceof Integer)
        {
            return TileObjects.query()
                    .filter(o -> o.getId() == (int) identifier)
                    .results().nearest();
        }
        else if (identifier instanceof String)
        {
            return TileObjects.query()
                    .filter(o -> o.getName().equals(identifier))
                    .results().nearest();
        }
        return null;
    }

    public static <T> EventBus.Subscriber register(Class<T> event, Consumer<T> callback)
    {
        return Static.getEventBus().register(event, callback, 0);
    }

    public static void unregister(List<EventBus.Subscriber> subs)
    {
        for(EventBus.Subscriber sub : subs)
        {
            Static.getEventBus().unregister(sub);
        }
    }
}