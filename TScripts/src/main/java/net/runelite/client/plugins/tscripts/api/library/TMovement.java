package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.client.Static;

public class TMovement
{
    public static void walkTo(int worldX, int worldY)
    {
        WorldPoint wp = TWorldPoint.translate(new WorldPoint(worldX, worldY, Static.getClient().getPlane()));
        TPackets.sendClickPacket(-1, -1);
        TPackets.sendWalkPacket(wp.getX(), wp.getY(), false);
    }
    public static void walkTo(WorldPoint point)
    {
        walkTo(point.getX(), point.getY());
    }
    public static boolean isReachable(WorldPoint point)
    {
        point = TWorldPoint.translate(point);
        WorldPoint player = TWorldPoint.translate(Static.getClient().getLocalPlayer().getWorldLocation());
        return Walker.canPathTo(player, point);
    }

    public static boolean inArea(WorldPoint p1, WorldPoint p2)
    {
        return inArea(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public static boolean inArea(int x1, int y1, int x2, int y2)
    {
        Player player = Static.getClient().getLocalPlayer();
        if(player == null)
            return false;

        WorldPoint wp = player.getWorldLocation();

        return wp.getX() >= x1 && wp.getX() <= x2 && wp.getY() >= y1 && wp.getY() <= y2;
    }
}
