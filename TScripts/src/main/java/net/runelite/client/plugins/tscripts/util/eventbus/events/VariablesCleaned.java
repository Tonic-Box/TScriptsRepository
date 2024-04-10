package net.runelite.client.plugins.tscripts.util.eventbus.events;

import lombok.Data;

/**
 * Event that is fired when a scope ends and variables from that scopes context are scrubbed from existence
 */
@Data
public class VariablesCleaned {
    private final String scopeHash;
}
