package net.runelite.client.plugins.tscripts.api.library;

import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.client.Static;

import java.util.Arrays;

/**
 * An enumeration of different prayer spells.
 */
public enum TPrayer
{
    /**
     * Thick Skin (Level 1, Defence).
     */
    THICK_SKIN(Varbits.PRAYER_THICK_SKIN, 5.0, WidgetInfo.PRAYER_THICK_SKIN, 1, 0),
    /**
     * Burst of Strength (Level 4, Strength).
     */
    BURST_OF_STRENGTH(Varbits.PRAYER_BURST_OF_STRENGTH, 5.0, WidgetInfo.PRAYER_BURST_OF_STRENGTH, 4, 1),
    /**
     * Clarity of Thought (Level 7, Attack).
     */
    CLARITY_OF_THOUGHT(Varbits.PRAYER_CLARITY_OF_THOUGHT, 5.0, WidgetInfo.PRAYER_CLARITY_OF_THOUGHT, 7, 2),
    /**
     * Sharp Eye (Level 8, Ranging).
     */
    SHARP_EYE(Varbits.PRAYER_SHARP_EYE, 5.0, WidgetInfo.PRAYER_SHARP_EYE, 8, 18),
    /**
     * Mystic Will (Level 9, Magic).
     */
    MYSTIC_WILL(Varbits.PRAYER_MYSTIC_WILL, 5.0, WidgetInfo.PRAYER_MYSTIC_WILL, 9, 19),
    /**
     * Rock Skin (Level 10, Defence).
     */
    ROCK_SKIN(Varbits.PRAYER_ROCK_SKIN, 10.0, WidgetInfo.PRAYER_ROCK_SKIN, 10, 3),
    /**
     * Superhuman Strength (Level 13, Strength).
     */
    SUPERHUMAN_STRENGTH(Varbits.PRAYER_SUPERHUMAN_STRENGTH, 10.0, WidgetInfo.PRAYER_SUPERHUMAN_STRENGTH, 13, 4),
    /**
     * Improved Reflexes (Level 16, Attack).
     */
    IMPROVED_REFLEXES(Varbits.PRAYER_IMPROVED_REFLEXES, 10.0, WidgetInfo.PRAYER_IMPROVED_REFLEXES, 16, 5),
    /**
     * Rapid Restore (Level 19, Stats).
     */
    RAPID_RESTORE(Varbits.PRAYER_RAPID_RESTORE, 60.0 / 36.0, WidgetInfo.PRAYER_RAPID_RESTORE, 19, 6),
    /**
     * Rapid Heal (Level 22, Hitpoints).
     */
    RAPID_HEAL(Varbits.PRAYER_RAPID_HEAL, 60.0 / 18, WidgetInfo.PRAYER_RAPID_HEAL, 22, 7),
    /**
     * Protect Item (Level 25).
     */
    PROTECT_ITEM(Varbits.PRAYER_PROTECT_ITEM, 60.0 / 18, WidgetInfo.PRAYER_PROTECT_ITEM, 25, 8),
    /**
     * Hawk Eye (Level 26, Ranging).
     */
    HAWK_EYE(Varbits.PRAYER_HAWK_EYE, 10.0, WidgetInfo.PRAYER_HAWK_EYE, 26, 20),
    /**
     * Mystic Lore (Level 27, Magic).
     */
    MYSTIC_LORE(Varbits.PRAYER_MYSTIC_LORE, 10.0, WidgetInfo.PRAYER_MYSTIC_LORE, 27, 21),
    /**
     * Steel Skin (Level 28, Defence).
     */
    STEEL_SKIN(Varbits.PRAYER_STEEL_SKIN, 20.0, WidgetInfo.PRAYER_STEEL_SKIN, 28, 9),
    /**
     * Ultimate Strength (Level 31, Strength).
     */
    ULTIMATE_STRENGTH(Varbits.PRAYER_ULTIMATE_STRENGTH, 20.0, WidgetInfo.PRAYER_ULTIMATE_STRENGTH, 31, 10),
    /**
     * Incredible Reflexes (Level 34, Attack).
     */
    INCREDIBLE_REFLEXES(Varbits.PRAYER_INCREDIBLE_REFLEXES, 20.0, WidgetInfo.PRAYER_INCREDIBLE_REFLEXES, 34, 11),
    /**
     * Protect from Magic (Level 37).
     */
    PROTECT_FROM_MAGIC(Varbits.PRAYER_PROTECT_FROM_MAGIC, 20.0, WidgetInfo.PRAYER_PROTECT_FROM_MAGIC, 37, 12),
    /**
     * Protect from Missiles (Level 40).
     */
    PROTECT_FROM_MISSILES(Varbits.PRAYER_PROTECT_FROM_MISSILES, 20.0, WidgetInfo.PRAYER_PROTECT_FROM_MISSILES, 40, 13),
    /**
     * Protect from Melee (Level 43).
     */
    PROTECT_FROM_MELEE(Varbits.PRAYER_PROTECT_FROM_MELEE, 20.0, WidgetInfo.PRAYER_PROTECT_FROM_MELEE, 43, 14),
    /**
     * Eagle Eye (Level 44, Ranging).
     */
    EAGLE_EYE(Varbits.PRAYER_EAGLE_EYE, 20.0, WidgetInfo.PRAYER_EAGLE_EYE, 44, 22),
    /**
     * Mystic Might (Level 45, Magic).
     */
    MYSTIC_MIGHT(Varbits.PRAYER_MYSTIC_MIGHT, 20.0, WidgetInfo.PRAYER_MYSTIC_MIGHT, 45, 23),
    /**
     * Retribution (Level 46).
     */
    RETRIBUTION(Varbits.PRAYER_RETRIBUTION, 5.0, WidgetInfo.PRAYER_RETRIBUTION, 46, 15),
    /**
     * Redemption (Level 49).
     */
    REDEMPTION(Varbits.PRAYER_REDEMPTION, 10.0, WidgetInfo.PRAYER_REDEMPTION, 49, 16),
    /**
     * Smite (Level 52).
     */
    SMITE(Varbits.PRAYER_SMITE, 30.0, WidgetInfo.PRAYER_SMITE, 52, 17),
    /**
     * Preserve (Level 55).
     */
    PRESERVE(Varbits.PRAYER_PRESERVE, 60.0 / 18, WidgetInfo.PRAYER_PRESERVE, 55, 28),
    /**
     * Chivalry (Level 60, Defence/Strength/Attack).
     */
    CHIVALRY(Varbits.PRAYER_CHIVALRY, 40.0, WidgetInfo.PRAYER_CHIVALRY, 60, 25),
    /**
     * Piety (Level 70, Defence/Strength/Attack).
     */
    PIETY(Varbits.PRAYER_PIETY, 40.0, WidgetInfo.PRAYER_PIETY, 70, 26),
    /**
     * Rigour (Level 74, Ranging/Damage/Defence).
     */
    RIGOUR(Varbits.PRAYER_RIGOUR, 40.0, WidgetInfo.PRAYER_RIGOUR, 74, 24),
    /**
     * Augury (Level 77, Magic/Magic Def./Defence).
     */
    AUGURY(Varbits.PRAYER_AUGURY, 40.0, WidgetInfo.PRAYER_AUGURY, 77, 27);

    private final int varbit;
    private final double drainRate;
    private final WidgetInfo widgetInfo;
    private final int level;
    @Getter
    private final int quickPrayerIndex;

    TPrayer(int varbit, double drainRate, WidgetInfo widgetInfo, int level, int quickPrayerIndex)
    {
        this.varbit = varbit;
        this.drainRate = drainRate;
        this.widgetInfo = widgetInfo;
        this.level = level;
        this.quickPrayerIndex = quickPrayerIndex;
    }

    /**
     * Gets the varbit that stores whether the prayer is active or not.
     *
     * @return the prayer active varbit
     */
    public int getVarbit()
    {
        return varbit;
    }

    /**
     * Gets the prayer drain rate (measured in pray points/minute)
     *
     * @return the prayer drain rate
     */
    public double getDrainRate()
    {
        return drainRate;
    }

    /**
     * Gets the required level to use this prayer
     * @return the level to use this prayer
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Returns the widget information associated with the prayer.
     *
     * @return the widget information.
     */
    public WidgetInfo getWidgetInfo()
    {
        return widgetInfo;
    }

    public boolean hasLevelFor()
    {
        return Static.getClient().getRealSkillLevel(Skill.PRAYER) >= level;
    }

    /**
     * check if this prayer is set as a quick prayer
     * @return bool
     */
    public boolean isQuickPrayer()
    {
        return TGame.invoke(() -> (Static.getClient().getVarbitValue(4102) & (int) Math.pow(2, quickPrayerIndex)) == Math.pow(2, quickPrayerIndex));
    }

    /**
     * set the quick prayers
     * @param prayers prayers
     */
    public static void setQuickPrayer(TPrayer... prayers)
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getId(), -1, -1);
        for(TPrayer prayer : prayers)
        {
            if(prayer.isQuickPrayer())
                continue;

            TPackets.sendClickPacket();
            TPackets.sendWidgetActionPacket(0, WidgetInfo.QUICK_PRAYER_PRAYERS.getId(), prayer.getQuickPrayerIndex(), -1);
        }

        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, 5046277, -1, -1);
    }

    /**
     * check if the quick prayers are enabled currently
     * @return bool
     */
    public static boolean isQuickPrayerEnabled()
    {
        return TGame.invoke(() -> Static.getClient().getVarbitValue(Varbits.QUICK_PRAYER) == 1);
    }

    /**
     * toggle quick prayer activation
     */
    public static void toggleQuickPrayer()
    {
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getId(), -1, -1);
    }

    /**
     * turn quick prayer on
     */
    public static void turnOnQuickPrayers()
    {
        if(!isQuickPrayerEnabled())
        {
            toggleQuickPrayer();
        }
    }

    /**
     * turn off quick prayers
     */
    public static void turnOffQuickPrayers()
    {
        if(isQuickPrayerEnabled())
        {
            toggleQuickPrayer();
        }
    }

    /**
     * Turns on the prayer for the given client if it's not already active.
     */
    public void turnOn()
    {
        if(isActive())
            return;
        toggle();
    }

    /**
     * Turns off the prayer for the given client if it's currently active.
     */
    public void turnOff()
    {
        if(!isActive())
            return;
        toggle();
    }

    /**
     * Toggles the prayer for the given client, only if the player meets the level requirements.
     */
    public void toggle()
    {
        if(!hasLevelFor() || TGame.invoke(() -> Static.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0))
            return;
        TPackets.sendClickPacket();
        TPackets.sendWidgetActionPacket(0, widgetInfo.getId(), -1, -1);
    }

    /**
     * Checks if the prayer is active for the given client.
     *
     * @return true if the prayer is active
     */
    public boolean isActive()
    {
        return TGame.invoke(() -> Static.getClient().isPrayerActive(Prayer.valueOf(this.name())));
    }

    public static void disableAll()
    {
        for(TPrayer prayer : values())
        {
            prayer.turnOff();
        }
    }
}
