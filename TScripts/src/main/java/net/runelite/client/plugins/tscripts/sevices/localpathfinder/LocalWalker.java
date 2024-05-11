package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.unethicalite.api.movement.pathfinder.Walker;

import java.util.List;

public class LocalWalker
{
    public static void walkTo(int boundsRadius, WorldPoint destination, List<WorldPoint> filter)
    {
        LocalPathfinder pathfinder = new LocalPathfinder();
        List<Step> path = pathfinder.pathTo(boundsRadius, destination, filter, null);

        for(Step step : path)
        {
            Walker.walkTo(step.getPosition());
            TDelay.tick(1);
        }
    }
}
