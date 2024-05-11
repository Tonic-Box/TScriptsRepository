package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.coords.WorldPoint;

public class WorldPointUtil
{
    public static int compress(WorldPoint point)
    {
        return compress(point.getX(), point.getY(), point.getPlane());
    }
    public static int compress(int x, int y, int z) {
        return x | y << 14 | z << 29;
    }

    public static short getCompressedX(int compressed)
    {
        return (short) (compressed & 0x3FFF);
    }

    public static short getCompressedY(int compressed)
    {
        return (short) ((compressed >>> 14) & 0x7FFF);
    }

    public static byte getCompressedPlane(int compressed)
    {
        return (byte)((compressed >>> 29) & 7);
    }

    public static WorldPoint fromCompressed(int compressed)
    {
        int x = compressed & 0x3FFF;
        int y = (compressed >>> 14) & 0x7FFF;
        int z = (compressed >>> 29) & 7;
        return new WorldPoint(x, y, z);
    }

    public static int dx(int compressed, int n)
    {
        return compressed + n;
    }

    public static int dy(int compressed, int n)
    {
        return compressed + (n << 14);
    }

    public static int dxy(int compressed, int nx, int ny)
    {
        return compressed + nx + (ny << 14);
    }
}
