package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TPackets;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import java.util.ArrayList;
import java.util.List;

public class GPackets implements GroupDefinition
{
    @Override
    public String groupName() {
        return "Packets";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "playerPacket",
                ImmutableMap.of(
                        0, Pair.of("action", Type.INT),
                        1, Pair.of("playerIndex", Type.INT)
                ),
                function ->
                {
                    int action = function.getArg(0, manager);
                    int id = function.getArg(1, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendPlayerActionPacket(action, id, false);
                }, "Interact with a player using a packet");
        addMethod(methods, "npcPacket",
                ImmutableMap.of(
                        0, Pair.of("action", Type.INT),
                        1, Pair.of("npcIndex", Type.INT)
                ),
                function ->
                {
                    int action = function.getArg(0, manager);
                    int npcIndex = function.getArg(1, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendNpcActionPacket(action, npcIndex, false);
                }, "Interact with an npc using a packet");
        addMethod(methods, "objectPacket",
                ImmutableMap.of(
                        0, Pair.of("action", Type.INT),
                        1, Pair.of("objectId", Type.INT),
                        2, Pair.of("worldX", Type.INT),
                        3, Pair.of("worldY", Type.INT)
                ),
                function ->
                {
                    int id = function.getArg(1, manager);
                    int action = function.getArg(0, manager);
                    int x = function.getArg(2, manager);
                    int y = function.getArg(3, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendObjectActionPacket(action, id, x, y, false);
                }, "Interact with a tile object using a packet");
        addMethod(methods, "itemPacket",
                ImmutableMap.of(
                        0, Pair.of("itemID", Type.INT),
                        1, Pair.of("action", Type.INT),
                        2, Pair.of("slot", Type.INT)
                ),
                function ->
                {
                    int id = function.getArg(0, manager);
                    int action = function.getArg(1, manager);
                    int slot = function.getArg(2, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendItemActionPacket(slot, id, action);
                }, "Interact with an item using a packet");

        addMethod(methods, "groundItemPacket",
                ImmutableMap.of(
                        0, Pair.of("itemId", Type.INT),
                        1, Pair.of("action", Type.INT),
                        2, Pair.of("worldX", Type.INT),
                        3, Pair.of("worldY", Type.INT)
                ),
                function ->
                {
                    int id = function.getArg(0, manager);
                    int action = function.getArg(1, manager);
                    int x = function.getArg(2, manager);
                    int y = function.getArg(3, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendGroundItemActionPacket(action, id, x, y, false);
                }, "Interact with a ground item using a packet");

        addMethod(methods, "widgetPacket",
                ImmutableMap.of(
                        0, Pair.of("action", Type.INT),
                        1, Pair.of("widgetId", Type.INT),
                        2, Pair.of("childId", Type.INT),
                        3, Pair.of("itemId", Type.INT)
                ),
                function ->
                {
                    int action = function.getArg(0, manager);
                    int widgetId = function.getArg(1, manager);
                    int itemId = function.getArg(2, manager);
                    int childId = function.getArg(3, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWidgetActionPacket(action, widgetId, itemId, childId);
                }, "Interact with a widget using a packet");

        addMethod(methods, "walkPacket",
                ImmutableMap.of(
                        0, Pair.of("worldX", Type.INT),
                        1, Pair.of("worldY", Type.INT)
                ),
                function ->
                {
                    int x = function.getArg(0, manager);
                    int y = function.getArg(1, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWalkPacket(x, y, false);
                }, "send a packet to walk to a coordinate");

        addMethod(methods, "widgetOnWidgetPacket",
                ImmutableMap.<Integer, Pair<String,Type>>builder()
                        .put(0, Pair.of("selectedWidgetId", Type.INT))
                        .put(1, Pair.of("itemId", Type.INT))
                        .put(2, Pair.of("slot", Type.INT))
                        .put(3, Pair.of("targetWidgetId", Type.INT))
                        .put(4, Pair.of("itemId2", Type.INT))
                        .put(5, Pair.of("slot2", Type.INT))
                        .build(),
                function ->
                {
                    int selectedWidgetId = function.getArg(0, manager);
                    int itemId = function.getArg(1, manager);
                    int slot = function.getArg(2, manager);
                    int targetWidgetId = function.getArg(3, manager);
                    int itemId2 = function.getArg(4, manager);
                    int slot2 = function.getArg(5, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWidgetOnWidgetPacket(selectedWidgetId, itemId, slot, targetWidgetId, itemId2, slot2);
                }, "send widget on target widget packet");


        //TODO: Add logging for below

        addMethod(methods, "resumeCountDialoguePacket",
                ImmutableMap.of(
                        0, Pair.of("count", Type.INT)
                ),
                function ->
                {
                    int count = function.getArg(0, manager);
                    TPackets.sendResumeCountDialoguePacket(count);
                }, "send resume count dialogue packet");

        addMethod(methods, "resumePauseWidgetPacketPacket",
                ImmutableMap.of(
                        0, Pair.of("widgetID", Type.INT),
                        1, Pair.of("optionIndex", Type.INT)
                ),
                function ->
                {
                    int widgetID = function.getArg(0, manager);
                    int optionIndex = function.getArg(1, manager);
                    TPackets.sendResumePauseWidgetPacket(widgetID, optionIndex);
                }, "send resume pause widget packet packet");

        addMethod(methods, "resumeObjectDialoguePacketPacket",
                ImmutableMap.of(
                        0, Pair.of("id", Type.INT)
                ),
                function ->
                {
                    int id = function.getArg(0, manager);
                    TPackets.sendResumeObjectDialoguePacket(id);
                }, "send resume object dialogue packet packet");

        addMethod(methods, "resumeNameDialoguePacket",
                ImmutableMap.of(
                        0, Pair.of("text", Type.STRING)
                ),
                function ->
                {
                    String text = function.getArg(1, manager);
                    TPackets.sendResumeNameDialoguePacket(text);
                }, "send resume name dialogue packet");

        addMethod(methods, "widgetOnObjectPacket",
                ImmutableMap.<Integer, Pair<String,Type>>builder()
                        .put(0, Pair.of("selectedWidgetId", Type.INT))
                        .put(1, Pair.of("itemId", Type.INT))
                        .put(2, Pair.of("slot", Type.INT))
                        .put(3, Pair.of("objectID", Type.INT))
                        .put(4, Pair.of("worldX", Type.INT))
                        .put(5, Pair.of("worldY", Type.INT))
                        .put(6, Pair.of("ctrl", Type.BOOL))
                        .build(),
                function ->
                {
                    int selectedWidgetId = function.getArg(0, manager);
                    int itemId = function.getArg(1, manager);
                    int slot = function.getArg(2, manager);
                    int objectID = function.getArg(3, manager);
                    int worldX = function.getArg(4, manager);
                    int worldY = function.getArg(5, manager);
                    boolean ctrl = function.getArg(6, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWidgetOnObjectPacket(selectedWidgetId, itemId, slot, objectID, worldX, worldY, ctrl);
                }, "send widget on object packet");

        addMethod(methods, "widgetOnGroundItemPacket",
                ImmutableMap.<Integer, Pair<String,Type>>builder()
                        .put(0, Pair.of("selectedWidgetId", Type.INT))
                        .put(1, Pair.of("itemId", Type.INT))
                        .put(2, Pair.of("slot", Type.INT))
                        .put(3, Pair.of("groundItemID", Type.INT))
                        .put(4, Pair.of("worldX", Type.INT))
                        .put(5, Pair.of("worldY", Type.INT))
                        .put(6, Pair.of("ctrl", Type.BOOL))
                        .build(),
                function ->
                {
                    int selectedWidgetId = function.getArg(0, manager);
                    int itemId = function.getArg(1, manager);
                    int slot = function.getArg(2, manager);
                    int groundItemID = function.getArg(3, manager);
                    int worldX = function.getArg(4, manager);
                    int worldY = function.getArg(5, manager);
                    boolean ctrl = function.getArg(6, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWidgetOnGroundItemPacket(selectedWidgetId, itemId, slot, groundItemID, worldX, worldY, ctrl);
                }, "send widget on ground item packet");

        addMethod(methods, "widgetOnNpcPacket",
                ImmutableMap.of(
                        0, Pair.of("selectedWidgetId", Type.INT),
                        1, Pair.of("itemId", Type.INT),
                        2, Pair.of("slot", Type.INT),
                        3, Pair.of("npcIndex", Type.INT),
                        4, Pair.of("ctrl", Type.BOOL)
                ),
                function ->
                {
                    int selectedWidgetId = function.getArg(0, manager);
                    int itemId = function.getArg(1, manager);
                    int slot = function.getArg(2, manager);
                    int npcIndex = function.getArg(3, manager);
                    boolean ctrl = function.getArg(4, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWidgetOnNpcPacket(selectedWidgetId, itemId, slot, npcIndex, ctrl);
                }, "send widget on npc packet");

        addMethod(methods, "widgetOnPlayerPacket",
                ImmutableMap.of(
                        0, Pair.of("selectedWidgetId", Type.INT),
                        1, Pair.of("itemId", Type.INT),
                        2, Pair.of("slot", Type.INT),
                        3, Pair.of("playerIndex", Type.INT),
                        4, Pair.of("ctrl", Type.BOOL)
                ),
                function ->
                {
                    int selectedWidgetId = function.getArg(0, manager);
                    int itemId = function.getArg(1, manager);
                    int slot = function.getArg(2, manager);
                    int playerIndex = function.getArg(3, manager);
                    boolean ctrl = function.getArg(4, manager);
                    TPackets.sendClickPacket();
                    TPackets.sendWidgetOnPlayerPacket(selectedWidgetId, itemId, slot, playerIndex, ctrl);
                }, "send widget on player packet");

        addMethod(methods, "interfaceClosePacket",
                ImmutableMap.of(),
                function ->
                {
                    TPackets.sendClickPacket();
                    TPackets.sendInterfaceClosePacket();
                }, "send interface close packet");

        return methods;
    }
}
