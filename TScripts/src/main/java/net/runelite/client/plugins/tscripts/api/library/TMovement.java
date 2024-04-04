package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.client.Static;

public class TMovement
{
    public static void walkTo(int worldX, int worldY)
    {
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendWalkPacket(worldX, worldY, false);
    }
    public static void walkTo(WorldPoint point)
    {
        walkTo(point.getX(), point.getY());
    }
    public static boolean isReachable(WorldPoint point)
    {
        WorldPoint player = Static.getClient().getLocalPlayer().getWorldLocation();
        return Walker.canPathTo(player, point);
    }
}
