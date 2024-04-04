package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.client.eventbus.EventBus;
import net.unethicalite.client.Static;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TGame
{
    public static <T> T invoke(Supplier<T> supplier)
    {
        if (!Static.getClient().isClientThread())
        {
            CompletableFuture<T> future = new CompletableFuture<>();
            Runnable runnable = () -> future.complete(supplier.get());
            Static.getClientThread().invoke(runnable);
            return future.join();
        } else
        {
            return supplier.get();
        }
    }

    public static <T> EventBus.Subscriber register(Class<T> event, Consumer<T> callback)
    {
        return Static.getEventBus().register(event, callback, 0);
    }

    public static void unregister(List<EventBus.Subscriber> subs)
    {
        for(EventBus.Subscriber sub : subs)
        {
            Static.getEventBus().unregister(sub);
        }
    }
}
