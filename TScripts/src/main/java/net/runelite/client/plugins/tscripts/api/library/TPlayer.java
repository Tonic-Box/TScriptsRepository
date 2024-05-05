package net.runelite.client.plugins.tscripts.api.library;

import net.runelite.api.Player;
import net.unethicalite.api.entities.Players;
import net.unethicalite.client.Static;

import java.util.List;
import java.util.function.Predicate;

public class TPlayer
{
    public static Predicate<Player> nameEquals(String name)
    {
        return player -> player.getName().equals(name);
    }

    public static Predicate<Player> nameContains(String name)
    {
        return player -> player.getName().contains(name);
    }

    public static Predicate<Player> indexEquals(int index)
    {
        return player -> player.getIndex() == index;
    }

    public static Predicate<Player> withinDistance(int distance)
    {
        return player -> player.distanceTo(Static.getClient().getLocalPlayer()) <= distance;
    }

    public static List<Player> getPlayers()
    {
        return Static.getClient().getPlayers();
    }

    public static Player getPlayer(Predicate<Player> predicate)
    {
        return TGame.invoke(() -> getPlayers().stream()
                .filter(predicate)
                .filter(player -> !player.equals(Static.getClient().getLocalPlayer()))
                .findFirst().orElse(null));
    }
}
