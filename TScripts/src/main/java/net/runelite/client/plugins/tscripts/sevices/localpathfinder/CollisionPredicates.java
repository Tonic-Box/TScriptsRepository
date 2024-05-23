package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import java.util.Set;
import java.util.function.Predicate;

public class CollisionPredicates
{
    public static final Predicate<Set<MovementFlag>> FULL_BLOCKING = flags -> flags.contains(MovementFlag.BLOCK_MOVEMENT_FULL) || flags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT) || flags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR) || flags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR_DECORATION);
    public static final Predicate<Set<MovementFlag>> BLOCKED_NORTH = flags -> FULL_BLOCKING.test(flags) || flags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH) || flags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH_EAST) || flags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH_WEST);
    public static final Predicate<Set<MovementFlag>> BLOCKED_EAST = flags -> FULL_BLOCKING.test(flags) || flags.contains(MovementFlag.BLOCK_MOVEMENT_EAST) || flags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH_EAST) || flags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH_EAST);
    public static final Predicate<Set<MovementFlag>> BLOCKED_SOUTH = flags -> FULL_BLOCKING.test(flags) || flags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH) || flags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH_EAST) || flags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH_WEST);
    public static final Predicate<Set<MovementFlag>> BLOCKED_WEST = flags -> FULL_BLOCKING.test(flags) || flags.contains(MovementFlag.BLOCK_MOVEMENT_WEST) || flags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH_WEST) || flags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH_WEST);

}
