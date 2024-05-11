package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.plugins.tscripts.runtime.Runtime;
import net.runelite.client.plugins.tscripts.sevices.cache.GameCache;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.plugins.tscripts.util.ThreadPool;
import net.unethicalite.client.Static;

public class TDelay
{
    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (Exception ex) {
            Logging.errorLog(ex);
        }
    }

    public static boolean tick(int length)
    {
        int original = GameCache.get().getTickCount();
        int next = GameCache.get().getTickCount() + length;
        while(GameCache.get().getTickCount() < next && GameCache.get().getTickCount() >= original)
        {
            if(Runtime.isInterrupted())
                return false;
            sleep(50);
        }
        return true;
    }

    public static void waitUntilIdle()
    {
        tick(1);
        Player player = Static.getClient().getLocalPlayer();
        while(!TGame.invoke(player::isIdle) && tick(1));
    }

    public static void waitUntilOnTile(int worldX, int worldY)
    {
        Player player = Static.getClient().getLocalPlayer();
        while((player.getWorldLocation().getX() != worldX || player.getWorldLocation().getY() != worldY) && tick(1));
    }

    public static void invokeLater(Runnable runnable, int ticks)
    {
        ThreadPool.submit(() -> {
            tick(ticks);
            Static.getClientThread().invoke(runnable);
        });
    }
}
