package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.client.events.ChatboxInput;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class ChatboxInputEvent implements EventData
{

    @Override
    public String getEventName() {
        return "ChatboxInput";
    }

    @Override
    public List<String> getKeys() {
        return List.of("input", "type");
    }

    @Override
    public Map<String, Object> getEventData(Object event)
    {
        ChatboxInput chatboxInput = (ChatboxInput) event;
        return Map.of(
                "input", chatboxInput.getValue(),
                "type", chatboxInput.getChatType()
        );
    }
}
