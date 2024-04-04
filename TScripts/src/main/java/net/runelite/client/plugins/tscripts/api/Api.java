package net.runelite.client.plugins.tscripts.api;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.tscripts.api.library.TPackets;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

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

    public static NPC getNpc(Object identifier)
    {
        if(identifier instanceof NPC)
        {
            return (NPC) identifier;
        }
        NPC npc = null;
        if (identifier instanceof Integer)
        {
            npc =  new NPCQuery()
                    .filter(n -> n.getId() == (int)identifier)
                    .filter(n -> !n.isDead())
                    .filter(n -> isReachable(n.getWorldLocation()))
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());
        }
        else if (identifier instanceof String)
        {
            npc =  new NPCQuery()
                    .filter(n -> n.getName().equals(identifier))
                    .filter(n -> !n.isDead())
                    .filter(n -> isReachable(n.getWorldLocation()) || identifier.equals("Fishing spot"))
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());
        }
        return npc;
    }

    public static NPC getFreeNpc(Object identifier)
    {
        if(identifier instanceof NPC)
        {
            return (NPC) identifier;
        }
        NPC npc = null;
        if (identifier instanceof Integer)
        {
            npc = new NPCQuery().filter(n -> !n.isInteracting() || !(n.getIdlePoseAnimation() == n.getPoseAnimation() && n.getAnimation() == -1)
                            || (n.getInteracting() != null && n.getInteracting().getHealthScale() != -1))
                    .filter(n -> n.getId() == (int)identifier)
                    .filter(n -> !n.isDead())
                    .filter(n -> isReachable(n.getWorldLocation()))
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());
        }
        else if (identifier instanceof String)
        {
            if(identifier.equals("Fishing spot"))
            {
                return getNpc(identifier);
            }
            npc = new NPCQuery().filter(n -> !n.isInteracting() || !(n.getIdlePoseAnimation() == n.getPoseAnimation() && n.getAnimation() == -1)
                            || (n.getInteracting() != null && n.getInteracting().getHealthScale() != -1))
                    .filter(n -> n.getName().equals(identifier))
                    .filter(n -> !n.isDead())
                    .filter(n -> isReachable(n.getWorldLocation()))
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());
        }
        return npc;
    }

    public static boolean isReachable(WorldPoint point)
    {
        WorldPoint player = Static.getClient().getLocalPlayer().getWorldLocation();
        return Walker.canPathTo(player, point);
    }

    public static boolean isInCombat(Actor actor)
    {
        return !isIdle(actor) || actor.getInteracting() != null ||
                (actor.getInteracting() != null && !actor.getInteracting().isDead());
    }

    public static boolean isIdle(Actor actor)
    {
        return (actor.getIdlePoseAnimation() == actor.getPoseAnimation() && actor.getAnimation() == -1);
    }

    /**
     * Generic continue any pause dialogue
     * @return true if there was a dialogue to continue
     */
    public static boolean continueDialogue() {
        return invoke(() -> {
            if (Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 5) != null) {
                net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.PACK(WidgetID.DIALOG_NPC_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(633, 0) != null) {
                net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.PACK(633, 0), -1);
                return true;
            }
            else if (Widgets.get(WidgetID.DIALOG_PLAYER_GROUP_ID, 5) != null) {
                net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.PACK(WidgetID.DIALOG_PLAYER_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG_SPRITE) != null) {
                net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_SPRITE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG2_SPRITE) != null) {
                net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.DIALOG2_SPRITE_CONTINUE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.MINIGAME_DIALOG_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE.getId(), -1);
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

    public static TileItem getTileItem(Object identifier)
    {
        if(identifier instanceof TileItem)
        {
            return (TileItem) identifier;
        }
        if(identifier instanceof Integer)
        {
            return TileItems.query()
                    .filter(o -> o.getId() == (int) identifier)
                    .results().nearest();
        }
        else if (identifier instanceof String)
        {
            return TileItems.query()
                    .filter(o -> o.getName().equals(identifier))
                    .results().nearest();
        }
        return null;
    }

    public static TileItem getTileItemAt(Object identifier, int x, int y)
    {
        if(identifier instanceof Integer)
        {
            return TileItems.query()
                    .filter(o -> o.getId() == (int) identifier)
                    .filter(o -> o.getWorldLocation().getX() == x && o.getWorldLocation().getY() == y)
                    .results().nearest();
        }
        else if (identifier instanceof String)
        {
            return TileItems.query()
                    .filter(o -> o.getName().equals(identifier))
                    .filter(o -> o.getWorldLocation().getX() == x && o.getWorldLocation().getY() == y)
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