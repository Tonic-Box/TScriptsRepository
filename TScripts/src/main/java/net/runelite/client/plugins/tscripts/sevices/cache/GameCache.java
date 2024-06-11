package net.runelite.client.plugins.tscripts.sevices.cache;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.client.Static;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class GameCache
{
    public static GameCache get()
    {
        if(instance == null)
            instance = new GameCache();
        return instance;
    }
    private static GameCache instance;
    private final List<TileObject> objectCache = Collections.synchronizedList(new ArrayList<>());
    private final List<NPC> npcCache = Collections.synchronizedList(new ArrayList<>());
    private final List<Player> playerCache = Collections.synchronizedList(new ArrayList<>());
    private Actor lastInteracting = null;
    @Getter
    private int tickCount = 0;

    @Subscribe
    public void onGameTick(GameTick event)
    {
        tickCount++;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if(event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
            tickCount = 0;
    }

    private GameCache()
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

    public Stream<Player> playerStream()
    {
        synchronized (playerCache)
        {
            return new ArrayList<>(playerCache).stream();
        }
    }

    public Stream<NPC> npcStream()
    {
        synchronized (npcCache)
        {
            return new ArrayList<>(npcCache).stream();
        }
    }

    public ArrayList<Player> playerList()
    {
        synchronized (playerCache)
        {
            return new ArrayList<>(playerCache);
        }
    }

    public ArrayList<NPC> npcList()
    {
        synchronized (npcCache)
        {
            return new ArrayList<>(npcCache);
        }
    }

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned event)
    {
        synchronized (npcCache)
        {
            playerCache.add(event.getPlayer());
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        synchronized (npcCache)
        {
            npcCache.add(event.getNpc());
        }
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned event)
    {
        synchronized (npcCache)
        {
            playerCache.remove(event.getPlayer());
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        synchronized (npcCache)
        {
            npcCache.remove(event.getNpc());
        }
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
        addTileObject(event.getGameObject());
    }

    @Subscribe
    public void onDespawned(GameObjectDespawned event)
    {
        removeTileObject(event.getGameObject());
    }

    @Subscribe
    public void onSpawned(WallObjectSpawned event)
    {
        addTileObject(event.getWallObject());
    }

    @Subscribe
    public void onDespawned(WallObjectDespawned event)
    {
        removeTileObject(event.getWallObject());
    }

    @Subscribe
    public void onSpawned(DecorativeObjectSpawned event)
    {
        addTileObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onDespawned(DecorativeObjectDespawned event)
    {
        removeTileObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onSpawned(GroundObjectSpawned event)
    {
        addTileObject(event.getGroundObject());
    }

    @Subscribe
    public void onDespawned(GroundObjectDespawned event)
    {
        removeTileObject(event.getGroundObject());
    }

    private void addTileObject(TileObject tileObject)
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
