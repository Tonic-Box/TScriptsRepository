package net.runelite.client.plugins.tscripts.adapter.old;

import lombok.Data;
import net.runelite.client.plugins.tscripts.adapter.models.condition.ConditionType;
import net.runelite.client.plugins.tscripts.adapter.models.method.MethodCall;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Comparator;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Condition;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Conditions;
import net.runelite.client.plugins.tscripts.adapter.models.condition.ForCondition;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Glue;
import net.runelite.client.plugins.tscripts.adapter.models.ElementType;
import net.runelite.client.plugins.tscripts.adapter.models.Token;
import net.runelite.client.plugins.tscripts.adapter.models.TokenType;
import net.runelite.client.plugins.tscripts.adapter.models.variable.ArrayAccess;
import net.runelite.client.plugins.tscripts.adapter.models.variable.AssignmentType;
import net.runelite.client.plugins.tscripts.adapter.models.variable.VariableAssignment;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.adapter.models.Scope.Scope;
import net.runelite.client.plugins.tscripts.adapter.models.Element;
import org.apache.commons.lang3.NotImplementedException;

import java.rmi.UnexpectedException;
import java.util.*;

/**
 * The Lexer class is responsible for parsing the tokens into an AST.
 */
@Data
@Deprecated
public class Lexer
{
    private static final Lexer lexer = new Lexer();
    private List<String> userFunctions = new ArrayList<>();

    /**
     * Lexes the tokens into a scope.
     * @param tokens the tokens to lex
     * @return the scope
     * @throws Exception if an error occurs
     */
    public static Scope lex(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        lexer.getUserFunctions().clear();
        return lexer.parse(tokens);
    }

    /**
     * Parses the tokens into a scope.
     * @param tokens the tokens to parse
     * @return the scope
     * @throws Exception if an error occurs
     */
    public Scope parse(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        return flushScope(tokens, null);
    }

    /**
     * processes the tokens into a scope.
     * @param tokens the tokens to flush
     * @param conditions the conditions
     * @return the scope
     * @throws Exception if an error occurs
     */
    private Scope flushScope(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens, Conditions conditions) throws Exception
    {
        //Stores the elements in the scope
        Map<Integer, Element> elements = new HashMap<>();

        //current list of tokens being processed
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> segment = new ArrayList<>();

        //Other variables to keep track of things
        int elementsPointer = 0;
        ElemType currentType = null;
        Conditions _conditions = null;
        String userFunctionName = null;
        int depthCounter = -1;
        int pointer = -1;


        for(net.runelite.client.plugins.tscripts.adapter.models.Token token : tokens)
        {
            pointer++;

            //skip comments
            if (token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.COMMENT)
            {
                continue;
            }

            //if the lexer has determined the current type of element being processed, then lets process it
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

                        if (tokens.get(pointer + 1).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.SEMICOLON)
                        {
                            elements.put(elementsPointer++, flushVariableAssignment(new ArrayList<>(segment)));
                            segment.clear();
                            depthCounter = -1;
                            currentType = null;
                        }
                        continue;
                    case SCOPE:
                        if (depthCounter == -1 && tokens.get(pointer - 1).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_BRACE)
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
                            Element element = elementsPointer - 1 >= 0 ? elements.get(elementsPointer - 1) : null;
                            if(_conditions != null && element != null && _conditions.getType() == ConditionType.ELSE && element.getType() == ElementType.SCOPE)
                            {
                                Scope scope = (Scope) element;
                                Scope elseScope = flushScope(new ArrayList<>(segment), null);
                                scope.setElseElements(elseScope.getElements());
                            }
                            else
                            {
                                elements.put(elementsPointer++, flushScope(new ArrayList<>(segment), (_conditions != null ? _conditions.clone() : null)));
                            }
                            segment.clear();
                            _conditions = null;
                        }

                        continue;
                    case FUNCTION:
                        if (depthCounter == -1 && token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
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
                    case USER_DEFINED_FUNCTION:
                    case CONDITION_FOR:
                    case CONDITION:
                        if (depthCounter == -1 && token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
                        {
                            depthCounter = 1;
                            segment.add(token);
                            continue;
                        }
                        else if (depthCounter == -1 && token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.IDENTIFIER && userFunctionName == null)
                        {
                            userFunctionName = token.getValue();
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
                            depthCounter = -1;
                            _conditions = (currentType == ElemType.CONDITION_FOR) ? flushForCondition(segment) : flushConditions(segment);
                            currentType = null;
                            if((_conditions.getType() == ConditionType.USER_DEFINED_FUNCTION || _conditions.getType() == ConditionType.SUBSCRIBE) && userFunctionName != null)
                            {
                                userFunctions.add(userFunctionName.toLowerCase());
                                _conditions.setUserFunctionName(userFunctionName);
                                userFunctionName = null;
                            }
                            if (tokens.get(pointer + 1).getType() != net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_BRACE)
                            {
                                throw new UnexpectedException("Lexer::parseScope[CONDITION->SCOPE] unexpected value, expected start of scope [T:" + pointer + "] got [" + tokens.get(pointer + 1).getType().name() + "]");
                            }
                            segment.clear();
                        }
                        continue;
                }
            }

            //if the current token type hasn't been set yet, lets figure it out
            switch (token.getType())
            {
                case KEYWORD_IF:
                case KEYWORD_WHILE:
                case KEYWORD_SUBSCRIBE:
                    segment.add(token);
                    currentType = ElemType.CONDITION;
                    break;
                case KEYWORD_FOR:
                    segment.add(token);
                    currentType = ElemType.CONDITION_FOR;
                    break;
                case KEYWORD_USER_DEFINED_FUNCTION:
                    segment.add(token);
                    currentType = ElemType.USER_DEFINED_FUNCTION;
                    break;
                case KEYWORD_ELSE:
                    segment.add(token);
                    depthCounter = -1;
                    _conditions = new Conditions();
                    _conditions.setType(ConditionType.ELSE);
                    segment.clear();
                    break;
                case OPEN_BRACE:
                    currentType = ElemType.SCOPE;
                    break;
                case IDENTIFIER:
                    segment.add(token);
                    currentType = ElemType.FUNCTION;
                    break;
                case ARRAY_ACCESS_START:
                    segment.add(token);
                    currentType = ElemType.VARIABLE_ASSIGNMENT;
                    break;
                case VARIABLE:
                    net.runelite.client.plugins.tscripts.adapter.models.TokenType btt = tokens.get(pointer + 1).getType();
                    if (btt != net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_ASSIGNMENT && btt != net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_INCREMENT && btt != net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_DECREMENT && btt != net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_ADD_ONE && btt != net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE_REMOVE_ONE)
                    {
                        throw new UnexpectedException("Lexer::parseScope[VARIABLE] unexpected value, expected VALUE_ASSIGNMENT token [T:" + (pointer + 1) + "] got [" + tokens.get(pointer + 1).getType().name() + "] on line {" + token.getLine() + "}");
                    }
                    segment.add(token);
                    currentType = ElemType.VARIABLE_ASSIGNMENT;
                    break;
                case NEGATE:
                    segment.add(token);
                    break;
            }
        }

        //return our scope
        return new Scope(elements, conditions);
    }

    /**
     * processes the tokens into a variable assignment.
     * @param tokens the tokens to flush
     * @return the variable assignment
     * @throws Exception if an error occurs
     */
    private VariableAssignment flushVariableAssignment(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        Object _variable = null;
        List<Object> _values = new ArrayList<>();
        AssignmentType _type = null;
        boolean inMethodCall = false;
        boolean inArray = false;
        int depthCounter = -1;
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> segment = new ArrayList<>();

        for(net.runelite.client.plugins.tscripts.adapter.models.Token token : tokens)
        {
            if(inArray)
            {
                if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.ARRAY_ACCESS_END)
                {
                    inArray = false;
                    segment.add(token);
                    if(_variable == null)
                        _variable = flushArrayAccess(new ArrayList<>(segment), false);
                    else
                        _values.add(flushArrayAccess(new ArrayList<>(segment), false));
                    segment.clear();
                    continue;
                }
                segment.add(token);
                continue;
            }
            if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.IDENTIFIER)
            {
                inMethodCall = true;
            }
            else if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE && !inMethodCall)
            {
                segment.add(token);
                continue;
            }

            if(inMethodCall)
            {
                if(depthCounter == -1 && token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
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

            if(!segment.isEmpty() && segment.get(0).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
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
                case ARRAY_ACCESS_START:
                    segment.add(token);
                    inArray = true;
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
                case VARIABLE_ADD_ONE:
                    _type = AssignmentType.ADD_ONE;
                    break;
                case VARIABLE_REMOVE_ONE:
                    _type = AssignmentType.REMOVE_ONE;
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
    private MethodCall flushFunction(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        boolean negated = false;
        int pointer = 0;
        if(tokens.get(pointer).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
        {
            negated = true;
            pointer++;
        }
        String name = tokens.get(pointer).getValue();
        List<Object> _values = new ArrayList<>();
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> segment = new ArrayList<>();
        boolean inMethodCall = false;
        boolean inArrayAccess = false;
        int depthCounter = -1;

        pointer += 2;

        for(int i = pointer; i <= tokens.size() - 1; i++)
        {
            net.runelite.client.plugins.tscripts.adapter.models.Token token = tokens.get(i);

            if(inArrayAccess)
            {
                if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.ARRAY_ACCESS_END)
                {
                    inArrayAccess = false;
                    segment.add(token);
                    _values.add(flushArrayAccess(new ArrayList<>(segment), negated));
                    segment.clear();
                    continue;
                }
                segment.add(token);
                continue;
            }

            if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.IDENTIFIER && tokens.get(i + 1).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
            {
                inMethodCall = true;
            }

            if(inMethodCall)
            {
                if(depthCounter == -1 && token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
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
                case IDENTIFIER:
                case STRING:
                    _values.add(token.getValue());
                    break;
                case ARRAY_ACCESS_START:
                    segment.add(token);
                    inArrayAccess = true;
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
        if(check != MethodManager.CHECK_RESPONSE.OK && !userFunctions.contains(name.toLowerCase()))
        {
            throw new UnexpectedException("Lexer::flushFunction method '" + name + "' contained errors: " + check.name() + " on line {" + tokens.get(0).getLine() + "}");
        }
        return methodCall;
    }

    private Conditions flushForCondition(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        Conditions conditions = new Conditions();
        ForCondition forCondition = new ForCondition();
        conditions.setType(ConditionType.FOR);
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> segment = new ArrayList<>();
        int part = 0;
        int depthCounter = -1;
        boolean inMethodCall = false;
        for(int i = 2; i < tokens.size() - 1; i++)
        {
            net.runelite.client.plugins.tscripts.adapter.models.Token token = tokens.get(i);
            while(inMethodCall)
            {
                token = tokens.get(++i);
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
                    inMethodCall = false;
                }
            }

            if (token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.SEMICOLON)
            {
                if (part == 0)
                {
                    forCondition.setVariableAssignment(flushVariableAssignment(segment));
                }
                else if (part == 1)
                {
                    segment.add(token);
                    conditions = flushConditions(segment);
                }
                segment.clear();
                part++;
            }
            else if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.IDENTIFIER && tokens.size() > i + 1 && tokens.get(i + 1).getType()  == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
            {
                inMethodCall = true;
            }
            else
            {
                segment.add(token);
            }
        }
        forCondition.setOperation(flushVariableAssignment(segment));
        segment.clear();
        conditions.setForCondition(forCondition);
        return conditions;
    }

    private Conditions flushConditions(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        Conditions conditions = new Conditions();
        ConditionType type;
        int iStart = 2;
        switch (tokens.get(0).getType())
        {
            case KEYWORD_IF:
                type = ConditionType.IF;
                break;
            case KEYWORD_ELSE:
                type = ConditionType.ELSE;
                break;
            case KEYWORD_WHILE:
                type = ConditionType.WHILE;
                break;
            case KEYWORD_SUBSCRIBE:
                type = ConditionType.SUBSCRIBE;
                break;
            case KEYWORD_USER_DEFINED_FUNCTION:
                type = ConditionType.USER_DEFINED_FUNCTION;
                break;
            default:
                iStart = 0;
                type = ConditionType.FOR;
                //throw new UnexpectedException("Lexer::flushCondition unexpected condition type on line {" + tokens.get(0).getLine() + "}");
        }

        conditions.setType(type);

        if(type == ConditionType.USER_DEFINED_FUNCTION || type == ConditionType.SUBSCRIBE)
        {
            for (int i = 2; i < tokens.size() - 1; i++)
            {
                net.runelite.client.plugins.tscripts.adapter.models.Token token = tokens.get(i);
                if (token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.VARIABLE)
                {
                    Condition condition = new Condition(token.getValue(), null, null);
                    conditions.getConditions().put(conditions.getConditions().size(), condition);
                }
            }
            return conditions;
        }

        List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokenList = new ArrayList<>();
        for(int i = iStart; i < tokens.size() - 1; i++)
        {
            net.runelite.client.plugins.tscripts.adapter.models.Token token = tokens.get(i);
            if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_AND || token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_OR)
            {
                Glue glue = token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.CONDITION_AND ? Glue.AND : Glue.OR;
                conditions.getConditions().put(conditions.getConditions().size(), flushCondition(tokenList));
                conditions.getGlues().put(conditions.getConditions().size() - 1, glue);
                tokenList.clear();
            }
            else
            {
                tokenList.add(token);
            }
        }
        conditions.getConditions().put(conditions.getConditions().size(), flushCondition(tokenList));
        return conditions;
    }

    /**
     * processes the tokens into a condition.
     * @param tokens the tokens to flush
     * @return the condition
     * @throws Exception if an error occurs
     */
    private Condition flushCondition(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens) throws Exception
    {
        Object left = null;
        Object right = null;
        Comparator comparator = null;
        net.runelite.client.plugins.tscripts.adapter.models.Token tok;
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> segment = new ArrayList<>();
        boolean inMethodCall = false;
        boolean inArray = false;
        int depthCounter = -1;
        for(int i = 0; i < tokens.size(); i++)
        {
            tok = tokens.get(i);

            if(inArray)
            {
                if(tok.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.ARRAY_ACCESS_END)
                {
                    inArray = false;
                    segment.add(tok);
                    if(left == null)
                    {
                        left = flushArrayAccess(new ArrayList<>(segment), false);
                    }
                    else if(right == null)
                    {
                        right = flushArrayAccess(new ArrayList<>(segment), false);
                    }
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition on line {" + tok.getLine() + "}");
                    segment.clear();
                    continue;
                }
                segment.add(tok);
                continue;
            }

            if(tok.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.IDENTIFIER && tokens.size() > i + 1 && tokens.get(i + 1).getType()  == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
            {
                inMethodCall = true;
            }
            else if (tok.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
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
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition on line {" + tok.getLine() + "}");
                    break;
                case BOOLEAN:
                    if(!segment.isEmpty() && segment.get(0).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
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
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition on line {" + tok.getLine() + "}");
                    break;
                case IDENTIFIER:
                    if(tokens.get(i + 1).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
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
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition on line {" + tok.getLine() + "}");
                    break;
                case STRING:
                    if(!segment.isEmpty() && segment.get(0).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
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
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition on line {" + tok.getLine() + "}");
                    break;
                case VARIABLE:
                    if(!segment.isEmpty() && segment.get(0).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
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
                    else throw new UnexpectedException("Lexer::flushCondition[" + tok.getType() + "] unexpected value in condition on line {" + tok.getLine() + "}");
                    break;
                case ARRAY_ACCESS_START:
                    segment.add(tok);
                    inArray = true;
                    break;
            }
        }

        return new Condition(left, right, comparator);
    }

    private ArrayAccess flushArrayAccess(List<net.runelite.client.plugins.tscripts.adapter.models.Token> tokens, boolean negated) throws Exception {
        int pointer = 0;
        if(tokens.get(pointer).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.NEGATE)
        {
            negated = true;
            pointer++;
        }
        String name = tokens.get(pointer++).getValue().split("\\[")[0];
        List<net.runelite.client.plugins.tscripts.adapter.models.Token> segment = new ArrayList<>();
        boolean inMethodCall = false;
        boolean inArrayAccess = false;
        int depthCounter = -1;
        Object idx = null;

        for(int i = pointer; i <= tokens.size() - 1; i++)
        {
            Token token = tokens.get(i);

            if(inArrayAccess)
            {
                if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.ARRAY_ACCESS_END)
                {
                    segment.add(token);
                    idx = flushArrayAccess(new ArrayList<>(segment), negated);
                    segment.clear();
                    break;
                }
                segment.add(token);
                continue;
            }

            if(token.getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.IDENTIFIER && tokens.get(i + 1).getType() == net.runelite.client.plugins.tscripts.adapter.models.TokenType.OPEN_PAREN)
            {
                inMethodCall = true;
            }

            if(inMethodCall)
            {
                if(depthCounter == -1 && token.getType() == TokenType.OPEN_PAREN)
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
                    idx = flushFunction(new ArrayList<>(segment));
                    segment.clear();
                    break;
                }
                continue;
            }

            switch (token.getType())
            {
                case VARIABLE:
                case IDENTIFIER:
                case STRING:
                    idx = token.getValue();
                    break;
                case ARRAY_ACCESS_START:
                    segment.add(token);
                    inArrayAccess = true;
                    break;
                case STATIC_VALUE:
                    throw new NotImplementedException("Lexer::flushFunction static values are not implemented");
                case BOOLEAN:
                    idx = token.getValue().equalsIgnoreCase("true");
                    break;
                case INTEGER:
                    idx = Integer.parseInt(token.getValue());
                    break;
            }

            if(idx != null)
            {
                break;
            }
        }
        return new ArrayAccess(name, idx, negated);
    }

    /**
     * The element type.
     */
    private enum ElemType
    {
        CONDITION,
        FUNCTION,
        SCOPE,
        USER_DEFINED_FUNCTION,
        CONDITION_FOR, VARIABLE_ASSIGNMENT
    }
}