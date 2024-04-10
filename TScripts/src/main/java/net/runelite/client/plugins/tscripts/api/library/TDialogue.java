package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class TDialogue
{
    public static void interact(Object option)
    {
        if(option instanceof Integer)
            interact((int)option);
        else if(option instanceof String)
            interact((String)option);
    }
    public static void interact(int option)
    {
        TGame.invoke(() -> {
            TPackets.sendClickPacket();
            TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_OPTION_OPTION1.getId(), option);
            return null;
        });
    }

    public static void makeX(int quantity)
    {
        TGame.invoke(() -> {
            TPackets.sendClickPacket();
            TPackets.sendResumePauseWidget(17694734, quantity);
        });
    }

    /**
     * Interact with a dialogue option
     * @param option the option to interact with
     * @return true if the option was interacted with
     */
    public static boolean interact(String option) {
        return TGame.invoke(() -> {
            Client client = Static.getClient();
            Widget widget = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1);
            if(widget == null)
                return false;
            Widget[] dialogOption1kids = widget.getChildren();
            if(dialogOption1kids == null)
                return false;
            if(dialogOption1kids.length < 2)
                return false;
            int i = 0;
            for(Widget w : dialogOption1kids) {
                if(w.getText().toLowerCase().contains(option.toLowerCase())) {
                    interact(i);
                    return true;
                }
                i++;
            }
            return false;
        });
    }

    /**
     * Generic continue any pause dialogue
     * @return true if there was a dialogue to continue
     */
    public static boolean continueDialogue() {
        return TGame.invoke(() -> {
            if (Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 5) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.PACK(WidgetID.DIALOG_NPC_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(633, 0) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.PACK(633, 0), -1);
                return true;
            }
            else if (Widgets.get(WidgetID.DIALOG_PLAYER_GROUP_ID, 5) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.PACK(WidgetID.DIALOG_PLAYER_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG_SPRITE) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_SPRITE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG2_SPRITE) != null) {
                TPackets.sendResumePauseWidget(WidgetInfo.DIALOG2_SPRITE_CONTINUE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.MINIGAME_DIALOG_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidget(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    TPackets.sendResumePauseWidget(WidgetInfo.LEVEL_UP_CONTINUE.getId(), -1);
                    return true;
                }
            }
            return false;
        });
    }

    public static boolean isDialogueOpen() {
        return TGame.invoke(() -> {
            if (Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 5) != null) {
                return true;
            }
            else if (Widgets.get(633, 0) != null) {
                return true;
            }
            else if (Widgets.get(WidgetID.DIALOG_PLAYER_GROUP_ID, 5) != null) {
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG_SPRITE) != null) {
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG2_SPRITE) != null) {
                return true;
            }
            else if (Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE);
                return w != null && w.getText() != null && w.getText().equals("Click here to continue");
            }
            else if (Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
                return w != null && w.getText() != null && w.getText().equals("Click here to continue");
            }
            else if (Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE);
                return w != null && w.getText() != null && w.getText().equals("Click here to continue");
            }
            return false;
        });
    }
}
