package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.client.Static;

public class WorldPointUtil
{
    public static int fromCord(int x, int y)
    {
        return x << 14 | y;
    }

    public static WorldPoint fromCompressed(int point)
    {
        return new WorldPoint(getX(point), getY(point), Static.getClient().getPlane());
    }

    public static int getX(int point)
    {
        return point >> 14;
    }

    public static int getY(int point)
    {
        return point & 0x3FFF;
    }

    public static int dx(int point, int xAdditive)
    {
        return point + (xAdditive << 14);
    }

    public static int dy(int point, int yAdditive)
    {
        return point + yAdditive;
    }
}
