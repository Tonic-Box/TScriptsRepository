package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

public class BreakpointTripped
{
    public static BreakpointTripped get()
    {
        return instance;
    }
    private static final BreakpointTripped instance = new BreakpointTripped();
}
