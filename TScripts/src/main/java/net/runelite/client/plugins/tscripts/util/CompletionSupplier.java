package net.runelite.client.plugins.tscripts.util;

import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.types.filters.NpcFilterType;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import java.util.ArrayList;
import java.util.List;

public class CompletionSupplier
{
    /**
     * Creates a completion provider with all the methods from the MethodManager
     * @return the completion provider
     */
    public static CompletionProvider createBaseCompletionProvider()
    {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.setAutoActivationRules(true, ".$_");
        List<Completion> completions = new ArrayList<>();

        for(MethodDefinition method : MethodManager.getInstance().getMethods().values())
        {
            StringBuilder params = new StringBuilder();
            int i = 0;
            while(method.getParameters().containsKey(i))
            {
                var param = method.getParameters().get(i);
                if(param == null)
                    break;
                if(i > 0)
                {
                    params.append(", ");
                }
                params.append(param.getValue().name().toLowerCase()).append(" ").append(param.getKey());
                i++;
            }
            String returnType = "";
            if(method.getReturnType() != null && !method.getReturnType().equals(Type.VOID))
            {
                returnType = "<" + method.getReturnType().name() + ">";
            }
            String name = method.getName() + "(";
            if(params.length() == 0)
            {
                name += params + ")";
            }
            else
            {
                returnType += " ";
            }
            completions.add(new BasicCompletion(provider, name, returnType + params));
        }

        //NpcFilters
        for(NpcFilterType filter : NpcFilterType.values())
        {
            completions.add(new BasicCompletion(provider, filter.name().toUpperCase(), filter.getDescription()));
        }

        completions.add(new BasicCompletion(provider, "while(", "CONDITION) { ... }"));
        completions.add(new BasicCompletion(provider, "if(", "CONDITION) { ... }"));
        completions.add(new BasicCompletion(provider, "continue();", ""));
        completions.add(new BasicCompletion(provider, "break();", ""));
        completions.add(new BasicCompletion(provider, "die();", ""));
        completions.add(new BasicCompletion(provider, "register(", "String event) { ... }"));
        completions.add(new BasicCompletion(provider, "function(", "String name) { ... }"));

        for(Class<?> event : MethodManager.getInstance().getEventClasses())
        {
            completions.add(new BasicCompletion(provider, event.getSimpleName(), " [event]"));
        }

        provider.addCompletions(completions);
        return provider;
    }

    /**
     * Generates documentation of all the methods in the MethodManager
     * @param manager the method manager
     * @return the string of all the methods
     */
    public static String genDocs(MethodManager manager)
    {
        StringBuilder docs = new StringBuilder("SCRIPTING DOCS\n* * * * * * * * *\n");

        List<GroupDefinition> groups = manager.getMethodClasses();
        for (GroupDefinition groupDefinition : groups)
        {
            List<MethodDefinition> methods = groupDefinition.methods(manager);
            docs.append("\n# ").append(groupDefinition.groupName()).append("\n");
            for (MethodDefinition method : methods)
            {
                StringBuilder params = new StringBuilder();
                int i = 0;
                while(method.getParameters().containsKey(i))
                {
                    var param = method.getParameters().get(i);
                    if(param == null)
                        break;
                    if(i > 0)
                    {
                        params.append(", ");
                    }
                    params.append(param.getValue().name().toLowerCase()).append(" ").append(param.getKey());
                    i++;
                }
                String returnType = method.getReturnType() == null || method.getReturnType().equals(Type.VOID) ? "" : "<" + method.getReturnType().name().toLowerCase() + "> ";
                String name = method.getName() + "(";
                docs.append("// ").append(method.getDescription()).append("\n").append(returnType).append(name).append(params).append(");\n");
            }
        }

        //NpcFilters
        docs.append("\n# ").append("NPC Filters").append("\n");
        for(NpcFilterType filter : NpcFilterType.values())
        {
            docs.append("// " + filter.getDescription() + "\n" + filter.getName() + "\n");
        }

        docs.append("\n# ").append("Built-In Constructs").append("\n");
        docs.append("// while loop\nwhile(CONDITION) { ... }\n");
        docs.append("// if statement\nif(CONDITION) { ... }\n");
        docs.append("// continue statement\ncontinue();\n");
        docs.append("// break statement\nbreak();\n");
        docs.append("// die statement\ndie();\n");
        docs.append("// register event\nregister(String event) { ... }\n");
        docs.append("// function declaration\nfunction(String name) { ... }\n");
        docs.append("// end execution in a user defined method early\n<optional any> return(any optionalReturnValue);\n");

        docs.append("\n# ").append("Subscribable Events").append("\n");
        List<Class<?>> events = manager.getEventClasses();
        for (Class<?> event : events)
        {
            docs.append("* ").append(event.getSimpleName()).append("\n");
        }
        return docs.toString();
    }
}