package net.runelite.client.plugins.tscripts.lexer.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a token in the script.
 */
@Getter
@RequiredArgsConstructor
public class Token
{
    private final TokenType type;
    private final String value;
    private final int line;

    @Override
    public String toString()
    {
        return "[" + type + "] " + value;
    }
}