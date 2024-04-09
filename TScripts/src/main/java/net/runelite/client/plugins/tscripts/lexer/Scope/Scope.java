package net.runelite.client.plugins.tscripts.lexer.Scope;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Conditions;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.models.ElementType;

import java.util.Map;
import java.util.UUID;

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
     * @param conditions The conditions of the scope
     */
    public Scope(Map<Integer, Element> elements, Conditions conditions)
    {
        this.elements = elements;
        setType(ElementType.SCOPE);
        this.conditions = conditions;
    }

    private final Map<Integer, Element> elements;
    @Setter
    private Map<Integer, Element> elseElements = null;
    @Setter
    private Conditions conditions = null;
    @Getter
    private String hash = UUID.randomUUID().toString();

    /**
     * Clones the scope
     * @return The cloned scope
     */
    public Scope clone()
    {
        return new Scope(elements, conditions);
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
        if(conditions != null)
        {
            for(Condition condition : conditions.getConditions().values())
            {
                condition.setCurrent(current);
            }
        }
    }
}
