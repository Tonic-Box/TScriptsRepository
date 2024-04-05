package net.runelite.client.plugins.tscripts.util.cache;

import net.runelite.api.TileObject;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.Collections;
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
    private final List<TileObject> objectCache = Collections.synchronizedList(new ArrayList<>());

    public Stream<TileObject> objectStream()
    {
        synchronized (objectCache)
        {
            return new ArrayList<>(objectCache).stream();
        }
    }

    private EntityCache()
    {
        Static.getEventBus().register(this);
    }

    @Subscribe
    public void onSpawned(GameObjectSpawned event)
    {
        synchronized (objectCache)
        {
            objectCache.add(event.getGameObject());
        }
    }

    @Subscribe
    public void onDespawned(GameObjectDespawned event)
    {
        removeTileObject(event.getGameObject());
    }

    @Subscribe
    public void onSpawned(WallObjectSpawned event)
    {
        synchronized (objectCache)
        {
            objectCache.add(event.getWallObject());
        }
    }

    @Subscribe
    public void onDespawned(WallObjectDespawned event)
    {
        removeTileObject(event.getWallObject());
    }

    @Subscribe
    public void onSpawned(DecorativeObjectSpawned event)
    {
        synchronized (objectCache)
        {
            objectCache.add(event.getDecorativeObject());
        }
    }

    @Subscribe
    public void onDespawned(DecorativeObjectDespawned event)
    {
        removeTileObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onSpawned(GroundObjectSpawned event)
    {
        synchronized (objectCache)
        {
            objectCache.add(event.getGroundObject());
        }
    }

    @Subscribe
    public void onDespawned(GroundObjectDespawned event)
    {
        removeTileObject(event.getGroundObject());
    }

    private void removeTileObject(TileObject tileObject)
    {
        synchronized (objectCache)
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
}
