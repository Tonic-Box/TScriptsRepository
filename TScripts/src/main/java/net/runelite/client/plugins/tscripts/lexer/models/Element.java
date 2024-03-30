package net.runelite.client.plugins.tscripts.lexer.models;

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
