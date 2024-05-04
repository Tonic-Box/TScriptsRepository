package net.runelite.client.plugins.tscripts.sevices.ipc.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

@Data
public class IPCPacket
{
    private static final Gson gson = new GsonBuilder().create();

    private final  String data;
    private final  String target;

    public IPCPacket(String target, String data)
    {
        this.data = data;
        this.target = target;
    }
    
    public byte[] serialize()
    {
        return gson.toJson(this).getBytes();
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends IPCPacket> T accept(byte[] bytes)
    {
        return (T) gson.fromJson(new String(bytes), IPCPacket.class);
    }
}
