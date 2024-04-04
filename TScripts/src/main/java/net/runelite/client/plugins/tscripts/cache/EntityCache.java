package net.runelite.client.plugins.tscripts.cache;

import net.runelite.api.TileObject;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.Iterator;
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
    public synchronized void onSpawned(GameObjectSpawned event)
    {
        objectCache.add(event.getGameObject());
    }

    @Subscribe
    public synchronized void onDespawned(GameObjectDespawned event)
    {
        removeTileObject(event.getGameObject());
    }

    @Subscribe
    public synchronized void onSpawned(WallObjectSpawned event)
    {
        objectCache.add(event.getWallObject());
    }

    @Subscribe
    public synchronized void onDespawned(WallObjectDespawned event)
    {
        removeTileObject(event.getWallObject());
    }

    @Subscribe
    public synchronized void onSpawned(DecorativeObjectSpawned event)
    {
        objectCache.add(event.getDecorativeObject());
    }

    @Subscribe
    public synchronized void onDespawned(DecorativeObjectDespawned event)
    {
        removeTileObject(event.getDecorativeObject());
    }

    @Subscribe
    public synchronized void onSpawned(GroundObjectSpawned event)
    {
        objectCache.add(event.getGroundObject());
    }

    @Subscribe
    public synchronized void onDespawned(GroundObjectDespawned event)
    {
        removeTileObject(event.getGroundObject());
    }

    private synchronized void removeTileObject(TileObject tileObject)
    {
        Iterator<TileObject> iterator = objectCache.iterator();
        while (iterator.hasNext())
        {
            TileObject obj = iterator.next();
            if (obj.equals(tileObject))
            {
                iterator.remove();
                break;
            }
        }
    }
}
