package net.runelite.client.plugins.tscripts.util.eventbus.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VariableUpdated
{
    private final String name;
    private final Object value;
}
