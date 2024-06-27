package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.library.TWorldPoint;

@Getter
public class Step {
    private final int position;
    private final boolean doored;

    public WorldPoint getPosition()
    {
        return TWorldPoint.get(WorldPointUtil.fromCompressed(position));
    }

    public Step(int position, boolean door) {
        this.position = position;
        this.doored = door;
    }
}