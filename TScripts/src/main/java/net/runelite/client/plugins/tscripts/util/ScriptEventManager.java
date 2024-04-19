package net.runelite.client.plugins.tscripts.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.adapter.Adapter;
import net.runelite.client.plugins.tscripts.adapter.models.Scope.Scope;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.runtime.Runtime;
import net.unethicalite.client.Static;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ScriptEventManager
{
    private final TScriptsPlugin plugin;
    @Getter
    private static ScriptEventManager instance;

    public static void init(TScriptsPlugin plugin)
    {
        instance = new ScriptEventManager(plugin);
    }
    private final Map<String, EventBus.Subscriber> subscribers = new HashMap<>();

    public void registerSubscriber(String name, String profile, Class<?> event)
    {
        unregisterSubscriber(name);
        subscribers.put(name, TGame.register(event, ev -> {
            try {
                if(!plugin.config.eventsEnabled())
                    return;
                Path path = Paths.get(plugin.getScriptPath(name, profile));
                String code = Files.readString(path);
                Scope scope = Adapter.parse(code);
                Runtime runtime = new Runtime();
                ThreadPool.submit(() -> runtime.execute(scope, "TS_EVENT", "TS_EVENT"));
            } catch (Exception ex) {
                Logging.errorLog(ex);
            }
        }));
    }

    public void unregisterSubscriber(String script)
    {
        if(subscribers.containsKey(script))
        {
            EventBus.Subscriber oldSubscriber = subscribers.get(script);
            Static.getEventBus().unregister(oldSubscriber);
        }
    }

    public void loadProfile(String profile, Map<String,Class<?>> scripts)
    {
        //clear the previous subscribers
        clearAllSubscribers();

        //register the new subscribers
        for(Map.Entry<String,Class<?>> entry : scripts.entrySet())
        {
            registerSubscriber(entry.getKey(), profile, entry.getValue());
        }
    }

    public void clearAllSubscribers()
    {
        for(EventBus.Subscriber sub : subscribers.values())
        {
            Static.getEventBus().unregister(sub);
        }
        subscribers.clear();
    }
}
