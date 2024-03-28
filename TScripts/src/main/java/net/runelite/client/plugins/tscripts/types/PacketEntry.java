package net.runelite.client.plugins.tscripts.types;

import com.google.gson.annotations.Expose;
import lombok.Data;

/**
 * Represents a packet entry
 */
@Data
public class PacketEntry
{
    @Expose
    private String field;
    @Expose
    private int id;
    @Expose
    private int length;
}
