package net.runelite.client.plugins.tscripts.sevices.ipc.packets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PacketOpcodes
{
    SCRIPT((byte) 0),
    SHUTDOWN((byte) 1);

    private final byte opcode;
}
