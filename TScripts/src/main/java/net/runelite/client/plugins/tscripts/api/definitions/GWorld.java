package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.api.library.TPackets;
import net.runelite.client.plugins.tscripts.api.library.TWorld;
import net.runelite.client.plugins.tscripts.sevices.WorldService.WorldSelectionService;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.http.api.worlds.World;
import net.unethicalite.client.Static;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

public class GWorld implements GroupDefinition
{
    @Override
    public String groupName() {
        return "World";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "hop",
                ImmutableMap.of(
                        0, Pair.of("world", Type.INT)
                ),
                function ->
                {
                    int world = function.getArg(0, manager);
                    TWorld.hop(world);
                },
                "Hops to the specified world"
        );
        addMethod(methods, "hopRandomMembers",
                ImmutableMap.of(
                ),
                function ->
                {
                    World world = WorldSelectionService.get().getRandomMemberWorld();
                    System.out.println("hopping: " + world.getId());
                    TWorld.hop(world.getId());
                },
                "Hops to a random members world"
        );
        addMethod(methods, "hopRandomF2p",
                ImmutableMap.of(
                ),
                function ->
                {
                    World world = WorldSelectionService.get().getRandomF2pWorld();
                    System.out.println("hopping: " + world.getId());
                    TWorld.hop(world.getId());
                },
                "Hops to a random f2p world"
        );
        return methods;
    }
}
