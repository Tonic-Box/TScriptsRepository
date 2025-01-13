package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

public class FramePopped
{
    private static final FramePopped INSTANCE = new FramePopped();

    public static FramePopped get()
    {
        return INSTANCE;
    }
}
