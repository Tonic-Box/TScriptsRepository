package net.runelite.client.plugins.tscripts.runtime.variables;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.sevices.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.VariablesCleared;

@Getter
public class VariableMap
{
    private final Frames frames = new Frames();

    public void clear()
    {
        frames.clean();
        TEventBus.post(VariablesCleared.get());
    }

    public boolean isFrozen(String name, String hash)
    {
        return false;
    }

    public void toggleFreeze(String name, String hash)
    {
        if (isFrozen(name, hash))
            unfreeze(name, hash);
        else
            freeze(name, hash);
    }

    private void freeze(String name, String hash)
    {
    }

    private void unfreeze(String name, String hash)
    {
    }

    public Frame pushScope()
    {
        return frames.push();
    }

    public Frame popScope()
    {
        return frames.pop();
    }

    public void put(String key, Object value)
    {
        frames.put(key, value);
    }

    public void put(String key, Object index, Object value)
    {
        frames.put(key, index, value);
    }

    public Object get(String key)
    {
        return frames.get(key);
    }

    public Object get(String key, Object index)
    {
        return frames.get(key, index);
    }

    public int getFrameCount()
    {
        return frames.getFrameCount();
    }

    public int getVariableCount()
    {
        return frames.getVariableCount();
    }
}
