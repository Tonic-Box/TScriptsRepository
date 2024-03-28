package net.runelite.client.plugins.tscripts.util.packets;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.runelite.client.plugins.tscripts.util.TextUtil;
import net.unethicalite.client.Static;
import java.math.BigInteger;

/**
 * Represents a packet buffer.
 */
@Getter
public class PacketBuffer {
    private final int packetId;
    @Setter
    private ByteBuf payload;

    @Setter
    private int offset;

    @Setter
    private int trueLength = 0;

    /**
     * Constructs a new packet buffer with the specified packet id and payload.
     *
     * @param packetId the packet id
     * @param payload the payload
     */
    public PacketBuffer(int packetId, byte[] payload) {
        this.packetId = packetId;
        this.payload = ByteBufferPool.allocate(payload.length);
        this.payload.writeBytes(payload);
        offset = 0;
    }

    /**
     * Constructs a new packet buffer with the specified packet id and size.
     *
     * @param packetId the packet id
     * @param size the size
     */
    public PacketBuffer(int packetId, int size)
    {
        this.packetId = packetId;
        if(size == -1)
            this.payload = ByteBufferPool.allocate();
        else
            this.payload = ByteBufferPool.allocate(size);
        offset = 0;
    }

    /**
     * Constructs a new packet buffer with the specified size.
     *
     * @param size the size
     */
    public PacketBuffer(int size)
    {
        this.packetId = -1;
        this.payload = ByteBufferPool.allocate(size);
        offset = 0;
    }

    /**
     * Sends the packet buffer. Releases the buffer after.
     *
     * @param clientPacket the client packet
     */
    public void send(ClientPacket clientPacket)
    {
        send(clientPacket, true);
    }

    /**
     * Sends the packet buffer.
     *
     * @param clientPacket the client packet
     * @param release whether to release the buffer after sending
     */
    public void send(ClientPacket clientPacket, boolean release)
    {
        Static.getClientThread().invoke(() -> {
            Client client = Static.getClient();
            PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket, client.getPacketWriter().getIsaacCipher());
            for(int i = 0; i < trueLength; i++)
            {
                packetBufferNode.getPacketBuffer().writeByte(payload.getByte(i));
            }
            packetBufferNode.send();
            if(release)
                release();
        });
    }

    public void encryptRsa(BigInteger var1, BigInteger var2) {
        int offset_L = this.offset;
        this.offset = 0;
        byte[] new_payload = new byte[offset_L];
        this.readBytes(new_payload, 0, offset_L);
        BigInteger var5 = new BigInteger(new_payload);
        BigInteger var6 = var5.modPow(var1, var2);
        byte[] var7 = var6.toByteArray();
        this.offset = 0;
        this.writeShort(var7.length);
        this.writeBytes(var7, 0, var7.length);
    }

    public void readBytes(byte[] var1, int var2, int var3) {
        for(int var4 = var2; var4 < var3 + var2; ++var4) {
            var1[var4] = payload.getByte(offset++);
        }
    }

    public byte readByte() {
        return payload.getByte(offset++);
    }

    public byte readByteAdd()
    {
        return (byte)(readByte() - 128);
    }

    public byte readByteNeg()
    {
        return (byte)(-readByte());
    }

    public byte readByteSub()
    {
        return (byte)(readByte() + 128);
    }

    public int readLengthByte() {
        int var = payload.getByte(offset - 1);
        if (var >= 0) {
            return var;
        }
        return -1; // or throw an exception indicating invalid data
    }

    public boolean readBoolean() {
        return readByte() != 0;
    }

    public boolean readBooleanAdd() {
        return readByteAdd() != 0;
    }

    public boolean readBooleanNeg() {
        return readByteNeg() != 0;
    }

    public boolean readBooleanSub() {
        return readByteSub() != 0;
    }

    public short readShort() {
        short value = (short)(((payload.getByte(offset) & 0xFF) << 8) | (payload.getByte(offset + 1) & 0xFF));
        offset += 2;
        return value;
    }

    public int readUnsignedShort() {
        int value = ((payload.getByte(offset) & 0xFF) << 8) | (payload.getByte(offset + 1) & 0xFF);
        offset += 2;
        return value;
    }

    public short readShortAdd() {
        //short value = (short)(((payload[offset] & 0xFF) << 8) | ((payload[offset + 1] & 0xFF) - 128));
        short value = (short)((payload.getByte(offset) & 0xFF) - 128 | ((payload.getByte(offset + 1) & 0xFF) << 8));
        offset += 2;
        return value;
    }

    public int readUnsignedShortAdd() {
        int low = payload.getByte(offset++) & 0xFF;
        if (low < 128) {
            low += 128;  // Adjust for the 128 added in the write method
        } else {
            low -= 128;  // Reverse the wrap-around effect
        }
        int high = (payload.getByte(offset++) & 0xFF) << 8;
        return high | low;
    }

    public short readShortLE() {
        short value = (short)((payload.getByte(offset + 1) & 0xFF) << 8 | (payload.getByte(offset) & 0xFF));
        offset += 2;
        return value;
    }

    public int readUnsignedShortLE() {
        int value = ((payload.getByte(offset + 1) & 0xFF) << 8) | (payload.getByte(offset) & 0xFF);
        offset += 2;
        return value;
    }

    public short readShortAddLE() {
        short value = (short)(((payload.getByte(offset) & 0xFF) << 8) | ((payload.getByte(offset + 1) & 0xFF) - 128));
        //short value = (short)((payload[offset] & 0xFF) - 128 | ((payload[offset + 1] & 0xFF) << 8));
        offset += 2;
        return value;
    }

    public int readUnsignedShortAddLE() {
        int value = ((payload.getByte(offset) & 0xFF) << 8) | ((payload.getByte(offset + 1) & 0xFF) - 128 & 0xFF);
        //int value = ((payload[offset] & 0xFF) - 128 & 0xFF) | ((payload[offset + 1] & 0xFF) << 8);
        offset += 2;
        return value;
    }

    public int readLengthShort() {
        if (offset < 2) {
            return -1;
        }
        int length = ((payload.getByte(offset - 2) & 0xFF) << 8) | (payload.getByte(offset - 1) & 0xFF);
        offset -= length + 2;
        return length;
    }

    public int readMedium() {
        offset += 3;
        int b1 = payload.getByte(offset - 3) & 0xFF;
        int b2 = payload.getByte(offset - 2) & 0xFF;
        int b3 = payload.getByte(offset - 1) & 0xFF;
        return (b1 << 16) | (b2 << 8) | b3;
    }

    public int readInt() {
        return ((payload.getByte(offset++) & 0xFF) << 24) |
                ((payload.getByte(offset++) & 0xFF) << 16) |
                ((payload.getByte(offset++) & 0xFF) << 8) |
                (payload.getByte(offset++) & 0xFF);
    }

    public long readUnsignedInt() {
        return ((payload.getByte(offset++) & 0xFFL) << 24) |
                ((payload.getByte(offset++) & 0xFFL) << 16) |
                ((payload.getByte(offset++) & 0xFFL) << 8) |
                (payload.getByte(offset++) & 0xFFL);
    }

    public int readIntME() {
        return ((payload.getByte(offset++) & 0xFF) << 8) |
                ((payload.getByte(offset++) & 0xFF)) |
                ((payload.getByte(offset++) & 0xFF) << 24) |
                ((payload.getByte(offset++) & 0xFF) << 16);
    }

    public long readUnsignedIntME() {
        return ((payload.getByte(offset++) & 0xFFL) << 8) |
                (payload.getByte(offset++) & 0xFFL) |
                ((payload.getByte(offset++) & 0xFFL) << 24) |
                ((payload.getByte(offset++) & 0xFFL) << 16);
    }

    public int readIntLE() {
        return (payload.getByte(offset++) & 0xFF) |
                ((payload.getByte(offset++) & 0xFF) << 8) |
                ((payload.getByte(offset++) & 0xFF) << 16) |
                ((payload.getByte(offset++) & 0xFF) << 24);
    }

    public long readUnsignedIntLE() {
        return (payload.getByte(offset++) & 0xFFL) |
                ((payload.getByte(offset++) & 0xFFL) << 8) |
                ((payload.getByte(offset++) & 0xFFL) << 16) |
                ((payload.getByte(offset++) & 0xFFL) << 24);
    }

    public int readIntIME() {
        return ((payload.getByte(offset++) & 0xFF) << 16) |
                ((payload.getByte(offset++) & 0xFF) << 24) |
                (payload.getByte(offset++) & 0xFF) |
                ((payload.getByte(offset++) & 0xFF) << 8);
    }

    public long readUnsignedIntIME() {
        return ((payload.getByte(offset++) & 0xFFL) << 16) |
                ((payload.getByte(offset++) & 0xFFL) << 24) |
                (payload.getByte(offset++) & 0xFFL) |
                ((payload.getByte(offset++) & 0xFFL) << 24);
    }

    public int readVarInt() {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = readByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    public int readLengthInt() {
        if (offset < 4) {
            return -1;
        }
        int length = ((payload.getByte(offset - 4) & 0xFF) << 24) | ((payload.getByte(offset - 3) & 0xFF) << 16) |
                ((payload.getByte(offset - 2) & 0xFF) << 8) | (payload.getByte(offset - 1) & 0xFF);
        offset -= length + 4;
        return length;
    }

    public long readLong() {
        long var1 = (long)readInt() & 4294967295L;
        long var2 = (long)readInt() & 4294967295L;
        return var2 + (var1 << 32);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public String readCESU8() {
        byte lengthPrefix = payload.getByte(offset);
        if (lengthPrefix != 0) {
            return null;
        }
        int encodedLength = readVarInt();
        if (encodedLength + offset > payload.capacity()) {
            return null;
        }
        String decodedString = TextUtil.decodeUtf8(payload.array(), offset, encodedLength);
        offset += encodedLength;
        return decodedString;
    }

    public String readStringCp1252NullTerminatedOrNull() {
        if (payload.getByte(offset) == 0) {
            ++offset;
            return null;
        } else {
            return readStringCp1252NullTerminated();
        }
    }

    public String readStringCp1252NullTerminated() {
        int start = offset;
        while(payload.getByte(offset++) != 0);
        int end = offset - start - 1;
        return end == 0 ? "" : TextUtil.decodeStringCp1252(payload.array(), start, end);
    }

    public String readStringCp1252NullCircumfixed() {
        int startPosition = offset;
        while (payload.getByte(++offset) != 0);
        int length = offset - startPosition - 1;
        return length == 0 ? "" : TextUtil.decodeStringCp1252(payload.array(), startPosition, length);
    }

    public void writeByte(int value) {
        this.trueLength++;
        payload.setByte(offset++, (byte)value);
    }

    public void writeByteAdd(int value) {
        writeByte((byte)(value + 128));
    }

    public void writeByteNeg(int value) {
        writeByte((byte)(-value));
    }

    public void writeByteSub(int value) {
        writeByte((byte)(128 - value));
    }

    public void writeSmartByteShort(int var1) {
        this.trueLength++;
        if (var1 >= 0 && var1 < 128) {
            writeByte(var1);
        } else if (var1 >= 0 && var1 < 32768) {
            writeShort(var1 + 32768);
        }
    }

    public void writeLengthByte(int var1) {
        this.trueLength++;
        if (var1 < 0 || var1 > 255)
        {
            return;
        }
        payload.setByte(offset - var1 - 1, (byte)var1);
    }

    public void writeBytes(byte[] src, int srcOffset, int length) {
        this.trueLength += length;
        for (int i = srcOffset; i < length + srcOffset; i++) {
            payload.setByte(offset++, src[i]);
        }
    }

    public void writeBuffer(byte[] src) {
        writeBytes(src, 0, src.length);
    }

    public void writeShort(int value) {
        this.trueLength += 2;
        payload.setByte(offset++, (byte)(value >> 8));
        payload.setByte(offset++, (byte)value);
    }

    public void writeShortAdd(int value) {
        this.trueLength += 2;
        payload.setByte(offset++, (byte)(value + 128));
        payload.setByte(offset++, (byte)(value >> 8));
    }

    public void writeShortLE(int value) {
        this.trueLength += 2;
        payload.setByte(offset++, (byte)value);
        payload.setByte(offset++, (byte)(value >> 8));
    }

    public void writeShortAddLE(int value) {
        this.trueLength += 2;
        payload.setByte(offset++, (byte)(value >> 8));
        payload.setByte(offset++, (byte)(value + 128));
    }

    public void writeLengthShort(int var1) {
        this.trueLength += 2;
        if (var1 < 0 || var1 > 65535) {
            return;
        }
        payload.setByte(offset - var1 - 2, (byte)(var1 >> 8));
        payload.setByte(offset - var1 - 1, (byte)var1);
    }

    public void writeMedium(int value) {
        writeByte((byte) (value >> 16));
        writeByte((byte) (value >> 8));
        writeByte((byte) value);
    }

    public void writeInt(int value) {
        this.trueLength += 4;
        payload.setByte(offset++, (byte)(value >> 24));
        payload.setByte(offset++, (byte)(value >> 16));
        payload.setByte(offset++, (byte)(value >> 8));
        payload.setByte(offset++, (byte)value);
    }

    public void writeIntME(int value) {
        this.trueLength += 4;
        payload.setByte(offset++, (byte)(value >> 8));
        payload.setByte(offset++, (byte) value);
        payload.setByte(offset++, (byte)(value >> 24));
        payload.setByte(offset++, (byte)(value >> 16));
    }

    public void writeIntLE(int value) {
        this.trueLength += 4;
        payload.setByte(offset++, (byte)value);
        payload.setByte(offset++, (byte)(value >> 8));
        payload.setByte(offset++, (byte)(value >> 16));
        payload.setByte(offset++, (byte)(value >> 24));
    }

    public void writeIntIME(int value) {
        this.trueLength += 4;
        payload.setByte(offset++, (byte) (value >> 16));
        payload.setByte(offset++, (byte) (value >> 24));
        payload.setByte(offset++, (byte) value);
        payload.setByte(offset++, (byte) (value >> 8));
    }

    public void writeVarInt(int value) {
        if ((value & 0xFFFFFF80) != 0) {
            if ((value & 0xFFFFC000) != 0) {
                if ((value & 0xFFE00000) != 0) {
                    if ((value & 0xF0000000) != 0) {
                        writeByte((value >>> 28) | 0x80);
                    }
                    writeByte((value >>> 21) | 0x80);
                }
                writeByte((value >>> 14) | 0x80);
            }
            writeByte((value >>> 7) | 0x80);
        }
        writeByte(value & 0x7F);
    }

    public void writeLengthInt(int var1) {
        this.trueLength += 4;
        if (var1 < 0) {
            return;
        }
        payload.setByte(offset - var1 - 4, (byte)(var1 >> 24));
        payload.setByte(offset - var1 - 3, (byte)(var1 >> 16));
        payload.setByte(offset - var1 - 2, (byte)(var1 >> 8));
        payload.setByte(offset - var1 - 1, (byte)var1);
    }

    public void writeLong(long value) {
        // Write the two int values of the long value in big-endian order
        writeInt((int) (value >> 32));
        writeInt((int) value);
    }

    public void writeFloat(float value) {
        // Write the int representation of the float value
        writeInt(Float.floatToIntBits(value));
    }

    public void writeCESU8(CharSequence var1) {
        int var3 = var1.length();
        int var4 = 0;

        for(int var5 = 0; var5 < var3; ++var5) {
            char var6 = var1.charAt(var5);
            if (var6 <= 127) {
                ++var4;
            } else if (var6 <= 2047) {
                var4 += 2;
            } else {
                var4 += 3;
            }
        }

        payload.setByte(offset++, 0);
        writeVarInt(var4);
        trueLength += 4;
        int temp = TextUtil.encodeUtf8(payload.array(), offset, var1);
        offset += temp;
        trueLength += temp;
    }

    public void writeStringCp1252NullTerminated(String var)
    {
        int var2 = var.indexOf(0);
        if(var2 < 0) {
            int temp = TextUtil.encodeStringCp1252(var, 0, var.length(), payload.array(), offset);
            offset += temp;
            trueLength += temp;
            payload.setByte(offset++, 0);
        }
    }

    public void writeStringCp1252NullCircumfixed(String var)
    {
        int var2 = var.indexOf(0);
        if(var2 < 0) {
            payload.setByte(offset++, 0);
            offset += TextUtil.encodeStringCp1252(var, 0, var.length(), payload.array(), offset);
            payload.setByte(offset++, 0);
            trueLength += var.length();
        }
    }

    @Override
    public String toString() {
        StringBuilder hex = new StringBuilder(trueLength * 2);
        for(int i = 0; i < trueLength; i += 2)
            hex.append(String.format("%02x", payload.getByte(i)));
        return hex.toString().toUpperCase();
    }

    /**
     * Releases the buffer.
     */
    public void release() {
        ByteBufferPool.release(payload);
    }
}
