package net.runelite.client.plugins.tscripts.api.events;

import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.plugins.tscripts.types.EventData;

import java.util.List;
import java.util.Map;

public class MenuOptionClickedEvent implements EventData
{
    @Override
    public String getEventName() {
        return "MenuOptionClicked";
    }

    @Override
    public List<String> getKeys() {
        return List.of("option", "target", "identifier", "opcode", "itemId", "param0", "param1");
    }

    @Override
    public Map<String, Object> getEventData(Object event) {
        MenuOptionClicked menuOptionClicked = (MenuOptionClicked) event;
        return Map.of(
                "option", menuOptionClicked.getMenuOption(),
                "target", menuOptionClicked.getMenuTarget(),
                "identifier", menuOptionClicked.getId(),
                "opcode", menuOptionClicked.getItemOp(),
                "itemId", menuOptionClicked.getItemId(),
                "param0", menuOptionClicked.getParam0(),
                "param1", menuOptionClicked.getParam1()
        );
    }
}
