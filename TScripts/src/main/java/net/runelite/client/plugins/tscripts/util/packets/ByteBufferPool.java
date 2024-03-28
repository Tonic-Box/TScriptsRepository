package net.runelite.client.plugins.tscripts.util.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * A simple pool for byte buffers
 */
public class ByteBufferPool
{
    private static final ByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    private static final int BUFFER_SIZE = 200;

    /**
     * Allocates a pooled ByteBuf of the predefined size.
     *
     * @return A ByteBuf instance with a capacity of 500 bytes.
     */
    public static ByteBuf allocate() {
        return ALLOCATOR.buffer(BUFFER_SIZE);
    }

    /**
     * Allocates a pooled ByteBuf of the specified size.
     *
     * @param size The size of the ByteBuf to allocate.
     * @return A ByteBuf instance with the specified capacity.
     */
    public static ByteBuf allocate(int size) {
        return ALLOCATOR.buffer(size);
    }

    /**
     * Releases a ByteBuf back to the pool.
     *
     * @param buffer The ByteBuf to release.
     */
    public static void release(ByteBuf buffer) {
        if (buffer != null) {
            buffer.release();
        }
    }
}