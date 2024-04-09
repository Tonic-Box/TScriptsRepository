package net.runelite.client.plugins.tscripts.util.eventbus.events;

/**
 * Event that is fired when a scope ends and variables from that scopes context are scrubbed from existence
 */
public class VariablesCleaned {
    public static VariablesCleaned get()
    {
        return instance;
    }
    private static final VariablesCleaned instance = new VariablesCleaned();
}
