package net.runelite.client.plugins.tscripts.lexer;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Comparator;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.ConditionType;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.models.Token;
import net.runelite.client.plugins.tscripts.lexer.models.TokenType;
import net.runelite.client.plugins.tscripts.lexer.variable.AssignmentType;
import net.runelite.client.plugins.tscripts.lexer.variable.VariableAssignment;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Field;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Lexer class is responsible for parsing the tokens into an AST.
 */
public class Lexer
{
    @Setter
    @Getter
    private boolean verify = true;
    @Setter
    @Getter
    private boolean debug = false;

    /**
     * Lexes the tokens into a scope.
     * @param tokens the tokens to lex
     * @return the scope
     * @throws Exception if an error occurs
     */
    public static Scope lex(List<Token> tokens) throws Exception {
        return new Lexer().parse(tokens);
    }

    /**
     * Parses the tokens into a scope.
     * @param tokens the tokens to parse
     * @return the scope
     * @throws Exception if an error occurs
     */
    public Scope parse(List<Token> tokens) throws Exception
    {
        return flushScope(tokens, null);
    }

    /**
     * processes the tokens into a scope.
     * @param tokens the tokens to flush
     * @param condition the condition
     * @return the scope
     * @throws Exception if an error occurs
     */
    private Scope flushScope(List<Token> tokens, Condition condition) throws Exception
    {
        Map<Integer, Element> elements = new HashMap<>();
        int elementsPointer = 0;

        ElemType currentType = null;
        Condition _condition = null;
        List<Token> segment = new ArrayList<>();
        int depthCounter = -1;

        int pointer = -1;
        for(Token token : tokens)
        {
            pointer++;

            if (token.getType().equals(TokenType.COMMENT))
            {
                continue;
            }

            if (currentType != null)
            {
                switch (currentType)
                {
                    case VARIABLE_ASSIGNMENT:
                        if (depthCounter == -1)
                        {
                            depthCounter = 1;
                        } else
                        {
                            depthCounter++;
                        }
                        segment.add(token);

                        if (tokens.get(pointer + 1).getType().equals(TokenType.SEMICOLON))
                        {
                            elements.put(elementsPointer++, flushVariableAssignment(new ArrayList<>(segment)));
                            segment.clear();
                            depthCounter = -1;
                            currentType = null;
                        }
                        continue;
                    case SCOPE:
                        if (depthCounter == -1 && tokens.get(pointer - 1).getType().equals(TokenType.OPEN_BRACE))
                        {
                            depthCounter = 1;
                            segment.add(token);
                            continue;
                        }

                        switch (token.getType())
                        {
                            case CLOSE_BRACE:
                                depthCounter--;
                                if (depthCounter != 0)
                                {
                                    segment.add(token);
                                }
                                break;
                            case OPEN_BRACE:
                                depthCounter++;
                                segment.add(token);
                                break;
                            default:
                                segment.add(token);
                        }

                        if (depthCounter == 0)
                        {
                            currentType = null;
                            depthCounter = -1;
                            elements.put(elementsPointer++, flushScope(new ArrayList<>(segment), (_condition != null ? _condition.clone() : null)));
                            segment.clear();
                            _condition = null;
                        }

                        continue;
                    case FUNCTION:
                        if (depthCounter == -1 && token.getType().equals(TokenType.OPEN_PAREN))
                        {
                            depthCounter = 1;
                            segment.add(token);
                            continue;
                        }

                        switch (token.getType())
                        {
                            case OPEN_PAREN:
                                depthCounter++;
                                segment.add(token);
                                break;
                            case CLOSE_PAREN:
                                depthCounter--;
                                segment.add(token);
                                break;
                            default:
                                segment.add(token);
                        }

                        if (depthCounter == 0)
                        {
                            currentType = null;
                            depthCounter = -1;
                            elements.put(elementsPointer++, flushFunction(new ArrayList<>(segment)));
                            segment.clear();
                        }
                        continue;
                    case CONDITION:
                        if (depthCounter == -1 && token.getType().equals(TokenType.OPEN_PAREN))
                        {
                            depthCounter = 1;
                            segment.add(token);
                            continue;
                        }

                        switch (token.getType())
                        {
                            case OPEN_PAREN:
                                depthCounter++;
                                segment.add(token);
                                break;
                            case CLOSE_PAREN:
                                depthCounter--;
                                segment.add(token);
                                break;
                            default:
                                segment.add(token);
                        }

                        if (depthCounter == 0)
                        {
                            currentType = null;
                            depthCounter = -1;
                            _condition = flushCondition(segment);
                            if (!tokens.get(pointer + 1).getType().equals(TokenType.OPEN_BRACE))
                            {
                                throw new UnexpectedException("Lexer::parseScope[CONDITION->SCOPE] unexpected value, expected start of scope [T:" + pointer + "] got [" + tokens.get(pointer + 1).getType().name() + "]");
                            }
                            segment.clear();
                        }
                        continue;
                }
            }


            if (token.getType().equals(TokenType.KEYWORD_IF))
            {
                segment.add(token);
                currentType = ElemType.CONDITION;
            }
            else if (token.getType().equals(TokenType.KEYWORD_WHILE))
            {
                segment.add(token);
                currentType = ElemType.CONDITION;
            }
            else if (token.getType().equals(TokenType.KEYWORD_REGISTER))
            {
                segment.add(token);
                currentType = ElemType.CONDITION;
            }
            else if (token.getType().equals(TokenType.OPEN_BRACE))
            {
                currentType = ElemType.SCOPE;
            }
            else if (token.getType().equals(TokenType.IDENTIFIER))
            {
                segment.add(token);
                currentType = ElemType.FUNCTION;
            }
            else if (token.getType().equals(TokenType.VARIABLE))
            {
                TokenType btt = tokens.get(pointer + 1).getType();
                if (!btt.equals(TokenType.VARIABLE_ASSIGNMENT) && !btt.equals(TokenType.VARIABLE_INCREMENT) && !btt.equals(TokenType.VARIABLE_DECREMENT))
                {
                    throw new UnexpectedException("Lexer::parseScope[VARIABLE] unexpected value, expected VALUE_ASSIGNMENT token [T:" + (pointer + 1) + "] got [" + tokens.get(pointer + 1).getType().name() + "]");
                }
                segment.add(token);
                currentType = ElemType.VARIABLE_ASSIGNMENT;
            }
            else if (token.getType().equals(TokenType.NEGATE))
            {
                segment.add(token);
            }
        }

        return new Scope(elements, condition);
    }

    /**
     * processes the tokens into a variable assignment.
     * @param tokens the tokens to flush
     * @return the variable assignment
     * @throws Exception if an error occurs
     */
    private VariableAssignment flushVariableAssignment(List<Token> tokens) throws Exception
    {
        String _variable = null;
        List<Object> _values = new ArrayList<>();
        AssignmentType _type = null;
        boolean inMethodCall = false;
        int depthCounter = -1;
        List<Token> segment = new ArrayList<>();

        for(Token token : tokens)
        {
            if(token.getType().equals(TokenType.IDENTIFIER))
            {
                inMethodCall = true;
            }
            else if(token.getType().equals(TokenType.NEGATE) && !inMethodCall)
            {
                segment.add(token);
                continue;
            }

            if(inMethodCall)
            {
                if(depthCounter == -1 && token.getType().equals(TokenType.OPEN_PAREN))
                {
                    depthCounter = 1;
                    segment.add(token);
                    continue;
                }

                switch (token.getType())
                {
                    case OPEN_PAREN:
                        depthCounter++;
                        segment.add(token);
                        break;
                    case CLOSE_PAREN:
                        depthCounter--;
                        segment.add(token);
                        break;
                    default:
                        segment.add(token);
                }

                if(depthCounter == 0)
                {
                    inMethodCall = false;
                    depthCounter = -1;
                    _values.add(flushFunction(new ArrayList<>(segment)));
                    segment.clear();
                }
                continue;
            }

            boolean negated = false;

            if(!segment.isEmpty() && segment.get(0).getType().equals(TokenType.NEGATE))
            {
                negated = true;
                segment.clear();
            }

            switch (token.getType())
            {
                case VARIABLE:
                    if(_variable == null)
                        _variable = token.getValue();
                    else
                        _values.add(token.getValue());
                    break;
                case STATIC_VALUE:
                    throw new NotImplementedException("Lexer::flushFunction static values are not implemented");
                case BOOLEAN:
                    if(negated)
                        _values.add(!token.getValue().equalsIgnoreCase("true"));
                    else
                        _values.add(token.getValue().equalsIgnoreCase("true"));
                    break;
                case INTEGER:
                    _values.add(Integer.parseInt(token.getValue()));
                    break;
                case STRING:
                    _values.add(token.getValue());
                    break;
                case VARIABLE_ASSIGNMENT:
                    _type = AssignmentType.ASSIGNMENT;
                    break;
                case VARIABLE_INCREMENT:
                    _type = AssignmentType.INCREMENT;
                    break;
                case VARIABLE_DECREMENT:
                    _type = AssignmentType.DECREMENT;
                    break;
            }
        }

        return new VariableAssignment(_variable, _values, _type);
    }

    /**
     * processes the tokens into a function.
     * @param tokens the tokens to flush
     * @return the method call
     * @throws Exception if an error occurs
     */
    private MethodCall flushFunction(List<Token> tokens) throws Exception
    {
        boolean negated = false;
        int pointer = 0;
        if(tokens.get(pointer).getType().equals(TokenType.NEGATE))
        {
            negated = true;
            pointer++;
        }
        String name = tokens.get(pointer).getValue();
        List<Object> _values = new ArrayList<>();
        List<Token> segment = new ArrayList<>();
        boolean inMethodCall = false;
        int depthCounter = -1;

        pointer += 2;

        for(int i = pointer; i <= tokens.size() - 1; i++)
        {
            Token token = tokens.get(i);
            if(token.getType().equals(TokenType.IDENTIFIER))
            {
                inMethodCall = true;
            }

            if(inMethodCall)
            {
                if(depthCounter == -1 && token.getType().equals(TokenType.OPEN_PAREN))
                {
                    depthCounter = 1;
                    segment.add(token);
                    continue;
                }

                switch (token.getType())
                {
                    case OPEN_PAREN:
                        depthCounter++;
                        segment.add(token);
                        break;
                    case CLOSE_PAREN:
                        depthCounter--;
                        segment.add(token);
                        break;
                    default:
                        segment.add(token);
                        break;
                }

                if(depthCounter == 0)
                {
                    inMethodCall = false;
                    depthCounter = -1;
                    _values.add(flushFunction(new ArrayList<>(segment)));
                    segment.clear();
                }
                continue;
            }

            switch (token.getType())
            {
                case VARIABLE:
                case STRING:
                    _values.add(token.getValue());
                    break;
                case STATIC_VALUE:
                    throw new NotImplementedException("Lexer::flushFunction static values are not implemented");
                case BOOLEAN:
                    _values.add(token.getValue().equalsIgnoreCase("true"));
                    break;
                case INTEGER:
                    _values.add(Integer.parseInt(token.getValue()));
                    break;
            }
        }
        MethodCall methodCall = new MethodCall(name, _values.toArray(), negated);
        MethodManager.CHECK_RESPONSE check = MethodManager.getInstance().check(methodCall);
        if(!check.equals(MethodManager.CHECK_RESPONSE.OK))
        {
            throw new UnexpectedException("Lexer::flushFunction method '" + name + "' contained errors: " + check.name());
        }
        return methodCall;
    }

    /**
     * processes the tokens into a condition.
     * @param tokens the tokens to flush
     * @return the condition
     * @throws Exception if an error occurs
     */
    private Condition flushCondition(List<Token> tokens) throws Exception
    {
        Object left = null;
        Object right = null;
        Comparator comparator = null;
        ConditionType type;
        switch (tokens.get(0).getType())
        {
            case KEYWORD_IF:
                type = ConditionType.IF;
                break;
            case KEYWORD_WHILE:
                type = ConditionType.WHILE;
                break;
            case KEYWORD_REGISTER:
                type = ConditionType.REGISTER;
                break;
            default:
                throw new UnexpectedException("Lexer::flushCondition unexpected condition type");
        }

        Token tok;
        List<Token> segment = new ArrayList<>();
        boolean inMethodCall = false;
        int depthCounter = -1;
        for(int i = 2; i < tokens.size() - 1; i++)
        {
            tok = tokens.get(i);

            if(tok.getType().equals(TokenType.IDENTIFIER) && tokens.size() > i + 1 && tokens.get(i + 1).getType().equals(TokenType.OPEN_PAREN))
            {
                inMethodCall = true;
            }
            else if (tok.getType().equals(TokenType.NEGATE))
            {
                segment.add(tok);
            }

            if(inMethodCall)
            {
                if (depthCounter == -1)
                {
                    segment.add(tok);
                    tok = tokens.get(++i);
                    segment.add(tok);
                    depthCounter = 1;
                }
                while(inMethodCall)
                {
                    tok = tokens.get(++i);
                    switch (tok.getType())
                    {
                        case OPEN_PAREN:
                            depthCounter++;
                            segment.add(tok);
                            break;
                        case CLOSE_PAREN:
                            depthCounter--;
                            segment.add(tok);
                            break;
                        default:
                            segment.add(tok);
                    }
                    if (depthCounter == 0)
                    {
                        inMethodCall = false;
                    }
                }

                depthCounter = -1;
                if(left == null)
                {
                    left = flushFunction(new ArrayList<>(segment));
                }
                else if(right == null)
                {
                    right = flushFunction(new ArrayList<>(segment));
                }
                segment.clear();
                continue;
            }

            boolean negated = false;
            switch (tok.getType())
            {
                case CONDITION_GT:
                case CONDITION_LT:
                case CONDITION_GTEQ:
                case CONDITION_LTEQ:
                case CONDITION_EQ:
                case CONDITION_NEQ:
                    comparator = Comparator.fromBaseTokenType(tok.getType());
                    break;
                case INTEGER:
                    if(left == null)
                    {
                        left = Integer.parseInt(tok.getValue());
                    }
                    else if(right == null)
                    {
                        right = Integer.parseInt(tok.getValue());
                    }
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition");
                    break;
                case BOOLEAN:
                    if(!segment.isEmpty() && segment.get(0).getType().equals(TokenType.NEGATE))
                    {
                        negated = true;
                        segment.clear();
                    }
                    if(left == null)
                    {
                        left = tok.getValue().equalsIgnoreCase("true");
                        if(negated)
                            left = !(boolean)left;
                    }
                    else if(right == null)
                    {
                        right = tok.getValue().equalsIgnoreCase("true");
                        if(negated)
                            right = !(boolean)right;
                    }
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition");
                    break;
                case IDENTIFIER:
                    if(tokens.get(i + 1).getType().equals(TokenType.OPEN_PAREN))
                    {
                        inMethodCall = true;
                        depthCounter = 1;
                        segment.add(tok);
                    }
                    else if(left == null)
                    {
                        left = tok.getValue();
                    }
                    else if(right == null)
                    {
                        right = tok.getValue();
                    }
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition");
                    break;
                case STRING:
                    if(!segment.isEmpty() && segment.get(0).getType().equals(TokenType.NEGATE))
                    {
                        segment.clear();
                    }
                    if(left == null)
                    {
                        left = tok.getValue();
                    }
                    else if(right == null)
                    {
                        right = tok.getValue();
                    }
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition");
                    break;
                case VARIABLE:
                    if(!segment.isEmpty() && segment.get(0).getType().equals(TokenType.NEGATE))
                    {
                        negated = true;
                        segment.clear();
                    }
                    if(left == null)
                    {
                        left = tok.getValue();
                        if(negated)
                            left = "!" + left;
                    }
                    else if(right == null)
                    {
                        right = tok.getValue();
                        if(negated)
                            right = "!" + right;
                    }
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition");
                    break;
            }
        }

        return new Condition(left, right, comparator, type);
    }

    /**
     * The element type.
     */
    private enum ElemType
    {
        CONDITION,
        FUNCTION,
        SCOPE,
        VARIABLE_ASSIGNMENT
    }
}