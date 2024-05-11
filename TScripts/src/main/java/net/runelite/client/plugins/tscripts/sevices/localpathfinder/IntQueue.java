package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

public final class IntQueue {
    private final int[] data;
    private int head;
    private int tail;

    public IntQueue(final int capacity) {
        data = new int[capacity];
        head = 0;
        tail = 0;
    }

    public void enqueue(final int value) {
        data[tail++] = value;
    }

    public int dequeue() {
        return data[head++];
    }

    public boolean isEmpty() {
        return head == tail;
    }
}