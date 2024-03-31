package net.runelite.client.plugins.tscripts.eventbus.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class RuntimeTelemetry
{
    private final Map<String,Object> flags;
}
