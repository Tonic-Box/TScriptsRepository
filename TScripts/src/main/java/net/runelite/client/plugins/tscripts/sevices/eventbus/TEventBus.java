package net.runelite.client.plugins.tscripts.sevices.eventbus;

import net.runelite.client.plugins.tscripts.util.Logging;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TEventBus
{
    private static final Map<Object,List<Method>> subscribers = new ConcurrentHashMap<>();

    public static void register(Object obj)
    {
        if (!subscribers.containsKey(obj))
            subscribers.put(obj, new ArrayList<>());

        for (Method method : obj.getClass().getMethods())
        {
            if (!method.isAnnotationPresent(_Subscribe.class))
                continue;

            method.setAccessible(true);

            if (!subscribers.get(obj).contains(method))
                subscribers.get(obj).add(method);
        }
    }

    public static void unregister(Object obj)
    {
        subscribers.remove(obj);
    }

    public static void post(Object event)
    {
        for (Object key : subscribers.keySet())
        {
            for(Method method : subscribers.get(key))
            {
                if (method.getParameterTypes().length != 1)
                    continue;

                if (!method.getParameterTypes()[0].equals(event.getClass()))
                    continue;

                try
                {
                    method.invoke(key, event);
                } catch (Exception ex)
                {
                    Logging.errorLog(ex);
                }
            }
        }
    }
}
