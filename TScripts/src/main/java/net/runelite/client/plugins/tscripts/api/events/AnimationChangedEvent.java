package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class AnimationChangedEvent implements EventData
{
    @Override
    public String getEventName() {
        return "AnimationChanged";
    }

    @Override
    public List<String> getKeys() {
        return List.of("actor", "npc", "player", "animation");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        AnimationChanged animationChanged = (AnimationChanged) event;
        return Map.of(
                "actor", animationChanged.getActor(),
                "npc", animationChanged.getActor() instanceof NPC ? animationChanged.getActor() : "null",
                "player", animationChanged.getActor() instanceof Player ? animationChanged.getActor() : "null",
                "animation", animationChanged.getActor().getAnimation()
        );
    }
}
