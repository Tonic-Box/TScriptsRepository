package net.runelite.client.plugins.tscripts.adapter.models;

import lombok.Data;

/**
 * Represents an element in the script.
 */
@Data
public class Element
{
    private ElementType type;
    private boolean current = false;
}
