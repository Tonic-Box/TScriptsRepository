package net.runelite.client.plugins.tscripts.util.eventbus.events;

public class BreakpointTripped
{
    public static BreakpointTripped get()
    {
        return instance;
    }
    private static final BreakpointTripped instance = new BreakpointTripped();
}
