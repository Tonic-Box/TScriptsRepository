package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.Client;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.library.TObjects;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.client.Static;
import org.jetbrains.kotlin.gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.List;

public class LocalPathfinder
{
    private static final LocalPathfinder instance = new LocalPathfinder();
    public static LocalPathfinder get()
    {
        return instance;
    }
    private CollisionUtil collisionUtil;
    private int target;
    private final BFSCache visited = new BFSCache();
    private final IntQueue queue = new IntQueue(5000);
    private LocalPathfinder()
    {
    }

    public List<Step> findPath(WorldPoint start, WorldPoint end)
    {
        collisionUtil = new CollisionUtil(getCollision());
        visited.setDoors(collisionUtil.getIgnoreTiles());

        target = WorldPointUtil.fromCord(end.getX(), end.getY());
        visited.clear();
        visited.put(WorldPointUtil.fromCord(start.getX(), start.getY()), -1);
        queue.clear();
        queue.enqueue(WorldPointUtil.fromCord(start.getX(), start.getY()));
        return bfs();
    }

    private List<Step> bfs()
    {
        int current;
        while(!queue.isEmpty())
        {
            current = queue.dequeue();
            if(current == target)
            {
                Logging.info("Nodes visited: " + visited.size());
                return visited.path(current);
            }
            addNeighbors(current);
        }
        return new ArrayList<>();
    }

    private void addNeighbors(int current)
    {
        int x = WorldPointUtil.getX(current);
        int y = WorldPointUtil.getY(current);

        if(!collisionUtil.blockedWest(x, y))
        {
            addNeighbor(current, WorldPointUtil.dx(current, -1));
        }

        if(!collisionUtil.blockedEast(x, y))
        {
            addNeighbor(current, WorldPointUtil.dx(current, 1));
        }

        if(!collisionUtil.blockedNorth(x, y))
        {
            addNeighbor(current, WorldPointUtil.dy(current, 1));
        }

        if(!collisionUtil.blockedSouth(x, y))
        {
            addNeighbor(current, WorldPointUtil.dy(current, -1));
        }

        if(!collisionUtil.blockedNorthWest(x, y))
        {
            addNeighbor(current, WorldPointUtil.dy(WorldPointUtil.dx(current, -1), 1));
        }

        if(!collisionUtil.blockedNorthEast(x, y))
        {
            addNeighbor(current, WorldPointUtil.dy(WorldPointUtil.dx(current, 1), 1));
        }

        if(!collisionUtil.blockedSouthWest(x, y))
        {
            addNeighbor(current, WorldPointUtil.dy(WorldPointUtil.dx(current, -1), -1));
        }

        if(!collisionUtil.blockedSouthEast(x, y))
        {
            addNeighbor(current, WorldPointUtil.dy(WorldPointUtil.dx(current, 1), -1));
        }
    }

    private void addNeighbor(final int node, final int neighbor) {
        if (visited.put(neighbor, node))
        {
            queue.enqueue(neighbor);
        }
    }

    private TIntIntHashMap getCollision()
    {
        Client client = Static.getClient();
        TIntIntHashMap collisionMap = new TIntIntHashMap();
        WorldView wv = client.getTopLevelWorldView();
        if(wv.getCollisionMaps() == null || wv.getCollisionMaps()[wv.getPlane()] == null)
            return collisionMap;

        int[][] flags = wv.getCollisionMaps()[wv.getPlane()].getFlags();
        WorldPoint point;
        for(int x = 0; x < flags.length; x++)
        {
            for(int y = 0; y < flags[x].length; y++)
            {
                point = WorldPoint.fromScene(client, x, y, wv.getPlane());
                collisionMap.put(WorldPointUtil.fromCord(point.getX(), point.getY()), flags[x][y]);
            }
        }
        return collisionMap;
    }
}
