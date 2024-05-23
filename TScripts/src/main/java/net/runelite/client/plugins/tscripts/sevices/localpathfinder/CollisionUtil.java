package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.client.plugins.tscripts.sevices.cache.GameCache;
import org.jetbrains.kotlin.gnu.trove.TIntIntHashMap;
import java.util.*;
import java.util.stream.Collectors;

public class CollisionUtil
{
    private final TIntIntHashMap collisionMap;
    private final List<Integer> ignoreTiles;
    CollisionUtil(TIntIntHashMap collisionMap)
    {
        this.collisionMap = collisionMap;
        this.ignoreTiles = getDoored();
    }

    public boolean blockedNorth(int x, int y)
    {
        if(isDoored(x, y) || isDoored(x, y + 1))
            return false;
        return CollisionPredicates.BLOCKED_NORTH.test(getFlags(x, y)) || CollisionPredicates.BLOCKED_SOUTH.test(getFlags(x, y + 1));
    }

    public boolean blockedEast(int x, int y)
    {
        if (isDoored(x, y) || isDoored(x + 1, y))
            return false;
        return CollisionPredicates.BLOCKED_EAST.test(getFlags(x, y)) || CollisionPredicates.BLOCKED_WEST.test(getFlags(x + 1, y));
    }

    public boolean blockedSouth(int x, int y)
    {
        if (isDoored(x, y) || isDoored(x, y - 1))
            return false;
        return CollisionPredicates.BLOCKED_SOUTH.test(getFlags(x, y)) || CollisionPredicates.BLOCKED_NORTH.test(getFlags(x, y - 1));
    }

    public boolean blockedWest(int x, int y)
    {
        if (isDoored(x, y) || isDoored(x - 1, y))
            return false;
        return CollisionPredicates.BLOCKED_WEST.test(getFlags(x, y)) || CollisionPredicates.BLOCKED_EAST.test(getFlags(x - 1, y));
    }

    public boolean blockedNorthEast(int x, int y)
    {
        return blockedNorth(x, y) || blockedEast(x, y) || blockedSouth(x, y + 1) || blockedWest(x + 1, y) || blockedSouth(x + 1, y + 1) || blockedWest(x + 1, y + 1);
    }

    public boolean blockedSouthEast(int x, int y)
    {
        return blockedSouth(x, y) || blockedEast(x, y) || blockedNorth(x, y - 1) || blockedWest(x + 1, y) || blockedNorth(x + 1, y - 1) || blockedWest(x + 1, y - 1);
    }

    public boolean blockedSouthWest(int x, int y)
    {
        return blockedSouth(x, y) || blockedWest(x, y) || blockedNorth(x, y - 1) || blockedEast(x - 1, y) || blockedNorth(x - 1, y - 1) || blockedEast(x - 1, y - 1);
    }

    public boolean blockedNorthWest(int x, int y)
    {
        return blockedNorth(x, y) || blockedWest(x, y) || blockedSouth(x, y + 1) || blockedEast(x - 1, y) || blockedSouth(x - 1, y + 1) || blockedEast(x - 1, y + 1);
    }

    private Set<MovementFlag> getFlags(int x, int y)
    {
        return MovementFlag.getSetFlags(collisionMap.get(x << 14 | y));
    }

    private boolean isDoored(int x, int y)
    {
        int packed = WorldPointUtil.fromCord(x, y);
        return ignoreTiles.contains(packed);
    }

    private List<Integer> getDoored()
    {
        return GameCache.get().objectStream()
                .filter(o -> {
                    String name = o.getName().toLowerCase();
                    return (name.contains("door") || name.contains("gate"));
                })
                .map(door -> WorldPointUtil.fromCord(door.getWorldLocation().getX(), door.getWorldLocation().getY()))
                .collect(Collectors.toList());
    }
}
