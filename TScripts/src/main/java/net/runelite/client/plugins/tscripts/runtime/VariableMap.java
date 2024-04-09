package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.util.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariablesCleaned;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariablesCleared;
import java.util.*;

public class VariableMap
{
    @Getter
    private final Map<String, Variable> variableMap = new HashMap<>();
    private final Stack<String> scopeStack = new Stack<>();

    private final List<String> frozenVariables = new ArrayList<>();

    public void put(String key, Object value)
    {
        if (!isFrozen(key))
        {
            for(Variable variable : variableMap.values())
            {
                if(variable.getName().equals(key) && scopeStack.contains(variable.getScopeHash()))
                {
                    variable.setValue(value);
                    postChangedEvent(key, value);
                    return;
                }
            }
            variableMap.put(key + " " + scopeStack.peek(), new Variable(key, value, scopeStack.peek()));
        }
        postChangedEvent(key, value);
    }

    public Object get(String key)
    {
        for (Variable variable : variableMap.values())
        {
            if (variable.getName().equals(key) && scopeStack.contains(variable.getScopeHash()))
            {
                return variable.getValue();
            }
        }
        return variableMap.getOrDefault(key + " " + scopeStack.peek(), new Variable(key, "null", scopeStack.peek())).getValue();
    }

    public boolean containsKey(String key)
    {
        for (Variable variable : variableMap.values())
        {
            if (variable.getName().equals(key) && scopeStack.contains(variable.getScopeHash()))
            {
                return true;
            }
        }
        return false;
    }

    public void clear()
    {
        variableMap.clear();
        frozenVariables.clear();
        TEventBus.post(VariablesCleared.get());
    }

    private void postChangedEvent(String key, Object value)
    {
        TEventBus.post(new VariableUpdated(key, value));
    }

    public boolean isFrozen(String key)
    {
        return frozenVariables.contains(key);
    }

    public void toggleFreeze(String key)
    {
        if (isFrozen(key))
            unfreeze(key);
        else
            freeze(key);
    }

    private void freeze(String key)
    {
        frozenVariables.add(key);
    }

    private void unfreeze(String key)
    {
        frozenVariables.remove(key);
    }

    public void cleanScope(String scope)
    {
        List<String> keysToRemove = new ArrayList<>();
        for (var variable : variableMap.entrySet())
        {
            if (variable.getValue().getScopeHash().equals(scope))
            {
                keysToRemove.add(variable.getKey());
            }
        }
        for (String key : keysToRemove)
        {
            variableMap.remove(key);
        }
        TEventBus.post(VariablesCleaned.get());
    }

    public void pushScope(String scope)
    {
        scopeStack.push(scope);
    }

    public String popScope()
    {
        String scope = scopeStack.pop();
        //cleanScope(scope);
        return scope;
    }

    public String popScope2()
    {
        return scopeStack.pop();
    }

    public String peekScope()
    {
        return scopeStack.peek();
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        for (Variable variable : variableMap.values())
        {
            out.append("\t").append(variable.getName()).append(" -> ").append(variable.getValue()).append(" (").append(variable.getScopeHash()).append(")");
        }
        return out.toString();
    }
}
