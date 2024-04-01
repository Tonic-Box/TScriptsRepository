package net.runelite.client.plugins.tscripts.lexer;

import net.runelite.client.plugins.tscripts.lexer.models.Token;
import net.runelite.client.plugins.tscripts.lexer.models.TokenType;
import net.runelite.client.plugins.tscripts.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizer
 */
public class Tokenizer
{
    /**
     * Parse the code into tokens
     *
     * @param code the code to parse
     * @return the list of tokens
     */
    public static List<Token> parse(String code)
    {
        return new Tokenizer().tokenize(code);
    }

    /**
     * Tokenize the code
     *
     * @param script the code to tokenize
     * @return the list of tokens
     */
    private List<Token> tokenize(String script) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inString = false;

        script = removeComments(script);

        char[] chars = script.toCharArray();
        int pointer = -1;
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            char next;
            if (i + 1 < chars.length)
            {
                next = chars[i + 1];
            }
            else
            {
                next = ' ';
            }
            pointer++;
            if (inString && c != '"')
            {
                currentToken.append(c);
            } else if (c == '"')
            {
                currentToken.append(c);
                if (inString)
                {
                    flushToken(currentToken, tokens);
                    inString = false;
                } else
                    inString = true;
            } else if (Character.isWhitespace(c))
            {
                flushToken(currentToken, tokens);
            } else if (c == '{' || c == '}' || c == '(' || c == ')' || c == ',' || c == ';')
            {
                flushToken(currentToken, tokens);
                tokens.add(new Token(getTokenType(String.valueOf(c)), String.valueOf(c)));
            } else if (isOperatorStart(c, next))
            {
                if (currentToken.length() != 0)
                {
                    if (currentToken.length() != 1 || !isOperatorStart(currentToken.charAt(0), next))
                    {
                        flushToken(currentToken, tokens);
                    }
                }

                if (isTwoCharOperator(String.valueOf(c) + chars[pointer + 1]))
                {
                    currentToken.append(c);
                } else
                {
                    currentToken.append(c);
                    flushToken(currentToken, tokens);
                }
            } else
            {
                currentToken.append(c);
            }
        }

        flushToken(currentToken, tokens);
        tokens.add(new Token(TokenType.EOF, ""));

        return tokens;
    }

    /**
     * Check if the character is the start of an operator
     *
     * @param c    the character
     * @param next the next character
     * @return true if the character is the start of an operator
     */
    private boolean isOperatorStart(char c, char next) {
        if(c == '-' && Character.isDigit(next))
        {
            return false;
        }
        return   c == '>' || c == '<' || c == '=' || c == '!' || c == '+' || c == '-';
    }

    /**
     * Check if the operator is a two character operator
     *
     * @param operator the operator
     * @return true if the operator is a two character operator
     */
    private boolean isTwoCharOperator(String operator) {
        return operator.equals("!=") || operator.equals(">=") || operator.equals("<=") || operator.equals("+=") || operator.equals("-=") || operator.equals("==");
    }

    /**
     * Flush the current token
     *
     * @param currentToken the current token
     * @param tokens       the list of tokens
     */
    private void flushToken(StringBuilder currentToken, List<Token> tokens) {
        if (currentToken.length() != 0) {
            tokens.add(new Token(getTokenType(currentToken.toString()), currentToken.toString()));
            currentToken.setLength(0);
        }
    }

    /**
     * Get the token type
     *
     * @param tokenValue the token value
     * @return the token type
     */
    private TokenType getTokenType(String tokenValue) {
        if (tokenValue.equals("if")) return TokenType.KEYWORD_IF;
        if (tokenValue.equals("while")) return TokenType.KEYWORD_WHILE;
        if (tokenValue.equals("register")) return TokenType.KEYWORD_REGISTER;
        if (tokenValue.equals("function")) return TokenType.KEYWORD_USER_DEFINED_FUNCTION;
        if (tokenValue.equals(">")) return TokenType.CONDITION_GT;
        if (tokenValue.equals("<")) return TokenType.CONDITION_LT;
        if (tokenValue.equals(">=")) return TokenType.CONDITION_GTEQ;
        if (tokenValue.equals("<=")) return TokenType.CONDITION_LTEQ;
        if (tokenValue.equals("==")) return TokenType.CONDITION_EQ;
        if (tokenValue.equals("!=")) return TokenType.CONDITION_NEQ;
        if (tokenValue.equals("!")) return TokenType.NEGATE;
        if (tokenValue.equals("=")) return TokenType.VARIABLE_ASSIGNMENT;
        if (tokenValue.equals("+=")) return TokenType.VARIABLE_INCREMENT;
        if (tokenValue.equals("-=")) return TokenType.VARIABLE_DECREMENT;
        if (tokenValue.equals("{")) return TokenType.OPEN_BRACE;
        if (tokenValue.equals("}")) return TokenType.CLOSE_BRACE;
        if (tokenValue.equals("(")) return TokenType.OPEN_PAREN;
        if (tokenValue.equals(")")) return TokenType.CLOSE_PAREN;
        if (tokenValue.equals(",")) return TokenType.COMMA;
        if (tokenValue.equals(";")) return TokenType.SEMICOLON;
        if (tokenValue.startsWith("$")) return TokenType.VARIABLE;
        if (tokenValue.startsWith("//")) return TokenType.COMMENT;
        if (tokenValue.startsWith("\"")) return TokenType.STRING;
        if (TextUtil.isNumeric(tokenValue)) return TokenType.INTEGER;
        if (tokenValue.equalsIgnoreCase("true")) return TokenType.BOOLEAN;
        if (tokenValue.equalsIgnoreCase("false")) return TokenType.BOOLEAN;
        if (tokenValue.contains(".")) return TokenType.STATIC_VALUE;
        return TokenType.IDENTIFIER;
    }

    /**
     * Remove comments from the code
     *
     * @param input the code
     * @return the code without comments
     */
    private String removeComments(String input) {
        String noComments = input.replaceAll("//.*", "");
        return noComments.replaceAll("/\\*(?s).*?\\*/", "");
    }
}