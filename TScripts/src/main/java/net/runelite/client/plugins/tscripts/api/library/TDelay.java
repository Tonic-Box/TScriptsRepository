package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.client.plugins.tscripts.util.Logging;

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
}
