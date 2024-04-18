package net.runelite.client.plugins.tscripts;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TWorldPoint;
import net.runelite.client.plugins.tscripts.runtime.Runtime;
import net.runelite.client.plugins.tscripts.ui.TScriptsPanel;
import net.runelite.client.plugins.tscripts.util.CompletionSupplier;
import net.runelite.client.plugins.tscripts.util.ConfigHandler;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.plugins.tscripts.util.TextUtil;
import net.runelite.client.plugins.tscripts.util.packets.PacketBuffer;
import net.runelite.client.plugins.tscripts.types.PacketDefinition;
import net.runelite.client.plugins.tscripts.util.packets.PacketMapReader;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import com.google.inject.Inject;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.unethicalite.api.events.PacketSent;
import net.unethicalite.client.Static;
import org.fife.ui.autocomplete.CompletionProvider;
import org.pf4j.Extension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The main plugin class for TScripts.
 */
@Slf4j
@Extension
@PluginDescriptor(name = "TScripts", description = "Cheats n Stuff", tags = {"TonicBox", "scripts"})
public class TScriptsPlugin  extends Plugin {
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ClientUI clientUI;
    @Inject
    public TScriptsConfig config;
    @Inject
    public KeyManager keyManager;
    @Getter
    @Setter
    private String profile = "[Default]";
    public ConfigHandler configHandler;
    @Inject
    private ClientToolbar clientToolbar;
    private NavigationButton navButton;
    public HashMap<String, KeyListener> hotKeyListeners = new HashMap<>();
    @Getter
    private Runtime runtime;
    @Getter
    private CompletionProvider baseCompletion;
    public static final String START_DIR = RuneLite.RUNELITE_DIR + File.separator + "HPQScripts" + File.separator;
    public static String HOME_DIR;

    @Provides
    TScriptsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TScriptsConfig.class);
    }

    /**
     * Called when the plugin is started up.
     */
    @Override
    protected void startUp() {
        HOME_DIR = START_DIR;
        try {
            Files.createDirectories(Paths.get(RuneLite.RUNELITE_DIR + File.separator + "HPQScripts" + File.separator));
        }
        catch(Exception ex) {
            Logging.errorLog(ex);
        }

        profile = "[Default]";
        configHandler = new ConfigHandler(getProfilePath(profile));
        configHandler.validateConfig();
        new MethodManager(this);
        this.runtime = new Runtime();
        this.baseCompletion = CompletionSupplier.createBaseCompletionProvider();
        sidePanel(true);
    }

    /**
     * Called when the plugin is shut down.
     */
    @Override
    protected void shutDown() {
        sidePanel(false);
        unregAllKeyListeners();
    }

    /**
     * Adds the TScripts panel to the client toolbar.
     */
    public void sidePanel(Boolean show)
    {
        TScriptsPanel panel;
        if(show)
        {
            panel = injector.getInstance(TScriptsPanel.class);
            panel.init(config, this);

            final BufferedImage icon = ImageUtil.loadImageResource(TScriptsPlugin.class, "Test_icon.png");

            navButton = NavigationButton.builder()
                    .tooltip("TScripts Panel")
                    .icon(icon)
                    .priority(2)
                    .panel(panel)
                    .build();
            clientToolbar.addNavigation(navButton);
        }
        else
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
            panel = null;
        }
    }

    /**
     * Gets the path to the profile directory.
     *
     * @param profile the profile name
     * @return the path to the profile directory
     */
    public String getProfilePath(String profile) {
        if(profile.equals("[Default]"))
            return  START_DIR;
        else
            return (START_DIR + profile + ".profile/").replace("\\", "/");
    }

    /**
     * Registers or unregisters a key listener.
     */
    public void setListenersToggle(boolean on) {
        for(var HKL : hotKeyListeners.entrySet()) {
            if(on) {
                register(HKL.getValue());
            }
            else {
                unregister(HKL.getValue());
            }
        }
    }

    /**
     * Unregisters all key listeners.
     */
    public void unregAllKeyListeners() {
        for(var HKL : hotKeyListeners.entrySet()) {
            keyManager.unregisterKeyListener(HKL.getValue());
        }
        hotKeyListeners = new HashMap<>();
    }

    /**
     * Gets the path to a script.
     *
     * @param id the script ID
     * @param profile the profile name
     * @return the path to the script
     */
    public String getScriptPath(String id, String profile) {
        return getProfilePath(profile) + id + ".script";
    }

    /**
     * Registers a key listener.
     */
    public void register(KeyListener KL) {
        keyManager.registerKeyListener(KL);
    }

    /**
     * Unregisters a key listener.
     */
    public void unregister(KeyListener KL) {
        keyManager.unregisterKeyListener(KL);
    }

    /**
     * Checks if a script is running.
     *
     * @param scriptName the script name
     * @return true if the script is running, false otherwise
     */
    public boolean amIRunning(String scriptName) {
        return this.runtime.getScriptName().equals(scriptName) && this.getRuntime().getProfile().equals(profile) && !canIRun();
    }

    /**
     * Checks if a script can be run.
     *
     * @return true if the script can be run, false otherwise
     */
    public boolean canIRun() {
        return this.runtime.isDone();
    }

    /**
     * Stops a script.
     * @param scriptName the script name
     */
    public void stopScript(String scriptName) {
        if(this.runtime != null && this.runtime.getScriptName().equals(scriptName)) {
            this.runtime.killScript();
        }
    }

    public void stopScript() {
        this.runtime.killScript();
    }

    //events

    @Subscribe
    public void onPacketSent(PacketSent event) {
        if(!config.packetLogger() || event.getPacketBufferNode().getClientPacket() == null)
            return;

        int id = event.getPacketBufferNode().getClientPacket().getId();
        byte[] payload = Arrays.copyOfRange(
                event.getPacketBufferNode().getPacketBuffer().getPayload(),
                1, event.getPacketBufferNode().getPacketBuffer().getOffset());

        PacketBuffer pb = new PacketBuffer(id, payload);
        PacketDefinition pd = null;
        String out;
        try
        {
            pd = PacketMapReader.analyze(pb);
            out = PacketMapReader.prettify(pb);
        }
        catch (Exception e)
        {
            out = "[oops]";
        }

        if (out.startsWith("[UNKNOWN("))
        {
            out = "";
        }

        if(pd != null)
        {
            switch(pd.getName())
            {
                case "OP_WALK":
                    out = "walkPacket(" + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ");";
                    break;
                case "OP_WIDGET_ACTION_0":
                case "OP_WIDGET_ACTION_1":
                case "OP_WIDGET_ACTION_2":
                case "OP_WIDGET_ACTION_3":
                case "OP_WIDGET_ACTION_4":
                case "OP_WIDGET_ACTION_5":
                case "OP_WIDGET_ACTION_6":
                case "OP_WIDGET_ACTION_7":
                case "OP_WIDGET_ACTION_8":
                case "OP_WIDGET_ACTION_9":
                    out = "widgetPacket(" + pd.getName().split("_")[3] + ", " + pd.getMap().get("widgetId") + ", " + pd.getMap().get("childId") + ", " + pd.getMap().get("itemId") + ");";
                    break;
                /*case "OP_RESUME_COUNTDIALOG":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendResumeCountDialogue(" + pd.getMap().get("count") + "); //" + out);
                    break;
                case "OP_RESUME_PAUSEBUTTON":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendResumePauseWidget(" + pd.getMap().get("widgetID") + ", " + pd.getMap().get("optionIndex") + "); //" + out);
                    break;
                case "OP_RESUME_OBJDIALOG":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendResumeObjectDialogue(" + pd.getMap().get("id") + "); //" + out);
                    break;
                case "OP_RESUME_NAMEDIALOG":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendResumeNameDialogue(" + pd.getMap().get("var7") + "); //" + out);
                    break;*/
                case "OP_PLAYER_ACTION_0":
                case "OP_PLAYER_ACTION_1":
                case "OP_PLAYER_ACTION_2":
                case "OP_PLAYER_ACTION_3":
                case "OP_PLAYER_ACTION_4":
                case "OP_PLAYER_ACTION_5":
                case "OP_PLAYER_ACTION_6":
                case "OP_PLAYER_ACTION_7":
                    out = "playerPacket(" + pd.getName().split("_")[3] + ", " + pd.getMap().get("identifier") + ");";
                    break;
                case "OP_NPC_ACTION_0":
                case "OP_NPC_ACTION_1":
                case "OP_NPC_ACTION_2":
                case "OP_NPC_ACTION_3":
                case "OP_NPC_ACTION_4":
                    out = "npcPacket(" + pd.getName().split("_")[3] + ", " + pd.getMap().get("identifier") + ");";
                    break;
                case "OP_GAME_OBJECT_ACTION_0":
                case "OP_GAME_OBJECT_ACTION_1":
                case "OP_GAME_OBJECT_ACTION_2":
                case "OP_GAME_OBJECT_ACTION_3":
                case "OP_GAME_OBJECT_ACTION_4":
                    out = "objectPacket(" + pd.getName().split("_")[4] + ", " + pd.getMap().get("identifier") + ", " + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ");";
                    break;
                case "OP_GROUND_ITEM_ACTION_0":
                case "OP_GROUND_ITEM_ACTION_1":
                case "OP_GROUND_ITEM_ACTION_2":
                case "OP_GROUND_ITEM_ACTION_3":
                case "OP_GROUND_ITEM_ACTION_4":
                    out = "groundItemPacket(" + pd.getName().split("_")[4] + ", " + pd.getMap().get("identifier") + ", " + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ");";
                    break;
                /*case "OP_WIDGET_TARGET_ON_GAME_OBJECT":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendWidgetOnObjectPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + "); //" + out);
                    break;
                case "OP_WIDGET_TARGET_ON_GROUND_ITEM":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendWidgetOnGroundItemPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + "); //" + out);
                    break;
                case "OP_WIDGET_TARGET_ON_NPC":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendWidgetOnNpcPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + "); //" + out);
                    break;
                case "OP_WIDGET_TARGET_ON_PLAYER":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendWidgetOnPlayerPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + "); //" + out);
                    break;
                case "OP_WIDGET_TARGET_ON_WIDGET":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendWidgetOnWidgetPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("targetWidgetID") + ", " + pd.getMap().get("identifier2") + ", " + pd.getMap().get("param0") + "); //" + out);
                    break;
                case "OP_INTERFACE_CLOSE":
                    logger.log(CustomLoggerLevels.SHORTPACKET, "sendWidgetClosePacket(); //" + out);
                    break;*/
            }
        }
        pb.release();

        if(out.startsWith("[") || out.isBlank())
            return;

        if(!Static.getClient().getGameState().equals(GameState.LOGGED_IN))
            return;

        out = out.replace("65535", "-1");

        Logging.logToChat(out);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded entry) {
        if (config.copyMenus()) //menuAction("Talk-to", "<col=ffff00>Woodsman tutor", 22692, 9, 0, 0);
        {
            String color = "<col=00ff00>";
            int opcode = entry.getType();
            String name = TextUtil.sanitize(entry.getTarget()).split(" \\(")[0].trim();
            if(opcode == MenuAction.EXAMINE_NPC.getId())
            {
                MenuEntry npcMenu = client.createMenuEntry(1)
                        .setOption("Dev Helper ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE_SUBMENU);

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(npcMenu)
                        .onClick(c -> Logging.copyToClipboard(name));

                NPC npc = client.getCachedNPCs()[entry.getIdentifier()];

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + npc.getId() + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(npcMenu)
                        .onClick(c -> Logging.copyToClipboard(npc.getId() + ""));
            }
            else if(opcode == MenuAction.EXAMINE_OBJECT.getId())
            {
                MenuEntry objectHelper = client.createMenuEntry(1)
                        .setOption("Dev Helper ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE_SUBMENU);

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(name));

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + entry.getIdentifier() + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(entry.getIdentifier() + ""));
            }
            else if(opcode == MenuAction.TRADE.getId())
            {
                MenuEntry objectHelper = client.createMenuEntry(1)
                        .setOption("Dev Helper ")
                        .setTarget("<col=ffff00>" + name + " ")
                        .setType(MenuAction.RUNELITE_SUBMENU);

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(name));
            }
            else if(entry.getOption().equals("Use")) //items
            {
                MenuEntry objectHelper = client.createMenuEntry(1)
                        .setOption("Dev Helper ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE_SUBMENU);

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(name));

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + entry.getItemId() + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(entry.getItemId() + ""));
            }
            else if(opcode == MenuAction.EXAMINE_ITEM_GROUND.getId())
            {
                MenuEntry objectHelper = client.createMenuEntry(1)
                        .setOption("Dev Helper ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE_SUBMENU);

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(name));

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + entry.getItemId() + " ")
                        .setType(MenuAction.RUNELITE)
                        .setParent(objectHelper)
                        .onClick(c -> Logging.copyToClipboard(entry.getIdentifier() + ""));
            }
            else if(opcode == MenuAction.WALK.getId())
            {
                if(Static.getClient().getSelectedSceneTile() != null)
                {
                    WorldPoint worldPoint = TWorldPoint.get(Static.getClient().getSelectedSceneTile().getWorldLocation());
                    client.createMenuEntry(1)
                            .setOption("Copy coords [" + worldPoint.getX() + ", " + worldPoint.getY() + "]")
                            .setTarget(color + name + " ")
                            .setType(MenuAction.RUNELITE)
                            .onClick(c -> Logging.copyToClipboard(worldPoint.getX() + ", " + worldPoint.getY()));
                }
            }
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {

        if((event.getMenuOption().equals("Accept trade")  || event.getMenuOption().equals("Accept invitation"))&& event.getMenuTarget().startsWith("<col=ffffff>TSCRIPTS_LOGGER:"))
        {
            String out = event.getMenuTarget().substring(28, event.getMenuTarget().length() - 6);
            Logging.copyToClipboard(out);
            event.consume();
        }
        else if(config.melog())
        {
            Logging.logToChat("menuAction(\"" +
                    event.getMenuOption() + "\", \"" +
                    event.getMenuTarget() + "\", " +
                    event.getId() + ", " +
                    event.getMenuAction().getId() + ", " +
                    event.getParam0() + ", " +
                    event.getParam1() + ");");
        }
    }
}
