package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TActor;
import net.runelite.client.plugins.tscripts.api.library.TWorldPoint;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.List;

public class GLocalPlayer implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Local Player";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "getX", Type.INT, ImmutableMap.of(),
                function -> TWorldPoint.get(Static.getClient().getLocalPlayer().getWorldLocation()).getX(),
                "Returns the x coordinate of the local player"
        );
        addMethod(methods, "getY", Type.INT, ImmutableMap.of(),
                function -> TWorldPoint.get(Static.getClient().getLocalPlayer().getWorldLocation()).getY(),
                "Returns the y coordinate of the local player"
        );
        addMethod(methods, "inCombat", Type.BOOL, ImmutableMap.of(),
                function -> TActor.isInCombat(Static.getClient().getLocalPlayer()),
                "Returns true if the local player is in combat"
        );
        return methods;
    }
}