package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class ActorDeathEvent implements EventData
{
    @Override
    public String getEventName() {
        return "ActorDeath";
    }

    @Override
    public List<String> getKeys() {
        return List.of("name", "actor", "npc", "player");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        ActorDeath actorDeath = (ActorDeath) event;
        return Map.of(
                "name" , actorDeath.getActor().getName(),
                "actor", actorDeath.getActor(),
                "npc", actorDeath.getActor() instanceof NPC ? actorDeath.getActor() : "null",
                "player", actorDeath.getActor() instanceof Player ? actorDeath.getActor() : "null"
        );
    }
}
