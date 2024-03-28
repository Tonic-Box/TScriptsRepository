package net.runelite.client.plugins.tscripts.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.tscripts.util.packets.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a packet definition.
 */
@RequiredArgsConstructor
@Getter
public class PacketDefinition
{
    private final String name;
    private final PacketBuffer buffer;
    private final Map<String,Long> map = new HashMap<>();
}