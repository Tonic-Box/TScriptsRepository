package net.runelite.client.plugins.tscripts.sevices.ipc;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.sevices.ipc.packets.IPCPacket;
import net.runelite.client.plugins.tscripts.util.Logging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSender {
    //@Getter
    //private static final MulticastSender instance = new MulticastSender();

    public void send(IPCPacket ipcPacket) {
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setReuseAddress(true);
            InetAddress group = InetAddress.getByName(IPCConfig.IPC_GROUP_IP);
            byte[] buf = ipcPacket.serialize();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, IPCConfig.IPC_PORT);
            socket.send(packet);
        } catch (IOException ex) {
            Logging.errorLog(ex);
        }
    }
}