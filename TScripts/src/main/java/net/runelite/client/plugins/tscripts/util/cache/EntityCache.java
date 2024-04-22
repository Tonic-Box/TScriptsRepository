package net.runelite.client.plugins.tscripts.util.cache;

import net.runelite.api.*;
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
    private Actor lastInteracting = null;

    private EntityCache()
    {
        Static.getEventBus().register(this);
    }

    // ############## Actors ##############

    public Actor getInteracting()
    {
        Actor interacting = Static.getClient().getLocalPlayer().getInteracting();
        if(interacting == null)
            interacting = lastInteracting;
        return interacting;
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if(event.getSource() == Static.getClient().getLocalPlayer() && event.getTarget() != null)
        {
            if(filter(event.getSource()))
                lastInteracting = event.getTarget();
        }
        else if(event.getTarget() == Static.getClient().getLocalPlayer() && event.getSource() != null)
        {
            if(filter(event.getSource()))
                lastInteracting = event.getSource();
        }
    }

    private boolean filter(Actor actor)
    {
        return actor.getName() != null;
    }

    public String data(Actor actor)
    {
        return "Name: " + actor.getName() + ", ID: " + actor.getId() + ", Combat Level: " + actor.getCombatLevel() + (actor instanceof NPC ? ", Is NPC: true" : ", Is NPC: false");
    }

    // ############## TileObjects ##############

    public Stream<TileObject> objectStream()
    {
        synchronized (objectCache)
        {
            return new ArrayList<>(objectCache).stream();
        }
    }

    @Subscribe
    public void onSpawned(GameObjectSpawned event)
    {
        addObject(event.getGameObject());
    }

    @Subscribe
    public void onDespawned(GameObjectDespawned event)
    {
        removeTileObject(event.getGameObject());
    }

    @Subscribe
    public void onSpawned(WallObjectSpawned event)
    {
        addObject(event.getWallObject());
    }

    @Subscribe
    public void onDespawned(WallObjectDespawned event)
    {
        removeTileObject(event.getWallObject());
    }

    @Subscribe
    public void onSpawned(DecorativeObjectSpawned event)
    {
        addObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onDespawned(DecorativeObjectDespawned event)
    {
        removeTileObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onSpawned(GroundObjectSpawned event)
    {
        addObject(event.getGroundObject());
    }

    @Subscribe
    public void onDespawned(GroundObjectDespawned event)
    {
        removeTileObject(event.getGroundObject());
    }

    private void addObject(TileObject tileObject)
    {
        synchronized (objectCache)
        {
            objectCache.add(tileObject);
        }
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
