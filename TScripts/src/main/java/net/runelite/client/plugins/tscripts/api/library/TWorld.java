package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.widgets.ComponentID;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.unethicalite.client.Static;

import java.rmi.UnexpectedException;

public class TWorld
{
    public static void hop(int world)
    {
        int attempts = 0;

        while(TGame.invoke(() -> Static.getClient().getWidget(ComponentID.WORLD_SWITCHER_WORLD_LIST)) == null)
        {
            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, 11927555, -1, -1);
            TDelay.tick(1);
            if(++attempts > 5)
            {
                Logging.errorLog(new UnexpectedException("Failed to open world hopper"));
                return;
            }
        }

        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, 4522002, world, -1);
    }
}
