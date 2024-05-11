package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TDialogue;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
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
        addMethod(methods, "continueAllDialogue", ImmutableMap.of(),
                function -> TDialogue.continueAllDialogue(),
                "Continues through all dialogue. Selects options highlighted\n" +
                        "by quest helper if present. Completes history museum quiz questions\n" +
                        "if present. Stops when dialogue is completed or when options are shown\n" +
                        "but don't fall under museum quiz or quest helper."
        );
        addMethod(methods, "continueQuestHelper", ImmutableMap.of(),
                function -> TDialogue.continueQuestHelper(),
                "Selects the option highlighted by quest helper. If \n" +
                        "none present it continues dialogue if a continue is present"
        );
        addMethod(methods, "isDialogueOpen", Type.BOOL, ImmutableMap.of(),
                function -> TDialogue.isDialogueOpen(),
                "Returns true if a dialogue is open"
        );
        addMethod(methods, "interact", ImmutableMap.of(0, Pair.of("option", Type.ANY)),
                function -> TDialogue.interact(function.getArg(0, manager)),
                "Interacts with the dialogue."
        );
        addMethod(methods, "numericInput", ImmutableMap.of(0, Pair.of("option", Type.INT)),
                function -> TDialogue.numericInput(function.getArg(0, manager)),
                "Submits a numeric input."
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
                        if(!TDelay.tick(1))
                            break;
                    }
                    TDelay.tick(1);
                },
                "Handles a conversation with the given options. Requires string options."
        );

        return methods;
    }


}