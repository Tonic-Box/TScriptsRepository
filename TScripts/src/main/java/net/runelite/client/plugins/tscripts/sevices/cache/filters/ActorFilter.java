package net.runelite.client.plugins.tscripts.sevices.cache.filters;

import net.runelite.api.Actor;
import net.runelite.client.plugins.tscripts.util.Location;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ActorFilter<T extends Actor> {
    private List<T> actors;

    public ActorFilter(List<T> actors) {
        this.actors = new ArrayList<>(actors); // Copy the list to avoid mutating the original list
    }

    public ActorFilter<T> removeIf(Predicate<T> predicate) {
        actors.removeIf(predicate.negate());
        return this;
    }

    public ActorFilter<T> keepIf(Predicate<T> predicate) {
        actors.removeIf(predicate);
        return this;
    }

    public T nearest(int id)
    {
        actors.removeIf(o -> o.getId() != id);
        actors.sort(Comparator.comparing(o -> Location.getDistance(Static.getClient().getLocalPlayer().getWorldLocation(), o.getWorldLocation())));
        return actors.get(0);
    }

    public T nearest(String name)
    {
        actors.removeIf(o -> !o.getName().equals(name));
        actors.sort(Comparator.comparing(o -> Location.getDistance(Static.getClient().getLocalPlayer().getWorldLocation(), o.getWorldLocation())));
        return actors.get(0);
    }

    public ActorFilter<T> sorted(Comparator<T> comparator) {
        actors.sort(comparator);
        return this;
    }

    public ActorFilter<T> limit(int maxSize) {
        if (actors.size() > maxSize) {
            actors = new ArrayList<>(actors.subList(0, maxSize));
        }
        return this;
    }

    public List<T> collect() {
        return actors;
    }
}
