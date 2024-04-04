package net.runelite.client.plugins.tscripts.util;

import net.runelite.api.NPC;
import net.runelite.client.plugins.tscripts.types.NpcFilterType;
import net.runelite.client.plugins.tscripts.util.Logging;

public class UserQueries {
    public static NPC getNpc(Object identifier, String... filters)
    {
        try
        {
            return NpcFilterType.filter(identifier, filters);
        }
        catch (Exception ex)
        {
            Logging.errorLog(ex);
            return null;
        }
    }
}
