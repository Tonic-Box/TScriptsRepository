package net.runelite.client.plugins.tscripts.types;

import java.util.List;
import java.util.Map;

public interface EventData
{
    String getEventName();
    List<String> getKeys();
    Map<String,Object> getEventData(Object event);
}
