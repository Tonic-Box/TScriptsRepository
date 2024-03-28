package net.runelite.client.plugins.tscripts;

import lombok.AllArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("HPQFlooder")
public interface TScriptsConfig extends Config {

    @ConfigItem(
            keyName = "packetLoggerType",
            name = "Packet Log Type",
            description = "",
            position = -1,
            hidden = true
    )
    default PLT packetLoggerType() {
        return PLT.DEFINED;
    }

    @ConfigItem(
            keyName = "packetLoggerType",
            name = "",
            description = ""
    )
    void setPacketLoggerType(PLT param);

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
            keyName = "packetFilter",
            name = "Filter",
            description = "",
            position = 1,
            hidden = true
    )
    default String packetFilter() {
        return "54,63,28,87,11,37";
    }

    @ConfigItem(
            keyName = "packetFilter",
            name = "",
            description = "",
            hidden = true
    )
    void setPacketFilter(String param);

    //*/
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
            keyName = "melogcopy",
            name = "Auto copy M.A. to clipboard",
            description = "",
            position = 3,
            hidden = true
    )
    default boolean melogcopy() {
        return false;
    }

    @ConfigItem(
            keyName = "melogcopy",
            name = "",
            description = ""
    )
    void setMelogcopy(boolean paramString);

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

    @ConfigItem(
            keyName = "socketing",
            name = "",
            description = "",
            position = -3,
            hidden = true
    )
    default boolean socketing() {
        return false;
    }

    @ConfigItem(
            keyName = "socketing",
            name = "",
            description = "",
            hidden = true
    )
    void setSocketing(boolean paramString);

    @ConfigItem(
            keyName = "events",
            name = "",
            description = "",
            position = -3,
            hidden = true
    )
    default boolean events() {
        return false;
    }

    @ConfigItem(
            keyName = "events",
            name = "",
            description = ""
    )
    void setEvents(boolean paramString);

    @ConfigItem(
            keyName = "serverPacketLogger",
            name = "Packet Log",
            description = "",
            position = 0,
            hidden = true
    )
    default boolean serverPacketLogger() {
        return false;
    }

    @ConfigItem(
            keyName = "serverPacketLogger",
            name = "",
            description = "",
            hidden = true
    )
    void setServerPacketLogger(boolean paramString);

    @ConfigItem(
            keyName = "serverPacketFilter",
            name = "Filter",
            description = "",
            position = 1,
            hidden = true
    )
    default String serverPacketFilter() {
        return "";
    }

    @ConfigItem(
            keyName = "serverPacketFilter",
            name = "",
            description = "",
            hidden = true
    )
    void setServerPacketFilter(String param);

    @ConfigItem(
            keyName = "WalkerOverlay",
            name = "",
            description = "",
            position = 0,
            hidden = true
    )
    default boolean getWalkerOverlay() {
        return false;
    }

    @ConfigItem(
            keyName = "WalkerOverlay",
            name = "",
            description = "",
            hidden = true
    )
    void setWalkerOverlay(boolean paramString);
}
