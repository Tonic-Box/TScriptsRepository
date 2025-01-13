package net.runelite.client.plugins.tscripts.runtime.variables;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.sevices.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.FramePopped;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.util.Logging;

import java.rmi.UnexpectedException;
import java.util.Stack;

@Getter
public class Frames
{
    private final Stack<Frame> frames = new Stack<>();

    public Frame push()
    {
        Frame frame = new Frame();
        frames.push(frame);
        return frame;
    }

    public Frame pop()
    {
        TEventBus.post(FramePopped.get());
        Frame frame = frames.pop();
        for(String key : frame.getVariableMap().keySet())
        {
            postChangedEvent(key, frame.get(key).getValue());
        }
        return frame;
    }

    public void put(String key, Object value)
    {
        for(Frame frame : frames)
        {
            if(frame.containsKey(key))
            {
                frame.put(key, value);
                return;
            }
        }
        frames.peek().put(key, value);
        postChangedEvent(key, value);
    }

    public Object get(String key)
    {
        for(Frame frame : frames)
        {
            if(frame.containsKey(key))
            {
                return frame.get(key).getValue();
            }
        }
        Logging.errorLog(new UnexpectedException("Variable not found: " + key));
        return null;
    }

    public void put(String key, Object index, Object value)
    {
        for(Frame frame : frames)
        {
            if(frame.containsKey(key))
            {
                frame.put(key, index, value);
                postChangedEvent(key + "[" + index + "]", value);
                return;
            }
        }
        frames.peek().put(key, index, value);
        postChangedEvent(key + "[" + index + "]", value);
    }

    public Object get(String key, Object index)
    {
        for(Frame frame : frames)
        {
            if(frame.containsKeyArray(key))
            {
                return frame.get(String.valueOf(key), index);
            }
        }
        Logging.errorLog(new UnexpectedException("Variable not found: " + key));
        return null;
    }

    private void postChangedEvent(String key, Object value)
    {
        TEventBus.post(new VariableUpdated(key, value));
    }

    public void clean()
    {
        for(Frame frame : frames)
        {
            frame.clean();
        }
        frames.clear();
        frames.push(new Frame());
    }

    public boolean containsKey(String name)
    {
        for(Frame frame : frames)
        {
            if(frame.containsKey(name))
            {
                return true;
            }
        }
        return false;
    }

    public boolean containsKeyArray(String name)
    {
        for(Frame frame : frames)
        {
            if(frame.containsKeyArray(name))
            {
                return true;
            }
        }
        return false;
    }

    public int getFrameCount()
    {
        return frames.size();
    }

    public int getVariableCount()
    {
        int count = 0;
        for(Frame frame : frames)
        {
            count += frame.getVariableMap().size() + frame.getArrayMap().size();
        }
        return count;
    }
}
