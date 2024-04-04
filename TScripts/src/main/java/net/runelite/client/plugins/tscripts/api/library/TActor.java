package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Actor;

public class TActor
{
    public static boolean isInCombat(Actor actor)
    {
        return !isIdle(actor) || actor.getInteracting() != null ||
                (actor.getInteracting() != null && !actor.getInteracting().isDead());
    }

    public static boolean isIdle(Actor actor)
    {
        return (actor.getIdlePoseAnimation() == actor.getPoseAnimation() && actor.getAnimation() == -1);
    }
}
