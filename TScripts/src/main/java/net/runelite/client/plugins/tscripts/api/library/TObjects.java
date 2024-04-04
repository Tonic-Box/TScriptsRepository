package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.util.Compare;
import net.runelite.client.plugins.tscripts.util.cache.EntityCache;
import net.unethicalite.client.Static;

import java.util.Arrays;

public class TObjects
{
    public static void interact(TileObject object, int action)
    {
        WorldPoint worldPoint = object.getWorldLocation();
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendObjectActionPacket(action, object.getId(), worldPoint.getX(), worldPoint.getY(), false);
    }

    public static void interact(TileObject object, String action)
    {
        int actionIndex = Arrays.asList(object.getActions()).indexOf(action);
        if (actionIndex == -1)
            return;
        interact(object, actionIndex);
    }

    public static TileObject getObject(Object identifier)
    {
        if(identifier instanceof TileObject)
        {
            return (TileObject) identifier;
        }
        if(identifier instanceof Integer)
        {
            return EntityCache.get().objectStream()
                    .filter(o -> o.getId() == (int) identifier)
                    .min(Compare.DISTANCE).orElse(null);
        }
        else if (identifier instanceof String)
        {
            return EntityCache.get().objectStream()
                    .filter(o -> o.getName().equals(identifier))
                    .min(Compare.DISTANCE).orElse(null);
        }
        return null;
    }

    public static TileObject getObjectWithin(Object identifier, int distance)
    {
        if(identifier instanceof TileObject)
        {
            TileObject object = (TileObject) identifier;
            return object.distanceTo(Static.getClient().getLocalPlayer()) <= distance ? object : null;
        }
        if(identifier instanceof Integer)
        {
            return EntityCache.get().objectStream()
                    .filter(o -> o.getId() == (int) identifier && o.distanceTo(Static.getClient().getLocalPlayer()) <= distance)
                    .min(Compare.DISTANCE).orElse(null);
        }
        else if (identifier instanceof String)
        {
            return EntityCache.get().objectStream()
                    .filter(o -> o.getName().equals(identifier) && o.distanceTo(Static.getClient().getLocalPlayer()) <= distance)
                    .min(Compare.DISTANCE).orElse(null);
        }
        return null;
    }

    public static TileObject getObjectAt(Object identifier, int x, int y)
    {
        if(identifier instanceof Integer)
        {
            return EntityCache.get().objectStream()
                    .filter(o -> o.getId() == (int) identifier && o.getWorldLocation().getX() == x && o.getWorldLocation().getY() == y)
                    .findFirst().orElse(null);
        }
        else if (identifier instanceof String)
        {
            return EntityCache.get().objectStream()
                    .filter(o -> o.getName().equals(identifier) && o.getWorldLocation().getX() == x && o.getWorldLocation().getY() == y)
                    .findFirst().orElse(null);
        }
        return null;
    }
}
