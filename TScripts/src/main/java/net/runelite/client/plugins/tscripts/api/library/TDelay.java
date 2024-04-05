package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.plugins.tscripts.util.Logging;
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

    public static void tick(int length)
    {
        int next = Static.getClient().getTickCount() + length;
        while(Static.getClient().getTickCount() < next && Static.getClient().getTickCount() != 0)
        {
            if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
            {
                return;
            }
            sleep(50);
        }
    }

    public static void waitUntilIdle()
    {
        if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
        {
            return;
        }
        tick(1);
        Player player = Static.getClient().getLocalPlayer();
        while(!TGame.invoke(player::isIdle))
        {
            if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
            {
                return;
            }
            tick(1);
        }
    }

    public static void waitUntilOnTile(int worldX, int worldY)
    {
        Player player = Static.getClient().getLocalPlayer();
        while(player.getWorldLocation().getX() != worldX || player.getWorldLocation().getY() != worldY)
        {
            if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN) && !Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.HOPPING))
            {
                return;
            }
            tick(1);
        }
    }
}
