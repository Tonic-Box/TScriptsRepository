package net.runelite.client.plugins.tscripts.lexer.models;

/**
 * Enum representing the different types of elements that can be found in a script.
 */
public enum TokenType
{
    KEYWORD_SUBSCRIBE,
    KEYWORD_USER_DEFINED_FUNCTION,
    KEYWORD_IF,
    KEYWORD_WHILE,
    IDENTIFIER,
    OPEN_BRACE,
    CLOSE_BRACE,
    OPEN_PAREN,
    CLOSE_PAREN,
    COMMA,
    SEMICOLON,
    STATIC_VALUE,
    COMMENT,
    VARIABLE,
    VARIABLE_ASSIGNMENT,
    VARIABLE_INCREMENT,
    VARIABLE_DECREMENT,
    EOF,
    STRING,
    INTEGER,
    BOOLEAN,
    CONDITION_GT,
    CONDITION_LT,
    CONDITION_GTEQ,
    CONDITION_LTEQ,
    CONDITION_EQ,
    CONDITION_NEQ,
    NEGATE,
    VARIABLE_ADD_ONE,
    CONDITION_AND, CONDITION_OR, MULTI_LINE_COMMENT, KEYWORD_ELSE, VARIABLE_REMOVE_ONE
}