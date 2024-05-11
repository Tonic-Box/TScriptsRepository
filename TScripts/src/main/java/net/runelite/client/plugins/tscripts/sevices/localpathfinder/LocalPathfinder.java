package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.CollisionData;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TObjects;
import net.runelite.client.plugins.tscripts.sevices.TileOverlay;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.List;

public class LocalPathfinder
{
    private int[][] collisionMap;
    private int boundsRadius;
    private int startX;
    private int startY;
    private int target;
    private TileOverlay overlay;

    public List<Step> pathTo(int boundsRadius, WorldPoint destination, List<WorldPoint> filter, TileOverlay overlay)
    {
        this.overlay = overlay;
        this.boundsRadius = boundsRadius;
        this.collisionMap = collisionMap();
        final BFSCache visited = new BFSCache();
        int x;
        int y;
        int plane;
        for(WorldPoint point : filter)
        {
            x = point.getX() - startX;
            y = point.getY() - startY;
            plane = point.getPlane();
            visited.put(WorldPointUtil.compress(x, y, plane), -1);
        }
        final IntQueue queue = new IntQueue(5000);

        WorldPoint point = Static.getClient().getLocalPlayer().getWorldLocation();
        x = point.getX() - startX;
        y = point.getY() - startY;
        plane = point.getPlane();

        System.out.println("Start: " + x + ", " + y + ", " + plane);
        queue.enqueue(WorldPointUtil.compress(x, y, plane));
        visited.put(WorldPointUtil.compress(x, y, plane), -1);
        return bfs(visited, queue, destination);
    }

    private List<Step> bfs(final BFSCache visited, final IntQueue queue, WorldPoint targetWorldPont)
    {
        int current;
        overlay.setStart(new Step(WorldPointUtil.compress(targetWorldPont)));
        int targetX = targetWorldPont.getX() - startX;
        int targetY = targetWorldPont.getY() - startY;
        int target = WorldPointUtil.compress(targetX, targetY, targetWorldPont.getPlane());
        this.target = target;
        System.out.println("Target: " + targetX + ", " + targetY + ", " + targetWorldPont.getPlane());
        while(!queue.isEmpty())
        {
            current = queue.dequeue();
            if(current == target)
            {
                Logging.info("Nodes visited: " + visited.size());
                return visited.path(current, startX, startY);
            }
            addNeighbors(current, queue, visited);
        }
        return new ArrayList<>();
    }

    private void addNeighbors(int node, IntQueue queue, BFSCache visited)
    {
        final short x = WorldPointUtil.getCompressedX(node);
        final short y = WorldPointUtil.getCompressedY(node);
        final byte plane = WorldPointUtil.getCompressedPlane(node);

        if(x < 0 || y < 0 || x >= collisionMap.length || y >= collisionMap[0].length)
            return;

        final byte flags = FlagUtil.all(collisionMap, x, y);

        switch (flags)
        {
            case Flags.ALL:
                addNeighbor(node, WorldPointUtil.compress(x - 1, y, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x + 1, y, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x, y - 1, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x, y + 1, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x - 1, y - 1, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x + 1, y - 1, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x - 1, y + 1, plane), queue, visited);
                addNeighbor(node, WorldPointUtil.compress(x + 1, y + 1, plane), queue, visited);
                return;
            case Flags.NONE:
                return;
        }

        if ((flags & Flags.WEST) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x - 1, y, plane), queue, visited);
        }

        if ((flags & Flags.EAST) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x + 1, y, plane), queue, visited);
        }

        if ((flags & Flags.SOUTH) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x, y - 1, plane), queue, visited);
        }

        if ((flags & Flags.NORTH) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x, y + 1, plane), queue, visited);
        }

        if ((flags & Flags.SOUTHWEST) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x - 1, y - 1, plane), queue, visited);
        }

        if ((flags & Flags.SOUTHEAST) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x + 1, y - 1, plane), queue, visited);
        }

        if ((flags & Flags.NORTHWEST) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x - 1, y + 1, plane), queue, visited);
        }

        if ((flags & Flags.NORTHEAST) != 0) {
            addNeighbor(node, WorldPointUtil.compress(x + 1, y + 1, plane), queue, visited);
        }
    }

    private void addNeighbor(final int node, final int neighbor, final IntQueue queue, final BFSCache visited) {
        if (visited.put(neighbor, node))
        {
            int x = WorldPointUtil.getCompressedX(neighbor);
            int y = WorldPointUtil.getCompressedY(neighbor);
            int plane = WorldPointUtil.getCompressedPlane(neighbor);
            System.out.println("Neighbor: " + x + ", " + y + ", " + plane + " - " + neighbor + " // " + target);
            overlay.addStep(new Step(WorldPointUtil.compress(startX + x, startY + y, plane)));
            queue.enqueue(neighbor);
            TDelay.tick(1);
        }
    }

    //z,x,y
    private int[][] collisionMap() {
        int[][] collisionMap = new int[boundsRadius * 2 + 1][boundsRadius * 2 + 1];

        CollisionData[] collisionMaps = Static.getClient().getCollisionMaps();
        if (collisionMaps == null) {
            return collisionMap;
        }

        WorldPoint local = Static.getClient().getLocalPlayer().getWorldLocation();

        startX = local.getX() - boundsRadius;
        startY = local.getY() - boundsRadius;
        int endX = local.getX() + boundsRadius;
        int endY = local.getY() + boundsRadius;
        TileObject door;

        for (int x = startX; x < endX; x++)
        {
            if(x >= collisionMaps[0].getFlags().length)
                continue;
            for (int y = startY; y < endY; y++)
            {
                if(y >= collisionMaps[0].getFlags()[x].length)
                    continue;
                door = TObjects.getObject("Door");
                if(door != null && door.getWorldLocation().getX() == x && door.getWorldLocation().getY() == y)
                    collisionMap[x - startX][y - startY] = 0;
                else
                    collisionMap[x - startX][y - startY] = collisionMaps[0].getFlags()[x][y];
            }
        }
        return collisionMap;
    }
}
