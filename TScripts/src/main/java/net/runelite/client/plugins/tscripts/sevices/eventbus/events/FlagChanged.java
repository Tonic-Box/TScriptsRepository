package net.runelite.client.plugins.tscripts.sevices.eventbus.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class FlagChanged
{
    private final Map<String,Object> flags;
}
