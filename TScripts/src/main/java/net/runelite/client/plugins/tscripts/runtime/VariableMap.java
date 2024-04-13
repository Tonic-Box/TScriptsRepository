package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.util.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariablesCleaned;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariablesCleared;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VariableMap
{
    @Getter
    private final Map<String, Variable> variableMap = new ConcurrentHashMap<>();
    private final Map<String, ArrayVariable> arrayMap = new ConcurrentHashMap<>();
    private final Stack<String> scopeStack = new Stack<>();

    public void put(String key, Object index, Object value)
    {
        for(ArrayVariable arrayVariable : arrayMap.values())
        {
            if(arrayVariable.getName().equals(key) && scopeStack.contains(arrayVariable.getScopeHash()))
            {
                arrayVariable.getValues().put(index, value);
                postChangedEvent(key, value);
                return;
            }
        }
        ArrayVariable arrayVariable = new ArrayVariable(key, scopeStack.peek());
        arrayVariable.getValues().put(index, value);
        arrayMap.put(key + " " + scopeStack.peek(), arrayVariable);
    }

    public Object get(String key, Object index)
    {
        for(ArrayVariable arrayVariable : arrayMap.values())
        {
            if(arrayVariable.getName().equals(key) && scopeStack.contains(arrayVariable.getScopeHash()))
            {
                for(Object entry : arrayVariable.getValues().keySet())
                {
                    if((entry + "").equals(index + ""))
                    {
                        return arrayVariable.getValues().get(entry);
                    }
                }
            }
        }
        return "null";
    }

    public void put(String key, Object value)
    {
        if(key == null || key.isBlank())
            return;

        for(Variable variable : variableMap.values())
        {
            if(variable.getName().equals(key) && scopeStack.contains(variable.getScopeHash()))
            {
                if(!variable.isFrozen())
                    variable.setValue(value);
                postChangedEvent(key, value);
                return;
            }
        }
        variableMap.put(key + " " + scopeStack.peek(), new Variable(key, value, scopeStack.peek()));
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

    public boolean containsKey(String key, Object index)
    {
        for (ArrayVariable arrayVariable : arrayMap.values())
        {
            if (arrayVariable.getName().equals(key) && scopeStack.contains(arrayVariable.getScopeHash()))
            {
                return arrayVariable.getValues().containsKey(index);
            }
        }
        return false;
    }

    public void clear()
    {
        variableMap.clear();
        TEventBus.post(VariablesCleared.get());
    }

    private void postChangedEvent(String key, Object value)
    {
        TEventBus.post(new VariableUpdated(key, value));
    }

    public boolean isFrozen(String name, String hash)
    {
        for(Variable variable : variableMap.values())
        {
            if(variable.getName().equals(name) && variable.getScopeHash().equals(hash))
            {
                return variable.isFrozen();
            }
        }
        return false;
    }

    public void toggleFreeze(String name, String hash)
    {
        if (isFrozen(name, hash))
            unfreeze(name, hash);
        else
            freeze(name, hash);
    }

    private void freeze(String name, String hash)
    {
        for(Variable variable : variableMap.values())
        {
            if(variable.getName().equals(name) && variable.getScopeHash().equals(hash))
            {
                variable.setFrozen(true);
                return;
            }
        }
    }

    private void unfreeze(String name, String hash)
    {
        for(Variable variable : variableMap.values())
        {
            if(variable.getName().equals(name) && variable.getScopeHash().equals(hash))
            {
                variable.setFrozen(false);
                return;
            }
        }
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

        keysToRemove.clear();
        for(var arrayVariable : arrayMap.entrySet())
        {
            if(arrayVariable.getValue().getScopeHash().equals(scope))
            {
                keysToRemove.add(arrayVariable.getKey());
            }
        }
        for(String key : keysToRemove)
        {
            arrayMap.remove(key);
        }

        TEventBus.post(new VariablesCleaned(scope));
    }

    public void pushScope(String scope)
    {
        scopeStack.push(scope);
    }

    public String popScope()
    {
        String scope = scopeStack.pop();
        cleanScope(scope);
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
