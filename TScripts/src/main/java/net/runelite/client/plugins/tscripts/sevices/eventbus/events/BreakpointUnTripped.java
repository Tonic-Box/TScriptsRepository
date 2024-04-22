package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

public class BreakpointUnTripped
{
    public static BreakpointUnTripped get()
    {
        return instance;
    }
    private static final BreakpointUnTripped instance = new BreakpointUnTripped();
}
