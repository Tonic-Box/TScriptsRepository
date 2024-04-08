package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TPrayer;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;

import java.util.ArrayList;
import java.util.List;

public class GPrayer implements GroupDefinition
{
    @Override
    public String groupName() {
        return "Prayer";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "activateQuickPrayers",
                ImmutableMap.of(),
                function -> TPrayer.turnOnQuickPrayers(),
                "Activates quick prayers"
        );
        addMethod(methods, "deactivateQuickPrayers",
                ImmutableMap.of(),
                function -> TPrayer.turnOffQuickPrayers(),
                "Deactivates quick prayers"
        );
        addMethod(methods, "isQuickPrayersEnabled", Type.BOOL,
                ImmutableMap.of(),
                function -> TPrayer.isQuickPrayerEnabled(),
                "Returns true if quick prayers are enabled, false otherwise"
        );

        return methods;
    }
}
