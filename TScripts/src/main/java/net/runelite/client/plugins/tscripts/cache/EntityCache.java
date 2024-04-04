package net.runelite.client.plugins.tscripts.cache;

import net.runelite.api.TileObject;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EntityCache
{
    public static EntityCache get()
    {
        if(instance == null)
            instance = new EntityCache();
        return instance;
    }
    private static EntityCache instance;
    private final List<TileObject> objectCache = new ArrayList<>();

    public Stream<TileObject> objectStream()
    {
        return objectCache.stream();
    }

    private EntityCache()
    {
        Static.getEventBus().register(this);
    }

    @Subscribe
    public void onSpawned(GameObjectSpawned event)
    {
        objectCache.add(event.getGameObject());
    }

    @Subscribe
    public void onDespawned(GameObjectDespawned event)
    {
        objectCache.remove(event.getGameObject());
    }

    @Subscribe
    public void onSpawned(WallObjectSpawned event)
    {
        objectCache.add(event.getWallObject());
    }

    @Subscribe
    public void onDespawned(WallObjectDespawned event)
    {
        objectCache.remove(event.getWallObject());
    }

    @Subscribe
    public void onSpawned(DecorativeObjectSpawned event)
    {
        objectCache.add(event.getDecorativeObject());
    }

    @Subscribe
    public void onDespawned(DecorativeObjectDespawned event)
    {
        objectCache.remove(event.getDecorativeObject());
    }

    @Subscribe
    public void onSpawned(GroundObjectSpawned event)
    {
        objectCache.add(event.getGroundObject());
    }

    @Subscribe
    public void onDespawned(GroundObjectDespawned event)
    {
        objectCache.remove(event.getGroundObject());
    }
}
