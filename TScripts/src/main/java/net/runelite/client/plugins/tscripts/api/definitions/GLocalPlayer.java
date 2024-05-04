package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Actor;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TActor;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.api.library.TWorldPoint;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.sevices.cache.GameCache;
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
        addMethod(methods, "getBoostedSkill", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("skill", Type.STRING)
                ),
                function -> Static.getClient().getBoostedSkillLevel(Skill.valueOf(function.getArg(0, manager))),
                "Returns the boosted level of a skill"
        );
        addMethod(methods, "getSkill", Type.INT,
                ImmutableMap.of(
                        0, Pair.of("skill", Type.STRING)
                ),
                function -> Static.getClient().getRealSkillLevel(Skill.valueOf(function.getArg(0, manager))),
                "Returns the real level of a skill"
        );
        addMethod(methods, "getRunEnergy", Type.INT,
                ImmutableMap.of(),
                function -> Static.getClient().getEnergy(),
                "Returns the run energy of the local player"
        );
        addMethod(methods, "runEnabled", Type.INT,
                ImmutableMap.of(),
                function -> TGame.invoke(() -> Static.getClient().getVarpValue(173)) == 1,
                "Returns true if run is enabled"
        );
        addMethod(methods, "getInteracting", Type.OBJECT,
                ImmutableMap.of(), function ->
                {
                    Actor actor = GameCache.get().getInteracting();

                    if(actor == null || actor.isDead())
                        return "null";

                    if(actor.getInteracting() != null && !actor.getInteracting().equals(Static.getClient().getLocalPlayer()))
                        return "null";

                    return actor;
                }, "Get the actor that the local player is interacting with"
        );
        addMethod(methods, "getSpecialAttackPercent", Type.INT,
                ImmutableMap.of(),
                function -> TGame.invoke(() -> Static.getClient().getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT)),
                "Gets special attack percent"
        );
        addMethod(methods, "isSpecialAttackEnabled", Type.INT,
                ImmutableMap.of(),
                function -> TGame.invoke(() -> Static.getClient().getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED)) == 1,
                "Returns true if special attack is enabled"
        );
        return methods;
    }
}