package net.runelite.client.plugins.tscripts.util;

public class Values
{
    public static int getAsInt(Object current) {
        if (current instanceof Integer) {
            return (int) current;
        } else if (current instanceof String) {
            return ((String) current).length();
        } else {
            return 0;
        }
    }
}
