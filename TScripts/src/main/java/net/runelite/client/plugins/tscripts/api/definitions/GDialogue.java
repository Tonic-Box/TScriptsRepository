package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TDialogue;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.ui.editor.debug.TokenDumper;

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
        addMethod(methods, "interact", ImmutableMap.of(0, Pair.of("option", Type.ANY)),
                function ->
                {
                    Object object = function.getArg(0, manager);
                    TDialogue.interact(object);
                },
                "Interacts with the dialogue."
        );
        addMethod(methods, "makeX", ImmutableMap.of(0, Pair.of("quantity", Type.INT)),
                function ->
                {
                    int quantity = function.getArg(0, manager);
                    TDialogue.makeX(quantity);
                },
                "Handles the make-x dialogue."
        );
        addMethod(methods, "handleConversation", ImmutableMap.of(0, Pair.of("options", Type.VARARGS)),
                function ->
                {
                    int ptr = 0;
                    while(true)
                    {
                        if(!TDialogue.continueDialogue())
                        {
                            if(ptr >= function.getArgs().length)
                            {
                                break;
                            }
                            else if(TDialogue.interact(function.getArg(ptr, manager)))
                            {
                                ptr++;
                            }
                            else if(!TDialogue.isDialogueOpen())
                            {
                                break;
                            }
                            else
                            {
                                continue;
                            }
                        }
                        TDelay.tick(1);
                    }
                    TDelay.tick(1);
                },
                "Handles a conversation with the given options. Requires string options."
        );
        return methods;
    }


}