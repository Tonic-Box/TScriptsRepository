package net.runelite.client.plugins.tscripts.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class ThreadPool
{
    private final static ExecutorService executor = Executors.newCachedThreadPool();

    public static Future<?> submit(Runnable runnable)
    {
        return executor.submit(runnable);
    }

    public static <T> T submit(Supplier<T> supplier)
    {
        CompletableFuture<T> future = new CompletableFuture<>();
        Runnable runnable = () -> future.complete(supplier.get());
        submit(runnable);
        return future.join();
    }

    public static void shutdown()
    {
        executor.shutdown();
    }
}
