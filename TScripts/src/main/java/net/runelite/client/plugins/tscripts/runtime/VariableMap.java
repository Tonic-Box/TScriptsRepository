package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.eventbus.events.VariablesCleared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableMap
{
    @Getter
    private final Map<String, Object> variableMap = new HashMap<>();
    private final List<String> frozenVariables = new ArrayList<>();

    public void put(String key, Object value)
    {
        if (!isFrozen(key))
            variableMap.put(key, value);
        pollVariableInspector(key, value);
    }

    public Object get(String key)
    {
        return variableMap.getOrDefault(key, "");
    }

    public boolean containsKey(String key)
    {
        return variableMap.containsKey(key);
    }

    public void clear()
    {
        variableMap.clear();
        frozenVariables.clear();
        TEventBus.post(VariablesCleared.get());
    }

    private void pollVariableInspector(String key, Object value)
    {
        TEventBus.post(new VariableUpdated(key, value));
    }

    public boolean isFrozen(String key)
    {
        return frozenVariables.contains(key);
    }

    public void toggleFreeze(String key)
    {
        if (isFrozen(key))
            unfreeze(key);
        else
            freeze(key);
    }

    private void freeze(String key)
    {
        frozenVariables.add(key);
    }

    private void unfreeze(String key)
    {
        frozenVariables.remove(key);
    }
}
