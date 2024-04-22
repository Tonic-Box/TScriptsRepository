package net.runelite.client.plugins.tscripts.sevices;

import net.runelite.api.NPC;
import net.runelite.client.plugins.tscripts.api.enums.NpcFilter;
import net.runelite.client.plugins.tscripts.util.Logging;

public class UserQueries {
    public static NPC getNpc(Object identifier, String... filters)
    {
        try
        {
            return NpcFilter.filter(identifier, filters);
        }
        catch (Exception ex)
        {
            Logging.errorLog(ex);
            return null;
        }
    }
}
