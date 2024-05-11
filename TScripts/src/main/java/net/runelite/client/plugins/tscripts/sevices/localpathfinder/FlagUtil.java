package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import static net.runelite.api.CollisionDataFlag.*;

public class FlagUtil
{
    public static byte all(int[][] map, int x, int y)
    {
        int flag = map[x][y];
        byte n = n(flag);
        byte e = e(flag);
        byte s = s(flag);
        byte w = w(flag);
        if((n | e | s | w) == 0)
        {
            return 0;
        }
        byte sw = (byte) (s & w & e(map[x][y - 1]) & n(map[x - 1][y]));
        byte se = (byte) (s & e & w(map[x][y - 1]) & n(map[x + 1][y]));
        byte nw = (byte) (n & w & e(map[x][y + 1]) & s(map[x - 1][y]));
        byte ne = (byte) (n & e & w(map[x][y + 1]) & s(map[x + 1][y]));

        return (byte) (nw | (n << 1) | (ne << 2) | (w << 3) | (e << 4) | (sw << 5) | (s << 6) | (se << 7));
    }

    public static byte n(int flag)
    {
        return checkDirection(flag, BLOCK_MOVEMENT_NORTH, BLOCK_MOVEMENT_FULL, BLOCK_MOVEMENT_OBJECT, BLOCK_MOVEMENT_FLOOR_DECORATION) ? (byte) 1 : 0;
    }

    public static byte e(int flag)
    {
        return checkDirection(flag, BLOCK_MOVEMENT_EAST, BLOCK_MOVEMENT_FULL, BLOCK_MOVEMENT_OBJECT, BLOCK_MOVEMENT_FLOOR_DECORATION) ? (byte) 1 : 0;
    }

    public static byte s(int flag)
    {
        return checkDirection(flag, BLOCK_MOVEMENT_SOUTH, BLOCK_MOVEMENT_FULL, BLOCK_MOVEMENT_OBJECT, BLOCK_MOVEMENT_FLOOR_DECORATION) ? (byte) 1 : 0;
    }

    public static byte w(int flag)
    {
        return checkDirection(flag, BLOCK_MOVEMENT_EAST, BLOCK_MOVEMENT_FULL, BLOCK_MOVEMENT_OBJECT, BLOCK_MOVEMENT_FLOOR_DECORATION) ? (byte) 1 : 0;
    }

    public static boolean checkDirection(int currentState, int... directionFlag) {
        for (int flag : directionFlag) {
            if ((flag & currentState) != 0) {
                return true;
            }

        }
        return false;
        //return (currentState & directionFlag) == 0;
    }
}
