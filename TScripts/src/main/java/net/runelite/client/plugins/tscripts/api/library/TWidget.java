package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.widgets.Widget;
import net.unethicalite.client.Static;

public class TWidget
{
    public static String getText(int groupId, int widgetId)
    {
        Widget widget = getWidget(groupId, widgetId);
        if(widget == null)
            return null;
        return TGame.invoke(widget::getText);
    }

    public static Widget getWidget(int groupId, int widgetId)
    {
        return TGame.invoke(() -> Static.getClient().getWidget(groupId, widgetId));
    }

    public static boolean isVisible(int groupId, int widgetId)
    {
        Widget widget = getWidget(groupId, widgetId);
        if(widget == null)
            return false;
        return TGame.invoke(() -> widget.isVisible() && !widget.isHidden()) == Boolean.TRUE;
    }
}
