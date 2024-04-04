package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.widgets.Widgets;

public class TDialogue
{
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
