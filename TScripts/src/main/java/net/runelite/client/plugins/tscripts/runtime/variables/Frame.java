package net.runelite.client.plugins.tscripts.runtime.variables;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Frame
{
    private final Map<String, Variable> variableMap = new ConcurrentHashMap<>();
    private final Map<String, ArrayVariable> arrayMap = new ConcurrentHashMap<>();

    public boolean containsKey(String key)
    {
        return variableMap.containsKey(key);
    }

    public boolean containsKeyArray(String key)
    {
        return arrayMap.containsKey(key);
    }

    public Variable get(String name)
    {
        return variableMap.get(name);
    }

    public void put(String name, Object value)
    {
        variableMap.put(name, new Variable(name, value));
    }

    public Object get(String name, Object index)
    {
        ArrayVariable arr;
        if (arrayMap.containsKey(name))
        {
            System.out.println("getting " + name + " " + index);
            arr = arrayMap.get(name);
        }
        else
        {
            arr = new ArrayVariable(name);
            arrayMap.put(name, arr);
        }

        if(!arr.getValues().containsKey(index))
        {
            arr.getValues().put(index, null);
        }
        for(Object key2 : arr.getValues().keySet())
        {
            System.out.println("key: " + key2 + " value: " + arr.getValues().get(key2));
        }
        System.out.println("returning " + arr.getValues().get(index));
        return arr.getValues().get(index);
    }

    public void put(String name, Object index, Object value)
    {
        System.out.println("putting " + name + " " + index + " " + value);
        ArrayVariable arr;
        if (arrayMap.containsKey(name))
        {
            arr = arrayMap.get(name);
        }
        else
        {
            arr = new ArrayVariable(name);
            arrayMap.put(name, arr);
        }
        arr.getValues().put(String.valueOf(index), value);
    }

    public void clean()
    {
        variableMap.clear();
        for (ArrayVariable arr : arrayMap.values())
        {
            arr.getValues().clear();
        }
        arrayMap.clear();
    }
}
