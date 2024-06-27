package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import org.jetbrains.kotlin.gnu.trove.TIntArrayList;
import org.jetbrains.kotlin.gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BFSCache
{
    private final TIntIntHashMap cache = new TIntIntHashMap(200000);
    private final List<Integer> doors = new ArrayList<>();

    public void setDoors(List<Integer> doors)
    {
        this.doors.addAll(doors);
    }

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

    public List<Step> path(int pos)
    {
        int parent = get(pos);
        LinkedList<Step> path = new LinkedList<>();
        path.add(0, new Step(pos, doors.contains(pos)));
        while(parent != -1)
        {
            pos = parent;
            parent = get(pos);
            path.add(0, new Step(pos, doors.contains(pos)));
        }
        return path;
    }
}