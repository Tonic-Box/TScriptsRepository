package net.runelite.client.plugins.tscripts.util;

import net.runelite.api.Locatable;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.client.Static;

import java.util.Comparator;

public class Compare {
    public static final Comparator<? super Locatable> DISTANCE = (o1, o2) -> {
        WorldPoint wp = Static.getClient().getLocalPlayer().getWorldLocation();
        WorldPoint wp1 = o1.getWorldLocation();
        WorldPoint wp2 = o2.getWorldLocation();
        double distance1 = distance(wp.getX(), wp.getY(), wp1.getX(), wp1.getY());
        double distance2 = distance(wp.getX(), wp.getY(), wp2.getX(), wp2.getY());
        return Double.compare(distance1, distance2);
    };
    private static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
