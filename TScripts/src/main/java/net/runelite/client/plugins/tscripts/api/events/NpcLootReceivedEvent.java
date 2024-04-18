package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class NpcLootReceivedEvent implements EventData
{
    @Override
    public String getEventName() {
        return "NpcLootReceived";
    }

    @Override
    public List<String> getKeys() {
        return List.of("npc");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        NpcLootReceived npcLootReceived = (NpcLootReceived) event;
        return Map.of(
                "npc", npcLootReceived.getNpc()
        );
    }
}
