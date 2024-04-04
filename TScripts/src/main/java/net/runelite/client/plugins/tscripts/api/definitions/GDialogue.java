package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDialogue;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;
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
                function -> TDialogue.continueDialogue(),
                "Continues the dialogue"
        );
        addMethod(methods, "isDialogueOpen", Type.BOOL, ImmutableMap.of(),
                function -> TDialogue.isDialogueOpen(),
                "Returns true if a dialogue is open"
        );
        return methods;
    }


}