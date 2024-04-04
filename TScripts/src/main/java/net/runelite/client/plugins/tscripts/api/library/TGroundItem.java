package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.TileItem;
import net.unethicalite.api.entities.TileItems;

import java.util.Arrays;

public class TGroundItem
{
    public static void interact(TileItem item, int action)
    {
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendGroundItemActionPacket(action, item.getId(), item.getWorldLocation().getX(), item.getWorldLocation().getY(), false);
    }

    public static void interact(TileItem item, String action)
    {
        int actionIndex = Arrays.asList(item.getActions()).indexOf(action);
        if (actionIndex == -1)
            return;
        interact(item, actionIndex);
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
}
