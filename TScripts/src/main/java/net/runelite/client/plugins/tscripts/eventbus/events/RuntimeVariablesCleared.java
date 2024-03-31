package net.runelite.client.plugins.tscripts.eventbus.events;

public class RuntimeVariablesCleared
{
    public static RuntimeVariablesCleared get()
    {
        return instance;
    }
    private static final RuntimeVariablesCleared instance = new RuntimeVariablesCleared();
}
