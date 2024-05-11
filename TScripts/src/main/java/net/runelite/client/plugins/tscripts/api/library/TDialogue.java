package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.tscripts.api.enums.VarrockMuseumAnswer;
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
            TPackets.sendResumePauseWidgetPacket(WidgetInfo.DIALOG_OPTION_OPTION1.getId(), option);
            return null;
        });
    }

    public static void makeX(int quantity)
    {
        TPackets.sendClickPacket();
        TPackets.sendResumePauseWidgetPacket(17694734, quantity);
    }

    public static void numericInput(int number)
    {
        TPackets.sendClickPacket();
        TPackets.sendResumeCountDialoguePacket(number);
        TClientScript.closeNumericInputDialogue();
    }

    /**
     * Interact with a dialogue option
     * @param option the option to interact with
     * @return true if the option was interacted with
     */
    public static boolean interact(String option) {
        Boolean out = TGame.invoke(() -> {
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
        return out != null && out;
    }

    /**
     * Generic continue any pause dialogue
     * @return true if there was a dialogue to continue
     */
    public static boolean continueDialogue() {
        Boolean out = TGame.invoke(() -> {
            if (Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 5) != null) {
                TPackets.sendResumePauseWidgetPacket(WidgetInfo.PACK(WidgetID.DIALOG_NPC_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(633, 0) != null) {
                TPackets.sendResumePauseWidgetPacket(WidgetInfo.PACK(633, 0), -1);
                return true;
            }
            else if (Widgets.get(WidgetID.DIALOG_PLAYER_GROUP_ID, 5) != null) {
                TPackets.sendResumePauseWidgetPacket(WidgetInfo.PACK(WidgetID.DIALOG_PLAYER_GROUP_ID, 5), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG_SPRITE) != null) {
                TPackets.sendResumePauseWidgetPacket(WidgetInfo.DIALOG_SPRITE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.DIALOG2_SPRITE) != null) {
                TPackets.sendResumePauseWidgetPacket(WidgetInfo.DIALOG2_SPRITE_CONTINUE.getId(), -1);
                return true;
            }
            else if (Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.MINIGAME_DIALOG_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidgetPacket(WidgetInfo.MINIGAME_DIALOG_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    net.runelite.client.plugins.tscripts.api.library.TPackets.sendResumePauseWidgetPacket(WidgetInfo.DIALOG_NOTIFICATION_CONTINUE.getId(), -1);
                    return true;
                }
            }
            else if (Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE) != null) {
                Widget w = Widgets.get(WidgetInfo.LEVEL_UP_CONTINUE);
                if(w != null && w.getText() != null && w.getText().equals("Click here to continue"))
                {
                    TPackets.sendResumePauseWidgetPacket(WidgetInfo.LEVEL_UP_CONTINUE.getId(), -1);
                    return true;
                }
            }
            return false;
        });
        return out != null && out;
    }

    public static boolean isDialogueOpen() {
        Boolean out = TGame.invoke(() -> {
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
        return out != null && out;
    }

    public static boolean continueQuestHelper() {
        Widget widget = Static.getClient().getWidget(WidgetInfo.DIALOG_OPTION_OPTION1);
        if(widget == null)
            return false;
        Widget[] dialogOption1kids = widget.getChildren();
        if(dialogOption1kids == null)
            return false;
        int i = 0;
        for(Widget w : dialogOption1kids) {
            if(w.getTextColor() == -0xff4d4e) {
                interact(i);
                return true;
            }
            ++i;
        }
        return false;
    }

    public static void continueAllDialogue()
    {
        while(true)
        {
            if(!continueDialogue())
            {
                if(!continueQuestHelper())
                {
                    if(!continueMuseumQuiz())
                    {
                        TDelay.tick(1);
                        break;
                    }
                }
            }
            if(!TDelay.tick(1))
                break;
        }
    }

    public static boolean continueMuseumQuiz() {
        Widget questionWidget = Static.getClient().getWidget(WidgetInfo.VARROCK_MUSEUM_QUESTION);
        if(questionWidget == null)
            return false;

        final Widget answerWidget = VarrockMuseumAnswer.findCorrect(
                Static.getClient(),
                questionWidget.getText(),
                WidgetInfo.VARROCK_MUSEUM_FIRST_ANSWER,
                WidgetInfo.VARROCK_MUSEUM_SECOND_ANSWER,
                WidgetInfo.VARROCK_MUSEUM_THIRD_ANSWER);

        if (answerWidget == null)
            return false;

        TPackets.sendWidgetActionPacket(1, answerWidget.getId(), -1, -1);
        return true;
    }
}
