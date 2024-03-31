package net.runelite.client.plugins.tscripts.eventbus.events;

public class RuntimeCurrentInstructionChanged
{
    public static RuntimeCurrentInstructionChanged get()
    {
        return instance;
    }
    private static final RuntimeCurrentInstructionChanged instance = new RuntimeCurrentInstructionChanged();
}