package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
public class Step
{
    public final int position;
    public boolean checkpoint = false;

    public WorldPoint getPosition()
    {
        return WorldPointUtil.fromCompressed(position);
    }
}
