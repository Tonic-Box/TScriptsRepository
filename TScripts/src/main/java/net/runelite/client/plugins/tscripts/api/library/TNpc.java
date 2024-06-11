package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.NPC;
import net.runelite.api.queries.NPCQuery;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.client.Static;

public class TNpc
{
    public static NPC getNpc(Object identifier)
    {
        if(identifier instanceof NPC)
        {
            return (NPC) identifier;
        }
        NPC npc = null;
        if (identifier instanceof Integer)
        {
            /*npc =  new NPCQuery()
                    .filter(n -> n.getId() == (int)identifier)
                    .filter(n -> !n.isDead())
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());*/
            npc = NPCs.getNearest(n -> n.getId() == (int)identifier && !n.isDead());
        }
        else if (identifier instanceof String)
        {
            /*npc =  new NPCQuery()
                    .filter(n -> n.getName().equals(identifier))
                    .filter(n -> !n.isDead())
                    .result(Static.getClient())
                    .nearestTo(Static.getClient().getLocalPlayer());*/
            npc = NPCs.getNearest(n -> n.getName().equals(identifier) && !n.isDead());
        }
        return npc;
    }
}
