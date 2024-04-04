package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.packets.ClientPacket;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.tscripts.types.MapEntry;
import net.runelite.client.plugins.tscripts.util.packets.PacketMapReader;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.client.Static;
import java.util.HashMap;
import java.util.Map;

/**
 * TPackets class.
 */
public class TPackets
{
    public static void sendClickPacket(int mouseX, int mouseY)
    {
        Static.getClient().setMouseLastPressedMillis(System.currentTimeMillis());
        int mousePressedTime = ((int) (Static.getClient().getMouseLastPressedMillis() - Static.getClient().getClientMouseLastPressedMillis()));
        if (mousePressedTime < 0)
        {
            mousePressedTime = 0;
        }
        if (mousePressedTime > 32767)
        {
            mousePressedTime = 32767;
        }
        Static.getClient().setClientMouseLastPressedMillis(Static.getClient().getMouseLastPressedMillis());
        int mouseInfo = (mousePressedTime << 1);

        MapEntry entry = PacketMapReader.get("OP_MOUSE_CLICK");
        Map<String,Object> args = new HashMap<>();
        args.put("mouseInfo", mouseInfo);
        args.put("mouseX", mouseX);
        args.put("mouseY", mouseY);

        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().EVENT_MOUSE_CLICK());
    }

    public static void sendWalkPacket(int worldX, int worldY, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_WALK");
        Map<String,Object> args = new HashMap<>();
        args.put("worldX", worldX);
        args.put("worldY", worldY);
        args.put("ctrl", ctrl ? 1 : 0);
        
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().MOVE_GAMECLICK());
    }
    
    public static void sendWidgetActionPacket(int type, int widgetId, int childId, int itemId)
    {
        MapEntry entry = PacketMapReader.get("OP_WIDGET_ACTION_" + type);
        if(entry == null)
        {
            System.err.println("Packets::sendWidgetActionPacket invalid type [" + type + "]");
            return;
        }
        Map<String,Object> args = new HashMap<>();
        args.put("widgetId", widgetId);
        args.put("childId", childId);
        args.put("itemId", itemId);
        
        ClientPacket clientPacket = null;
        
        switch (type)
        {
            case 0:
                clientPacket = Static.getClientPacket().IF_BUTTON1();
                break;
            case 1:
                clientPacket = Static.getClientPacket().IF_BUTTON2();
                break;
            case 2:
                clientPacket = Static.getClientPacket().IF_BUTTON3();
                break; 
            case 3:
                clientPacket = Static.getClientPacket().IF_BUTTON4();
                break;
            case 4:
                clientPacket = Static.getClientPacket().IF_BUTTON5();
                break;
            case 5:
                clientPacket = Static.getClientPacket().IF_BUTTON6();
                break;
            case 6:
                clientPacket = Static.getClientPacket().IF_BUTTON7();
                break;
            case 7:
                clientPacket = Static.getClientPacket().IF_BUTTON8();
                break;
            case 8:
                clientPacket = Static.getClientPacket().IF_BUTTON9();
                break;
            case 9:
                clientPacket = Static.getClientPacket().IF_BUTTON10();
                break;
        }
        if(clientPacket == null)
            return;

        PacketMapReader.createBuffer(entry, args).send(clientPacket);
    }
    
    public static void sendResumeCountDialogue(int count)
    {
        MapEntry entry = PacketMapReader.get("OP_RESUME_COUNTDIALOG");
        Map<String,Object> args = new HashMap<>();
        args.put("count", count);

        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().RESUME_COUNTDIALOG());
    }
    
    public static void sendResumePauseWidget(int widgetID, int optionIndex)
    {
        MapEntry entry = PacketMapReader.get("OP_RESUME_PAUSEBUTTON");
        Map<String,Object> args = new HashMap<>();
        args.put("widgetID", widgetID);
        args.put("optionIndex", optionIndex);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().RESUME_PAUSEBUTTON());
    }
    
    public static void sendResumeObjectDialogue(int id) {
        MapEntry entry = PacketMapReader.get("OP_RESUME_OBJDIALOG");
        Map<String,Object> args = new HashMap<>();
        args.put("id", id);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().RESUME_OBJDIALOG());
    }
    
    public static void sendResumeNameDialogue(String text) {
        MapEntry entry = PacketMapReader.get("OP_RESUME_NAMEDIALOG");
        Map<String,Object> args = new HashMap<>();
        args.put("length", text.length());
        args.put("var7", text);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().RESUME_NAMEDIALOG());
    }
    
    public static void sendPlayerActionPacket(int type, int playerIndex, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_PLAYER_ACTION_" + type);
        if(entry == null)
        {
            System.err.println("Packets::sendPlayerActionPacket invalid type [" + type + "]");
            return;
        }

        Map<String,Object> args = new HashMap<>();
        args.put("identifier", playerIndex);
        args.put("ctrl", ctrl ? 1 : 0);

        ClientPacket clientPacket = null;

        switch (type)
        {
            case 0:
                clientPacket = Static.getClientPacket().OPPLAYER1();
                break;
            case 1:
                clientPacket = Static.getClientPacket().OPPLAYER2();
                break;
            case 2:
                clientPacket = Static.getClientPacket().OPPLAYER3();
                break;
            case 3:
                clientPacket = Static.getClientPacket().OPPLAYER4();
                break;
            case 4:
                clientPacket = Static.getClientPacket().OPPLAYER5();
                break;
            case 5:
                clientPacket = Static.getClientPacket().OPPLAYER6();
                break;
            case 6:
                clientPacket = Static.getClientPacket().OPPLAYER7();
                break;
            case 7:
                clientPacket = Static.getClientPacket().OPPLAYER8();
                break;
        }
        if(clientPacket == null)
            return;

        PacketMapReader.createBuffer(entry, args).send(clientPacket);
    }
    
    public static void sendNpcActionPacket(int type, int npcIndex, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_NPC_ACTION_" + type);
        if(entry == null)
        {
            System.err.println("Packets::sendNpcActionPacket invalid type [" + type + "]");
            return;
        }
        Map<String,Object> args = new HashMap<>();
        args.put("identifier", npcIndex);
        args.put("ctrl", ctrl ? 1 : 0);

        ClientPacket clientPacket = null;
        switch (type)
        {
            case 0:
                clientPacket = Static.getClientPacket().OPNPC1();
                break;
            case 1:
                clientPacket = Static.getClientPacket().OPNPC2();
                break;
            case 2:
                clientPacket = Static.getClientPacket().OPNPC3();
                break;
            case 3:
                clientPacket = Static.getClientPacket().OPNPC4();
                break;
            case 4:
                clientPacket = Static.getClientPacket().OPNPC5();
                break;
        }
        if(clientPacket == null)
            return;

        PacketMapReader.createBuffer(entry, args).send(clientPacket);
    }

    public static void sendObjectActionPacket(int type, int identifier, int worldX, int worldY, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_GAME_OBJECT_ACTION_" + type);
        if(entry == null)
        {
            System.err.println("Packets::sendObjectActionPacket invalid type [" + type + "]");
            return;
        }

        Map<String,Object> args = new HashMap<>();
        args.put("identifier", identifier);
        args.put("ctrl", ctrl ? 1 : 0);
        args.put("worldX", worldX);
        args.put("worldY", worldY);

        ClientPacket clientPacket = null;
        switch (type)
        {
            case 0:
                clientPacket = Static.getClientPacket().OPLOC1();
                break;
            case 1:
                clientPacket = Static.getClientPacket().OPLOC2();
                break;
            case 2:
                clientPacket = Static.getClientPacket().OPLOC3();
                break;
            case 3:
                clientPacket = Static.getClientPacket().OPLOC4();
                break;
            case 4:
                clientPacket = Static.getClientPacket().OPLOC5();
                break;
        }
        if(clientPacket == null)
            return;

        PacketMapReader.createBuffer(entry, args).send(clientPacket);
    }

    public static void sendGroundItemActionPacket(int type, int identifier, int worldX, int worldY, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_GROUND_ITEM_ACTION_" + type);
        if(entry == null)
        {
            System.err.println("Packets::sendGroundItemActionPacket invalid type [" + type + "]");
            return;
        }

        Map<String,Object> args = new HashMap<>();
        args.put("identifier", identifier);
        args.put("ctrl", ctrl ? 1 : 0);
        args.put("worldX", worldX);
        args.put("worldY", worldY);

        ClientPacket clientPacket = null;
        switch (type)
        {
            case 0:
                clientPacket = Static.getClientPacket().OPOBJ1();
                break;
            case 1:
                clientPacket = Static.getClientPacket().OPOBJ2();
                break;
            case 2:
                clientPacket = Static.getClientPacket().OPOBJ3();
                break;
            case 3:
                clientPacket = Static.getClientPacket().OPOBJ4();
                break;
            case 4:
                clientPacket = Static.getClientPacket().OPOBJ5();
                break;
        }
        if(clientPacket == null)
            return;

        PacketMapReader.createBuffer(entry, args).send(clientPacket);
    }

    public static void sendWidgetOnObjectPacket(int selectedWidgetId, int itemId, int slot, int objectID, int worldX, int worldY, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_WIDGET_TARGET_ON_GAME_OBJECT");
        Map<String,Object> args = new HashMap<>();
        args.put("selectedWidgetId", selectedWidgetId);
        args.put("itemId", itemId);
        args.put("slot", slot);
        args.put("identifier", objectID);
        args.put("worldX", worldX);
        args.put("worldY", worldY);
        args.put("ctrl", ctrl ? 0 : 1);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().OPLOCT());
    }

    public static void sendWidgetOnGroundItemPacket(int selectedWidgetId, int itemId, int slot, int objectID, int worldX, int worldY, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_WIDGET_TARGET_ON_GROUND_ITEM");
        Map<String,Object> args = new HashMap<>();
        args.put("selectedWidgetId", selectedWidgetId);
        args.put("itemId", itemId);
        args.put("slot", slot);
        args.put("identifier", objectID);
        args.put("worldX", worldX);
        args.put("worldY", worldY);
        args.put("ctrl", ctrl ? 0 : 1);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().OPOBJT());
    }

    public static void sendWidgetOnNpcPacket(int selectedWidgetId, int itemId, int slot, int npcIndex, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_WIDGET_TARGET_ON_NPC");
        Map<String,Object> args = new HashMap<>();
        args.put("selectedWidgetId", selectedWidgetId);
        args.put("itemId", itemId);
        args.put("slot", slot);
        args.put("identifier", npcIndex);
        args.put("ctrl", ctrl ? 0 : 1);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().OPNPCT());
    }

    public static void sendWidgetOnPlayerPacket(int selectedWidgetId, int itemId, int slot, int playerIndex, boolean ctrl)
    {
        MapEntry entry = PacketMapReader.get("OP_WIDGET_TARGET_ON_PLAYER");
        Map<String,Object> args = new HashMap<>();
        args.put("selectedWidgetId", selectedWidgetId);
        args.put("itemId", itemId);
        args.put("slot", slot);
        args.put("identifier", playerIndex);
        args.put("ctrl", ctrl ? 0 : 1);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().OPPLAYERT());
    }

    public static void sendWidgetOnWidgetPacket(int selectedWidgetId, int itemId, int slot, int targetWidgetId, int itemId2, int slot2)
    {
        MapEntry entry = PacketMapReader.get("OP_WIDGET_TARGET_ON_WIDGET");
        Map<String,Object> args = new HashMap<>();
        args.put("selectedWidgetId", selectedWidgetId);
        args.put("itemId", itemId);
        args.put("slot", slot);
        args.put("targetWidgetID", targetWidgetId);
        args.put("identifier2", itemId2);
        args.put("param0", slot2);
        PacketMapReader.createBuffer(entry, args).send(Static.getClientPacket().IF_BUTTONT());
    }

    public static void sendItemActionPacket(int slot, int id, int action)
    {
        if(action == -1)
        {
            return;
        }

        sendWidgetActionPacket(action, WidgetInfo.INVENTORY.getId(), slot, id);
    }
}
