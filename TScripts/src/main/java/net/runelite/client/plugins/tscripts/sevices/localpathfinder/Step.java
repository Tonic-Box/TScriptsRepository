package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.library.TWorldPoint;

public class Step {
    public final int position;

    public WorldPoint getPosition()
    {
        return TWorldPoint.get(WorldPointUtil.fromCompressed(position));
    }

    public Step(int position) {
        this.position = position;
    }
}