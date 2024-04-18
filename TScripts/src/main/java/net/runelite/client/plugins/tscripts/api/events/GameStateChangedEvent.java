package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.api.events.GameStateChanged;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class GameStateChangedEvent implements EventData
{

    @Override
    public String getEventName() {
        return "GameStateChanged";
    }

    @Override
    public List<String> getKeys() {
        return null;
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        GameStateChanged gameStateChanged = (GameStateChanged) event;
        return Map.of(
                "name", gameStateChanged.getGameState().name(),
                "gameState", gameStateChanged.getGameState().getState()
        );
    }
}
