package net.runelite.client.plugins.tscripts.sevices.WorldService;

import net.runelite.client.game.WorldService;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WorldSelectionService
{
    private static final WorldSelectionService instance = new WorldSelectionService();
    public static WorldSelectionService get()
    {
        return instance;
    }

    private final WorldService worldService;
    private final List<World> safeMembersWorlds;
    private final List<World> safeF2pWorlds;
    private final Predicate<World> safeFilter = world ->
            !world.getTypes().contains(WorldType.PVP) &&
            !world.getTypes().contains(WorldType.PVP_ARENA) &&
            !world.getTypes().contains(WorldType.DEADMAN) &&
            !world.getTypes().contains(WorldType.HIGH_RISK) &&
            !world.getTypes().contains(WorldType.QUEST_SPEEDRUNNING) &&
            !world.getTypes().contains(WorldType.SKILL_TOTAL) &&
            !world.getTypes().contains(WorldType.BOUNTY) &&
            !world.getTypes().contains(WorldType.BETA_WORLD) &&
            !world.getTypes().contains(WorldType.NOSAVE_MODE) &&
            !world.getTypes().contains(WorldType.FRESH_START_WORLD) &&
            !world.getTypes().contains(WorldType.SEASONAL);


    public WorldSelectionService()
    {
        worldService = Static.getWorldService();

        safeMembersWorlds = filter(world -> world.getTypes().contains(WorldType.MEMBERS) && safeFilter.test(world));
        safeF2pWorlds = filter(world -> !world.getTypes().contains(WorldType.MEMBERS) && safeFilter.test(world));
    }

    public List<World> filter(Predicate<World> filter)
    {
        WorldResult worldResult = worldService.getWorlds();
        if(worldResult == null)
            return new ArrayList<>();
        return worldResult.getWorlds().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public World getRandomMemberWorld()
    {
        return safeMembersWorlds.get((int) (Math.random() * safeMembersWorlds.size()));
    }

    public World getRandomF2pWorld()
    {
        return safeF2pWorlds.get((int) (Math.random() * safeF2pWorlds.size()));
    }
}
