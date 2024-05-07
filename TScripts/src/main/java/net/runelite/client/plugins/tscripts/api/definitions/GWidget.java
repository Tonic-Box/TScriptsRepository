package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Player;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TPlayer;
import net.runelite.client.plugins.tscripts.api.library.TWidget;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;

import java.util.ArrayList;
import java.util.List;

public class GWidget implements GroupDefinition
{

    @Override
    public String groupName() {
        return "Widget";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "getWidgetText", Type.STRING,
                ImmutableMap.of(
                        0, Pair.of("groupId", Type.INT),
                        1, Pair.of("widgetId", Type.INT)
                ),
                function ->
                {
                    int groupId = function.getArg(0, manager);
                    int widgetId = function.getArg(1, manager);
                    return TWidget.getText(groupId, widgetId);
                }, "gets a widget's text");

        addMethod(methods, "setWidgetText",
                ImmutableMap.of(
                        0, Pair.of("groupId", Type.INT),
                        1, Pair.of("widgetId", Type.INT),
                        2, Pair.of("text", Type.STRING)
                ),
                function ->
                {
                    int groupId = function.getArg(0, manager);
                    int widgetId = function.getArg(1, manager);
                    String text = function.getArg(2, manager);
                    Widget widget =  TWidget.getWidget(groupId, widgetId);
                    if(widget == null)
                        return;
                    widget.setText(text);
                }, "sets a widget's text");

        return methods;
    }
}
