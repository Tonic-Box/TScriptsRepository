package net.runelite.client.plugins.tscripts.adapter.models.condition;

/**
 * Represents the type of condition
 */
public enum ConditionType
{
    WHILE,
    IF,
    ELSE,
    FOR,
    USER_DEFINED_FUNCTION,
    TERNARY,
    LAMBDA,
    IPC_POST, NONE
}