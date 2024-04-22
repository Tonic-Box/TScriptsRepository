package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

public class VariablesCleared
{
    public static VariablesCleared get()
    {
        return instance;
    }
    private static final VariablesCleared instance = new VariablesCleared();
}
