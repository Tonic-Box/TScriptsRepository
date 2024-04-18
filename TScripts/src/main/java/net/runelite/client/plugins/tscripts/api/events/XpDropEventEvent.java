package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.client.events.XpDropEvent;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class XpDropEventEvent implements EventData
{
    @Override
    public String getEventName() {
        return "XpDropEvent";
    }

    @Override
    public List<String> getKeys() {
        return List.of("skill", "xp");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        XpDropEvent xpDropEvent = (XpDropEvent) event;
        return Map.of(
                "skill", xpDropEvent.getSkill().name(),
                "xp", xpDropEvent.getExp()
        );
    }
}
