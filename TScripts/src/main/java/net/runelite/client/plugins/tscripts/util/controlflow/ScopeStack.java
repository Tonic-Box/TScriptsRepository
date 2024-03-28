package net.runelite.client.plugins.tscripts.util.controlflow;

import java.util.*;

/**
 * A stack that keeps track of the current scope
 */
public class ScopeStack
{
    private final Stack<Integer> stack = new Stack<>();
    private final List<Integer> whiles = new ArrayList<>();
    private final Map<Integer,Object> scopes = new HashMap<>();

    /**
     * Push a new scope onto the stack
     * @param scope the scope
     * @param isWhile if the scope is a while loop
     * @param scopeObject the object that represents the scope
     */
    public void push(int scope, boolean isWhile, Object scopeObject)
    {
        stack.push(scope);
        scopes.put(scope, scopeObject);
        if(isWhile)
            whiles.add(scope);
    }

    /**
     * Pop the current scope off the stack
     */
    public void clean()
    {
        stack.clear();
        whiles.clear();
        scopes.clear();
    }

    /**
     * Pop the current scope off the stack
     * @return the scope
     */
    public int pop()
    {
        return stack.pop();
    }

    /**
     * Peek at the current scope
     * @return the scope
     */
    public int peek()
    {
        if(stack.isEmpty())
            return 0;
        return stack.peek();
    }

    /**
     * Check if the stack is empty
     * @return if the stack is empty
     */
    public boolean isEmpty()
    {
        return stack.isEmpty();
    }

    /**
     * Continue to the next iteration of the while loop
     * @param linkBacks the link backs
     * @return the scope to continue to
     */
    public int _continue(Map<Object,String> linkBacks)
    {
        if(stack.isEmpty())
        {
            return 0;
        }

        int size = stack.size();
        int out = 0;
        while(size > 0)
        {
            size--;
            if(whiles.contains(stack.get(size)))
            {
                out = stack.get(size);
                break;
            }
        }
        Object node2 = _node(out);
        if(node2 != null)
        {
            linkBacks.put(node2, "[C]");
        }
        return out;
    }

    /**
     * Break out of the while loop
     * @param linkBacks the link backs
     * @return the scope to break to
     */
    public int _break(Map<Object,String> linkBacks)
    {
        if(stack.isEmpty())
        {
            return 0;
        }

        int size = stack.size();
        int out = 0;
        while(size > 0)
        {
            size--;
            if(whiles.contains(stack.get(size)))
            {
                out = stack.get(size - 1);
                break;
            }
        }

        Object node2 = _node(out);
        if(node2 != null)
        {
            linkBacks.put(node2, "[B]");
        }
        return out;
    }

    /**
     * Get the object that represents the scope
     * @param val the scope
     * @return the object
     */
    private Object _node(int val)
    {
        if(!scopes.containsKey(val))
            return null;
        return scopes.get(val);
    }
}