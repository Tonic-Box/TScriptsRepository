package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Actor;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;

import java.util.ArrayList;
import java.util.List;

public class GActor implements GroupDefinition {

    @Override
    public String groupName() {
        return "Actor";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "getActorName", Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("actor", Type.OBJECT)
                ), function ->
                {
                    Object actor = function.getArg(0, manager);
                    if(actor instanceof Actor)
                    {
                        return ((Actor) actor).getName();
                    }
                    return "null";
                }, "Get the name of the actor");

        addMethod(methods, "getCombatLevel", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("actor", Type.OBJECT)
                ), function ->
                {
                    Object actor = function.getArg(0, manager);
                    if(actor instanceof Actor)
                    {
                        return ((Actor) actor).getCombatLevel();
                    }
                    return 0;
                }, "Get the combat level of the actor");

        return methods;
    }
}
