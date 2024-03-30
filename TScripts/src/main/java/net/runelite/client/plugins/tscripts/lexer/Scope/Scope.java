package net.runelite.client.plugins.tscripts.lexer.Scope;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.ConditionType;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.models.ElementType;

import java.util.Map;

/**
 * Represents a scope of elements
 */
@Getter
public class Scope extends Element
{
    /**
     * Creates a new scope
     * @param elements The elements in the scope
     */
    public Scope(Map<Integer, Element> elements)
    {
        this.elements = elements;
        setType(ElementType.SCOPE);
    }

    /**
     * Creates a new scope
     * @param elements The elements in the scope
     * @param condition The condition of the scope
     */
    public Scope(Map<Integer, Element> elements, Condition condition)
    {
        this.elements = elements;
        setType(ElementType.SCOPE);
        this.condition = condition;
    }

    private final Map<Integer, Element> elements;
    @Setter
    private Condition condition = null;

    /**
     * Clones the scope
     * @return The cloned scope
     */
    public Scope clone()
    {
        return new Scope(elements, condition);
    }

    /**
     * Converts the scope to a json string
     * @return The json string
     */
    public String toJson()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public void setCurrent(boolean current)
    {
        if(condition != null)
        {
            condition.setCurrent(current);
        }
    }
}
