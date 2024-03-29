package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.widgets.Dialog;
import java.util.ArrayList;
import java.util.List;

public class GDialogue implements GroupDefinition
{
    @Override
    public String groupName()
    {
        return "Dialogue";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager)
    {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "continueDialogue", ImmutableMap.of(),
                function -> Api.continueDialogue(),
                "Continues the dialogue by pressing the space bar"
        );
        addMethod(methods, "isDialogueOpen", Type.BOOL, ImmutableMap.of(),
                function -> Api.isDialogueOpen(),
                "Returns true if a dialogue is open"
        );
        return methods;
    }


}