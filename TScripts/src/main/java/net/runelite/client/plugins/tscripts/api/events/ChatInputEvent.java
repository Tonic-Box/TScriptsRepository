package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.client.events.ChatInput;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class ChatInputEvent implements EventData
{
    @Override
    public String getEventName() {
        return "ChatInput";
    }

    @Override
    public List<String> getKeys() {
        return List.of("consumed");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        ChatInput chatInput = (ChatInput) event;
        return Map.of(
                "consumed", chatInput.isConsumed()
        );
    }
}
