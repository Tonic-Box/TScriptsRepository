package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.THttp;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GHttp implements GroupDefinition
{
    @Override
    public String groupName() {
        return "HTTP";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "httpGet", Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("url", Type.STRING),
                        1, Pair.of("timeoutMS", Type.INT)
                ),
                function -> THttp.get(function.getArg(0, manager), function.getArg(1, manager)),
                "Sends a GET request to the specified URL", false
        );

        addMethod(methods, "httpPost", Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("url", Type.STRING),
                        1, Pair.of("data", Type.STRING),
                        2, Pair.of("timeoutMS", Type.INT)
                ),
                function -> THttp.post(function.getArg(0, manager), function.getArg(1, manager), function.getArg(2, manager)),
                "Sends a POST request to the specified URL. data in the form\n" +
                        "of \"param1=value1&param2=value2\" an so on", false
        );

        return methods;
    }
}
