package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.api.events.ChatMessage;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class ChatMessageEvent implements EventData
{
    @Override
    public String getEventName() {
        return "ChatMessage";
    }

    @Override
    public List<String> getKeys() {
        return List.of("type", "name", "sender", "message");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        ChatMessage chatMessage = (ChatMessage) event;
        return Map.of(
                "type", chatMessage.getType().name(),
                "name", chatMessage.getName(),
                "sender", chatMessage.getSender(),
                "message", chatMessage.getMessage()
        );
    }
}
