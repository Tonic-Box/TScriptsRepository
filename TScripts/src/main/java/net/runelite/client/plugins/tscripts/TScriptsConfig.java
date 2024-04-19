package net.runelite.client.plugins.tscripts;

import lombok.AllArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("HPQFlooder")
public interface TScriptsConfig extends Config {

    @ConfigItem(
            keyName = "packetLogger",
            name = "Packet Log",
            description = "",
            position = 0,
            hidden = true
    )
    default boolean packetLogger() {
        return false;
    }

    @AllArgsConstructor
    enum PLT
    {
        RAW("Raw Packets"),
        DEFINED("Packet Functions");
        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }

    @ConfigItem(
            keyName = "packetLogger",
            name = "",
            description = "",
            hidden = true
    )
    void setPacketLogger(boolean paramString);

    @ConfigItem(
            keyName = "melog",
            name = "Menu Action Log",
            description = "",
            position = 2,
            hidden = true
    )
    default boolean melog() {
        return false;
    }

    @ConfigItem(
            keyName = "melog",
            name = "",
            description = ""
    )
    void setMelog(boolean paramString);

    @ConfigItem(
            keyName = "keybindsEnabled",
            name = "Keybinds Enabled",
            description = "",
            position = -3,
            hidden = true
    )
    default boolean keybindsEnabled() {
        return true;
    }

    @ConfigItem(
            keyName = "eventsEnabled",
            name = "",
            description = ""
    )
    void setEventsEnabled(boolean paramString);

    @ConfigItem(
            keyName = "eventsEnabled",
            name = "Keybinds Enabled",
            description = "",
            position = -3,
            hidden = true
    )
    default boolean eventsEnabled() {
        return false;
    }

    @ConfigItem(
            keyName = "keybindsEnabled",
            name = "",
            description = ""
    )
    void setkeybindsEnabled(boolean paramString);

    @ConfigItem(
            keyName = "copyMenus",
            name = "Copy Menus",
            description = "",
            position = -3,
            hidden = true
    )
    default boolean copyMenus() {
        return true;
    }

    @ConfigItem(
            keyName = "copyMenus",
            name = "",
            description = ""
    )
    void setCopyMenus(boolean paramString);
}
