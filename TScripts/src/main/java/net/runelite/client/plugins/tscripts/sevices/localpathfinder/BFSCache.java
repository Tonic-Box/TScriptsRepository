package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import org.jetbrains.kotlin.gnu.trove.TIntIntHashMap;

import java.util.LinkedList;
import java.util.List;

public class BFSCache
{
    private final TIntIntHashMap cache = new TIntIntHashMap(200000);

    public boolean put(final int point, final int parent)
    {
        if(cache.contains(point))
            return false;
        cache.put(point, parent);
        return true;
    }

    public int get(final int position)
    {
        return cache.get(position);
    }

    public void clear()
    {
        cache.clear();
    }

    public int size()
    {
        return cache.size();
    }

    public List<Step> path(int pos, int startX, int startY)
    {

        int parent = get(shift(pos, startX, startY));
        LinkedList<Step> path = new LinkedList<>();
        path.add(0, new Step(shift(pos, startX, startY)));
        while(parent != -1)
        {
            pos = parent;
            parent = get(shift(pos, startX, startY));
            path.add(0, new Step(pos));
        }
        return path;
    }

    private int shift(int position, int startX, int startY)
    {
        int x = WorldPointUtil.getCompressedX(position) + startX;
        int y = WorldPointUtil.getCompressedY(position) + startY;
        return WorldPointUtil.compress(x, y, WorldPointUtil.getCompressedPlane(position));
    }
}