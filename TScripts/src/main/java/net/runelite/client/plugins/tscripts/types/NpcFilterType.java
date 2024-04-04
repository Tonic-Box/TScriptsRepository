package net.runelite.client.plugins.tscripts.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.plugins.tscripts.api.library.TMovement;
import net.unethicalite.client.Static;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@AllArgsConstructor
@Getter
public enum NpcFilterType
{
    NPC_FREE("NPC_FREE", "The npc is not interacting with anything",
            npc -> !npc.isInteracting() || !(npc.getIdlePoseAnimation() == npc.getPoseAnimation() && npc.getAnimation() == -1)
            || (npc.getInteracting() != null && npc.getInteracting().getHealthScale() != -1)),
    NPC_REACHABLE("NPC_REACHABLE", "The npc's tile is reachable", npc -> TMovement.isReachable(npc.getWorldLocation())),
    NPC_UNREACHABLE("NPC_UNREACHABLE", "The npc's tile is not reachable", npc -> !TMovement.isReachable(npc.getWorldLocation())),
    NPC_ALIVE("NPC_ALIVE", "The npc is alive", npc -> !npc.isDead()),
    NPC_DEAD("NPC_DEAD", "the npc is dead", Actor::isDead);

    private final String name;
    private final String description;
    private final Predicate<NPC> condition;
    private final static BiPredicate<Object, NPC> byName = (name, npc) -> npc.getName().equals(name);
    private final static BiPredicate<Object, NPC> byId = (id, npc) -> npc.getId() == (int)id;

    public static NPC filter(Object identifier, NpcFilterType... filter)
    {
        BiPredicate<Object, NPC> by = identifier instanceof String ? byName : (identifier instanceof Integer ? byId : null);
        return new NPCQuery()
                .filter(n -> {
                    if(by != null && !by.test(identifier, n))
                    {
                        return false;
                    }
                    for (NpcFilterType f : filter)
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
        NpcFilterType[] filters = new NpcFilterType[filter.length];
        for (int i = 0; i < filter.length; i++)
        {
            for (NpcFilterType f : NpcFilterType.values())
            {
                if (f.getName().equals(filter[i]))
                {
                    filters[i] = f;
                    break;
                }
            }
        }
        return filter(identifier, filters);
    }
}
