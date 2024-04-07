package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.client.Static;

public class TClientScript
{
    public static void closeNumericInputDialogue()
    {
        TDelay.invokeLater(() -> {
            Client client = Static.getClient();
            Widget w = client.getWidget(WidgetInfo.CHATBOX_INPUT);
            Widget w2 = client.getWidget(WidgetInfo.CHATBOX_FULL_INPUT);
            if(w != null || w2 != null)
            {
                client.runScript(138);
            }
        }, 2);
    }
}
