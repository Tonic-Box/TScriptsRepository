package net.runelite.client.plugins.tscripts.sevices.ipc;

import net.runelite.client.plugins.tscripts.sevices.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.sevices.ipc.packets.IPCPacket;
import net.runelite.client.plugins.tscripts.util.Logging;

import java.io.IOException;
import java.net.*;

public class MulticastReceiver implements Runnable {
    private boolean shutdown = false;

    public void shutdown()
    {
        shutdown = true;
    }

    @Override
    @SuppressWarnings("deprecated")
    public void run() {
        shutdown = false;
        byte[] buffer = new byte[1024];
        try (MulticastSocket socket = new MulticastSocket(IPCConfig.IPC_PORT)) {
            // Using the localhost-only multicast address
            InetAddress group = InetAddress.getByName(IPCConfig.IPC_GROUP_IP);
            NetworkInterface networkInterface = NetworkInterface.getByName("lo"); // "lo" usually stands for loopback

            // Join the group on the loopback interface, ensuring it doesn't go beyond your local machine
            socket.joinGroup(new InetSocketAddress(group, 0), networkInterface);

            while (!Thread.currentThread().isInterrupted() && !shutdown) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                IPCPacket ipcPacket = IPCPacket.accept(packet.getData());
                TEventBus.post(ipcPacket);
            }

            // Leave the group on the loopback interface
            socket.leaveGroup(new InetSocketAddress(group, 0), networkInterface);
        } catch (IOException ex) {
            Logging.errorLog(ex);
        }
    }
}