package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VariableUpdated
{
    private final String name;
    private final Object value;
}
