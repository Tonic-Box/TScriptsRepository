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
        return methods;
    }
}
