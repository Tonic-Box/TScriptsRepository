package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.api.events.FakeXpDrop;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class FakeXpDropEvent implements EventData
{

    @Override
    public String getEventName() {
        return "FakeXpDrop";
    }

    @Override
    public List<String> getKeys() {
        return List.of("skill", "xp");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        FakeXpDrop fakeXpDrop = (FakeXpDrop) event;
        return Map.of(
                "skill", fakeXpDrop.getSkill().name(),
                "xp", fakeXpDrop.getXp()
        );
    }
}
