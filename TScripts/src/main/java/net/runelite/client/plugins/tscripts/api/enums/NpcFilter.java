package net.runelite.client.plugins.tscripts.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.plugins.tscripts.api.library.TMovement;
import net.unethicalite.client.Static;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@AllArgsConstructor
@Getter
public enum NpcFilter
{
    /*NPC_FREE("NPC_FREE", "The npc is not interacting with anything",
            npc -> !npc.isInteracting() || !(npc.getIdlePoseAnimation() == npc.getPoseAnimation() && npc.getAnimation() == -1)
                    || (npc.getInteracting() != null && npc.getInteracting().getHealthScale() == -1)),*/
    NPC_FREE("NPC_FREE", "The npc is not interacting with anything",
            npc ->
            {
                Actor interacting = npc.getInteracting();
                if(interacting != null)
                {
                    return interacting.equals(Static.getClient().getLocalPlayer());
                }

                if(!(npc.getIdlePoseAnimation() == npc.getPoseAnimation() && npc.getAnimation() == -1))
                    return false;

                return npc.getHealthScale() == -1;
            }),
    NPC_REACHABLE("NPC_REACHABLE", "The npc's tile is reachable", npc -> TMovement.isReachable(npc.getWorldLocation())),
    NPC_UNREACHABLE("NPC_UNREACHABLE", "The npc's tile is not reachable", npc -> !TMovement.isReachable(npc.getWorldLocation())),
    NPC_ALIVE("NPC_ALIVE", "The npc is alive", npc -> !npc.isDead()),
    NPC_DEAD("NPC_DEAD", "the npc is dead", Actor::isDead);

    private final String name;
    private final String description;
    private final Predicate<Actor> condition;
    private final static BiPredicate<Object, NPC> byName = (name, npc) -> npc.getName().equals(name);
    private final static BiPredicate<Object, NPC> byId = (id, npc) -> npc.getId() == (int)id;

    public static NPC filter(Object identifier, NpcFilter... filter)
    {
        BiPredicate<Object, NPC> by = identifier instanceof String ? byName : (identifier instanceof Integer ? byId : null);
        return new NPCQuery()
                .filter(n -> {
                    if(by != null && !by.test(identifier, n))
                    {
                        return false;
                    }
                    for (NpcFilter f : filter)
                    {
                        if (f != null && !f.getCondition().test(n))
                        {
                            return false;
                        }
                    }
                    return true;
                })
                .result(Static.getClient())
                .nearestTo(Static.getClient().getLocalPlayer());
    }

    public static NPC filter(Object identifier, String... filter)
    {
        NpcFilter[] filters = new NpcFilter[filter.length];
        int i = 0;
        for (NpcFilter f : NpcFilter.values())
        {
            if(ArrayUtils.contains(filter, f.getName()))
            {
                filters[i++] = f;
            }
        }
        return filter(identifier, filters);
    }
}
