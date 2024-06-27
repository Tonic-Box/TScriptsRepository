package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import lombok.Getter;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TObjects;
import net.runelite.client.plugins.tscripts.util.Location;
import net.runelite.client.plugins.tscripts.util.ThreadPool;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.client.Static;

import java.util.List;
import java.util.Random;

public class TWalker
{
    @Getter
    private final List<Step> path;
    private final Random random = new Random();
    public TWalker(List<Step> path)
    {
        this.path = path;
    }

    public TWalker(WorldPoint dest)
    {
        this.path = LocalPathfinder.get().findPath(Static.getClient().getLocalPlayer().getWorldLocation(), dest);
    }

    public void walkThreaded()
    {
        ThreadPool.submit(() -> {
            while(walk())
            {
                TDelay.tick(1);
            }
        });
    }

    public boolean walk()
    {
        if(path.isEmpty())
            return false;

        WorldPoint player = Static.getClient().getLocalPlayer().getWorldLocation();

        //cleanPath();

        int max = Math.min(10, path.size());
        int randomDist = random.nextInt(max) + 1;

        Step next = path.remove(0);
        Step lookAhead = path.get(1);

        int length = Location.pathTo(player, lookAhead.getPosition()).size();

        for(int i = 0; i < randomDist; i++)
        {
            next = path.remove(i);
            lookAhead = path.get(i + 1);
            if(!Location.isReachable(player, next.getPosition()))
            {
                System.out.println(1);
                if(length > 0 && length < 5)
                {
                    System.out.println(2);
                    path.remove(i + 1);
                    Movement.walkTo(lookAhead.getPosition());
                    return !path.isEmpty();
                }

                if(next.isDoored())
                {
                    System.out.println(3);
                    openDoor(next.getPosition());
                    return !path.isEmpty();
                }
            }
        }
        System.out.println("Walking to: " + next.getPosition());
        Movement.walkTo(next.getPosition());
        return !path.isEmpty();
    }

    private void openDoor(WorldPoint location)
    {
        TileObject tileObject = TObjects.getOpenableAt(location.getX(), location.getY());
        if(tileObject != null)
        {
            TObjects.interact(tileObject, "Open");
        }
    }

    private void cleanPath()
    {
        WorldPoint player = Static.getClient().getLocalPlayer().getWorldLocation();
        for(int i = 0; i < path.size(); i++)
        {
            Step step = path.get(i);
            if(!step.getPosition().equals(player))
            {
                path.remove(i);
            }
            else
            {
                break;
            }
        }
    }
}
