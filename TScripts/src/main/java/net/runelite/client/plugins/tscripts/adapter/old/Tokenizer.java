package net.runelite.client.plugins.tscripts.adapter.old;

import net.runelite.client.plugins.tscripts.adapter.models.Token;
import net.runelite.client.plugins.tscripts.adapter.models.TokenType;
import net.runelite.client.plugins.tscripts.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizer
 */
@Deprecated
public class Tokenizer
{
    /**
     * Parse the code into tokens
     *
     * @param code the code to parse
     * @return the list of tokens
     */
    public static List<net.runelite.client.plugins.tscripts.adapter.models.Token> parse(String code)
    {
        return new Tokenizer().tokenize(code);
    }

    /**
     * Tokenize the code
     *
     * @param script the code to tokenize
     * @return the list of tokens
     */
    private List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokenize(String script) {
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inString = false;
        boolean inComment = false;
        boolean inMultiLineComment = false;
        boolean inArrayAccess = false;

        char[] chars = script.toCharArray();
        int line = 1;

        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            char next;
            char previous;
            if (i + 1 < chars.length) next = chars[i + 1];
            else next = ' ';
            if (i - 1 > 0) previous = chars[i - 1];
            else previous = ' ';
            if (c == '\n') line++;

            // Add check for array access start (e.g., $variable[)
            if (c == '[' && currentToken.length() > 0 && currentToken.charAt(0) == '$') {
                inArrayAccess = true;
                currentToken.append(c);
                flushToken(currentToken, tokens, line, net.runelite.client.plugins.tscripts.adapter.models.TokenType.ARRAY_ACCESS_START);
                continue; // Skip further checks and continue to the next character
            }

            // Add check for array access end (e.g., ])
            if (c == ']' && !inString && inArrayAccess) {
                inArrayAccess = false;
                List<net.runelite.client.plugins.tscripts.adapter.models.Token> newTokens = tokenize(currentToken.toString());
                if (!newTokens.isEmpty()) {
                    tokens.addAll(newTokens.subList(0, newTokens.size() - 1));
                }
                currentToken.setLength(0);
                currentToken.append(c);
                flushToken(currentToken, tokens, line, net.runelite.client.plugins.tscripts.adapter.models.TokenType.ARRAY_ACCESS_END);
                continue;
            }

            if (inString && (c != '"' || previous == '\\'))
            {
                currentToken.append(c);
            }
            else if (c == '"')
            {
                currentToken.append(c);
                if (inString)
                {
                    flushToken(currentToken, tokens, line);
                    inString = false;
                }
                else
                    inString = true;
            }
            else if(inComment && c == '\n')
            {
                inComment = false;
            }
            else if(c == '/' && next == '/' || inComment)
            {
                inComment = true;
            }
            else if (c == '/' && previous == '*' && inMultiLineComment)
            {
                inMultiLineComment = false;
            }
            else if (c == '/' && next == '*' || inMultiLineComment)
            {
                inMultiLineComment = true;
            }
            else if (Character.isWhitespace(c))
            {
                flushToken(currentToken, tokens, line);
            }
            else if (c == '{' || c == '}' || c == '(' || c == ')' || c == ',' || c == ';')
            {
                flushToken(currentToken, tokens, line);
                tokens.add(new net.runelite.client.plugins.tscripts.adapter.models.Token(getTokenType(String.valueOf(c)), String.valueOf(c), line));
            }
            else if (isOperatorStart(c, next))
            {
                if (currentToken.length() != 0)
                {
                    if (currentToken.length() != 1 || !isOperatorStart(currentToken.charAt(0), next))
                    {
                        flushToken(currentToken, tokens, line);
                    }
                }

                if (isTwoCharOperator(String.valueOf(c) + next))
                {
                    currentToken.append(c);
                }
                else
                {
                    currentToken.append(c);
                    flushToken(currentToken, tokens, line);
                }
            }
            else
            {
                currentToken.append(c);
            }
        }

        flushToken(currentToken, tokens, line);
        tokens.add(new net.runelite.client.plugins.tscripts.adapter.models.Token(net.runelite.client.plugins.tscripts.adapter.models.TokenType.EOF, "", line));

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
        return operator.equals("!=") || operator.equals(">=") || operator.equals("<=") || operator.equals("+=") || operator.equals("-=") || operator.equals("==") || operator.equals("++") || operator.equals("--") || operator.equals("&&") || operator.equals("||");
    }

    /**
     * Flush the current token
     *
     * @param currentToken the current token
     * @param tokens       the list of tokens
     */
    private void flushToken(StringBuilder currentToken, List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens, int line) {
        if (currentToken.length() != 0) {
            net.runelite.client.plugins.tscripts.adapter.models.TokenType tokenType = getTokenType(currentToken.toString());
            flushToken(currentToken, tokens, line, tokenType);
        }
    }

    /**
     * Flush the current token
     *
     * @param currentToken the current token
     * @param tokens       the list of tokens
     * @param line         the line number
     * @param tokenType    the token type
     */
    private void flushToken(StringBuilder currentToken, List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens, int line, net.runelite.client.plugins.tscripts.adapter.models.TokenType tokenType) {
        if (currentToken.length() != 0) {
            if(tokenType == net.runelite.client.plugins.tscripts.adapter.models.TokenType.STRING && !currentToken.toString().equals("null"))
            {
                currentToken.deleteCharAt(currentToken.length() - 1);
            }
            else if(tokenType == net.runelite.client.plugins.tscripts.adapter.models.TokenType.STRING)
            {
                currentToken.insert(0, "\"");
            }
            tokens.add(new Token(tokenType, currentToken.toString(), line));
            currentToken.setLength(0);
        }
    }

    /**
     * Get the token type
     *
     * @param tokenValue the token value
     * @return the token type
     */
    private net.runelite.client.plugins.tscripts.adapter.models.TokenType getTokenType(String tokenValue)
    {
        switch (tokenValue.toLowerCase())
        {
            case "if": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.KEYWORD_IF;
            case "else": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.KEYWORD_ELSE;
            case "while": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.KEYWORD_WHILE;
            case "for": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.KEYWORD_FOR;
            case "subscribe": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.KEYWORD_SUBSCRIBE;
            case "function":
            case "func": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.KEYWORD_USER_DEFINED_FUNCTION;
            case ">": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_GT;
            case "<": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_LT;
            case ">=": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_GTEQ;
            case "<=": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_LTEQ;
            case "==": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_EQ;
            case "!=": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_NEQ;
            case "&&": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_AND;
            case "||": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_OR;
            case "!": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE;
            case "=": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_ASSIGNMENT;
            case "+=": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_INCREMENT;
            case "-=": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_DECREMENT;
            case "++": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_ADD_ONE;
            case "--": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_REMOVE_ONE;
            case "{": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_BRACE;
            case "}": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CLOSE_BRACE;
            case "(": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN;
            case ")": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.CLOSE_PAREN;
            case ",": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.COMMA;
            case ";": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.SEMICOLON;
            case "null": return net.runelite.client.plugins.tscripts.adapter.models.TokenType.STRING;
            default:
                if (tokenValue.startsWith("$")) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE;
                if (tokenValue.startsWith("//")) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.COMMENT;
                if (tokenValue.startsWith("/*")) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.MULTI_LINE_COMMENT;
                if (tokenValue.startsWith("\"")) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.STRING;
                if (TextUtil.isNumeric(tokenValue)) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.INTEGER;
                if (tokenValue.equalsIgnoreCase("true") || tokenValue.equalsIgnoreCase("false")) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.BOOLEAN;
                if (tokenValue.contains(".")) return net.runelite.client.plugins.tscripts.adapter.models.TokenType.STATIC_VALUE;
                return TokenType.IDENTIFIER;
        }
    }
}