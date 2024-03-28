package net.runelite.client.plugins.tscripts.util.packets;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.types.MapEntry;
import net.runelite.client.plugins.tscripts.types.PacketDefinition;
import net.runelite.client.plugins.tscripts.util.Logging;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reads and writes packet definitions from a JSON file
 */
public class PacketMapReader
{
    private static List<MapEntry> defs;
    private static final Gson gson = new GsonBuilder().create();

    /**
     * Get all packet definitions
     * @return list of packet definitions
     */
    public static List<MapEntry> get()
    {
        if(defs == null)
        {
            fillMaps();
        }
        return defs;
    }

    /**
     * Get a packet definition by name
     * @param packet packet name
     * @return packet definition
     */
    public static MapEntry get(String packet)
    {
        if(defs == null)
        {
            fillMaps();
        }
        return defs.stream()
                .filter(e -> e.getName().equals(packet))
                .findFirst().orElse(null);
    }

    /**
     * Analyzes a packet buffer and returns a packet definition.
     *
     * @param buffer The packet buffer to analyze.
     * @return The packet definition.
     */
    public static PacketDefinition analyze(PacketBuffer buffer)
    {
        MapEntry entry = get().stream()
                .filter(e -> e.getPacket().getId() == buffer.getPacketId())
                .findFirst().orElse(null);

        if(entry == null)
        {
            return null;
        }

        PacketDefinition definition = new PacketDefinition(entry.getName(), buffer);

        for(int i = 0; i < entry.getReads().size(); i++)
        {
            if(isParsableAsNumber(entry.getArgs().get(i)))
            {
                doRead(buffer, entry.getReads().get(i)); //shit to ignore
                continue;
            }
            definition.getMap().put(entry.getArgs().get(i), doRead(buffer, entry.getReads().get(i)));
        }
        buffer.setOffset(0);
        return definition;
    }

    /**
     * Formats a pretty String representing the buffer based on what packet it is.
     *
     * @param buffer The packet buffer to prettify.
     * @return A string representation of the packet buffer.
     */
    public static String prettify(PacketBuffer buffer)
    {
        MapEntry entry = get().stream()
                .filter(e -> e.getPacket().getId() == buffer.getPacketId())
                .findFirst().orElse(null);
        if(entry == null)
        {
            return "[UNKNOWN(" + buffer.getPacketId() + ")] " + buffer;
        }

        StringBuilder out = new StringBuilder("[" + entry.getName() + "(" + entry.getPacket().getId() + ")] ");
        for(int i = 0; i < entry.getReads().size(); i++)
        {
            if(isParsableAsNumber(entry.getArgs().get(i)))
            {
                doRead(buffer, entry.getReads().get(i)); //shit to ignore
                continue;
            }
            out.append(entry.getArgs().get(i)).append("=").append(doRead(buffer, entry.getReads().get(i))).append(", ");
        }
        buffer.setOffset(0);
        return out.toString();
    }

    /**
     * Performs a read operation on the packet buffer based on the provided method.
     *
     * @param buffer The packet buffer to read from.
     * @param method The read method to use.
     * @return The read value as a long.
     */
    private static long doRead(PacketBuffer buffer, String method) {
        switch (method) {
            case "readByte":
                return buffer.readByte();
            case "readByteAdd":
                return buffer.readByteAdd();
            case "readByteNeg":
                return buffer.readByteNeg();
            case "readByteSub":
                return buffer.readByteSub();
            case "readLengthByte":
                return buffer.readLengthByte();
            case "readBoolean":
                return buffer.readBoolean() ? 1 : 0;
            case "readBooleanAdd":
                return buffer.readBooleanAdd() ? 1 : 0;
            case "readBooleanNeg":
                return buffer.readBooleanNeg() ? 1 : 0;
            case "readBooleanSub":
                return buffer.readBooleanSub() ? 1 : 0;
            case "readShort":
                return buffer.readUnsignedShort();
            case "readShortAdd":
                return buffer.readUnsignedShortAdd();
            case "readShortLE":
                return buffer.readUnsignedShortLE();
            case "readShortAddLE":
                return buffer.readUnsignedShortAddLE();
            case "readLengthShort":
                return buffer.readLengthShort();
            case "readMedium":
                return buffer.readMedium();
            case "readInt":
                return buffer.readInt();
            case "readIntME":
                return buffer.readIntME();
            case "readIntLE":
                return buffer.readIntLE();
            case "readIntIME":
                return buffer.readIntIME();
            case "readVarInt":
                return buffer.readVarInt();
            case "readLengthInt":
                return buffer.readLengthInt();
            case "readLong":
                return buffer.readLong();
            case "readFloat":
                return (int) buffer.readFloat();
            default:
                return -1;
        }
    }

    /**
     * Creates a packet buffer from a map entry and a map of arguments.
     *
     * @param entry The map entry.
     * @param args The map of arguments.
     * @return The created packet buffer.
     */
    public static PacketBuffer createBuffer(MapEntry entry, Map<String,Object> args)
    {
        PacketBuffer buffer = new PacketBuffer(entry.getPacket().getId(), entry.getPacket().getLength());
        for(int i = 0; i < entry.getWrites().size(); i++)
        {
            if (args.get(entry.getArgs().get(i)) != null)
            {
                Object object = args.get(entry.getArgs().get(i));
                if(object instanceof Integer)
                {
                    doWrite(buffer, entry.getWrites().get(i), (int) object);
                }
                else if(object instanceof String)
                {
                    doWriteStr(buffer, entry.getWrites().get(i), (String) object);
                }

            }
            else if(isParsableAsNumber(entry.getArgs().get(i)))
            {
                doWrite(buffer, entry.getWrites().get(i), Integer.parseInt(entry.getArgs().get(i)));
            }
            else if(entry.getArgs().get(i).equals("true") || entry.getArgs().get(i).equals("false"))
            {
                doWrite(buffer, entry.getWrites().get(i), (entry.getArgs().get(i).equals("true") ? 1 : 0));
            }
        }
        return buffer;
    }

    /**
     * Writes a string to the packet buffer based on the provided method and value.
     *
     * @param buffer The packet buffer to write to.
     * @param method The write method to use.
     * @param value The string value to write.
     */
    private static void doWriteStr(PacketBuffer buffer, String method, String value)
    {
        switch (method)
        {
            case "writeStringCp1252NullTerminated":
                buffer.writeStringCp1252NullTerminated(value);
                break;
            case "writeStringCp1252NullCircumfixed":
                buffer.writeStringCp1252NullCircumfixed(value);
                break;
            case "writeCESU8":
                buffer.writeCESU8(value);
                break;
        }
    }

    /**
     * Performs a write operation on the packet buffer based on the provided method and value.
     *
     * @param buffer The packet buffer to write to.
     * @param method The write method to use.
     * @param value The value to write.
     */
    private static void doWrite(PacketBuffer buffer, String method, int value) {
        switch (method) {
            case "writeByte":
                buffer.writeByte(value);
                break;
            case "writeByteAdd":
                buffer.writeByteAdd(value);
                break;
            case "writeByteNeg":
                buffer.writeByteNeg(value);
                break;
            case "writeByteSub":
                buffer.writeByteSub(value);
                break;
            case "writeLengthByte":
                buffer.writeLengthByte(value);
                break;
            case "writeShort":
                buffer.writeShort(value);
                break;
            case "writeShortAdd":
                buffer.writeShortAdd(value);
                break;
            case "writeShortLE":
                buffer.writeShortLE(value);
                break;
            case "writeShortAddLE":
                buffer.writeShortAddLE(value);
                break;
            case "writeLengthShort":
                buffer.writeLengthShort(value);
                break;
            case "writeMedium":
                buffer.writeMedium(value);
                break;
            case "writeInt":
                buffer.writeInt(value);
                break;
            case "writeIntME":
                buffer.writeIntME(value);
                break;
            case "writeIntLE":
                buffer.writeIntLE(value);
                break;
            case "writeIntIME":
                buffer.writeIntIME(value);
                break;
            case "writeVarInt":
                buffer.writeVarInt(value);
                break;
            case "writeLengthInt":
                buffer.writeLengthInt(value);
                break;
            case "writeLong":
                buffer.writeLong(value);
                break;
            case "writeFloat":
                buffer.writeFloat(value);
                break;
        }
    }

    /**
     * Checks if a string can be parsed as a number.
     *
     * @param str The string to check.
     * @return True if the string can be parsed as a number, false otherwise.
     */
    public static boolean isParsableAsNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Fills the list of map entries with packet definitions from a JSON file.
     */
    public static void fillMaps()
    {
        try {
            InputStream inputStream = TScriptsPlugin.class.getResourceAsStream("packets.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String fileContent = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            defs = gson.fromJson(fileContent, new TypeToken<ArrayList<MapEntry>>(){}.getType());
        } catch (Exception ex) {
            Logging.errorLog(ex);
            defs = new ArrayList<>();
        }
    }
}
