package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

public class CurrentInstructionChanged
{
    public static CurrentInstructionChanged get()
    {
        return instance;
    }
    private static final CurrentInstructionChanged instance = new CurrentInstructionChanged();
}