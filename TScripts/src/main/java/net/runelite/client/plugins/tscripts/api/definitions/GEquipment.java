package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Item;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

import java.util.ArrayList;
import java.util.List;

public class GEquipment implements GroupDefinition {
    @Override
    public String groupName()
    {
        return "Equipment";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "equipmentContains", Type.BOOL, ImmutableMap.of(0, Pair.of("item", Type.VARARGS)),
                function ->
                {
                    for (Object object : function.getArgs())
                    {
                        if (object instanceof Integer)
                        {
                            if (Equipment.contains((int) object))
                                return true;
                        }
                        else if (object instanceof String)
                        {
                            if (Equipment.contains((String) object))
                                return true;
                        }
                        else if (object instanceof Item)
                        {
                            if (Equipment.contains(((Item) object).getName()))
                                return true;
                        }
                    }
                    return false;
                }, "Checks if the equipment contains the item");

        return methods;
    }
}
