package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScriptStateChanged
{
    private final String scriptName;
    private final String profile;
    private final Boolean running;
}
