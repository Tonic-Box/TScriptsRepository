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
import net.runelite.client.plugins.tscripts.sevices.ScriptEventService;
import net.runelite.client.plugins.tscripts.sevices.ipc.MulticastReceiver;
import net.runelite.client.plugins.tscripts.sevices.localpathfinder.*;
import net.runelite.client.plugins.tscripts.ui.TScriptsPanel;
import net.runelite.client.plugins.tscripts.util.*;
import net.runelite.client.plugins.tscripts.sevices.cache.GameCache;
import net.runelite.client.plugins.tscripts.util.BaseClientUI;
import net.runelite.client.plugins.tscripts.util.packets.PacketBuffer;
import net.runelite.client.plugins.tscripts.types.PacketDefinition;
import net.runelite.client.plugins.tscripts.util.packets.PacketMapReader;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import com.google.inject.Inject;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.unethicalite.api.events.PacketSent;
import net.unethicalite.client.Static;
import org.fife.ui.autocomplete.CompletionProvider;
import org.pf4j.Extension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private ClientToolbar titleToolbar;
    @Inject
    public TScriptsConfig config;
    @Inject
    public KeyManager keyManager;
    @Inject
    private OverlayManager overlayManager;

    @Getter
    @Setter
    private String profile = "[Default]";
    public ConfigHandler configHandler;
    @Inject
    private ClientToolbar clientToolbar;
    private NavigationButton navButton;
    public HashMap<String, KeyListener> hotKeyListeners = new HashMap<>();
    @Getter
    private CompletionProvider baseCompletion;
    public static final String START_DIR = RuneLite.RUNELITE_DIR + File.separator + "HPQScripts" + File.separator;
    public static String HOME_DIR;
    //private MulticastReceiver multicastReceiver;
    @Getter
    private TScriptsPanel panel;
    private TileOverlay overlays;

    @Provides
    TScriptsConfig provideConfig(ConfigManager configManager)
    {
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
        ScriptEventService.init(this);
        new MethodManager(this);
        this.baseCompletion = CompletionSupplier.createBaseCompletionProvider();
        sidePanel(true);
        GameCache.get();
        BaseClientUI baseClientUI = new BaseClientUI(clientUI);
        NavigationButton headlessToggleButton = NavigationButton
                .builder()
                .priority(99)
                .icon(ImageUtil.loadImageResource(TScriptsPlugin.class, "Test_icon.png"))
                .tooltip("Headless Mode")
                .onClick(baseClientUI::toggleHeadless)
                .build();
        clientToolbar.addNavigation(headlessToggleButton);
        //this.multicastReceiver = new MulticastReceiver();
        //ThreadPool.submit(this.multicastReceiver);
        this.overlays = new TileOverlay(this.client);
        this.overlayManager.add(this.overlays);
    }

    /**
     * Called when the plugin is shut down.
     */
    @Override
    protected void shutDown() {
        sidePanel(false);
        unregAllKeyListeners();
        //multicastReceiver.shutdown();
    }

    /**
     * Adds the TScripts panel to the client toolbar.
     */
    public void sidePanel(Boolean show)
    {
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
    public void unregister(KeyListener KL)
    {
        keyManager.unregisterKeyListener(KL);
    }

    //events

    @Subscribe
    public void onPacketSent(PacketSent event) {
        if(!config.packetLogger() || event.getPacketBufferNode().getClientPacket() == null)
            return;

        int id = event.getPacketBufferNode().getClientPacket().getId();
        byte[] payload = Arrays.copyOfRange(
                event.getPacketBufferNode().getPacketBuffer().getPayload(),
                1, event.getPacketBufferNode().getPacketBuffer().getOffset()
        );

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
                case "OP_RESUME_COUNTDIALOG":
                    out = "resumeCountDialoguePacket(" + pd.getMap().get("count") + ");";
                    break;
                case "OP_RESUME_PAUSEBUTTON":
                    out =  "resumePauseWidgetPacket(" + pd.getMap().get("widgetID") + ", " + pd.getMap().get("optionIndex") + ");";
                    break;
                case "OP_RESUME_OBJDIALOG":
                    out = "resumeObjectDialoguePacket(" + pd.getMap().get("id") + ");";
                    break;
                case "OP_RESUME_NAMEDIALOG":
                    out = "resumeNameDialoguePacket(" + pd.getMap().get("var7") + ");";
                    break;
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
                case "OP_WIDGET_TARGET_ON_WIDGET":
                    out = "widgetOnWidgetPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("targetWidgetID") + ", " + pd.getMap().get("identifier2") + ", " + pd.getMap().get("param0") + ");";
                    break;
                case "OP_WIDGET_TARGET_ON_GAME_OBJECT":
                    out = "widgetOnObjectPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + "); //";
                    break;
                case "OP_WIDGET_TARGET_ON_GROUND_ITEM":
                    out = "widgetOnGroundItemPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + pd.getMap().get("worldX") + ", " + pd.getMap().get("worldY") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + ");";
                    break;
                case "OP_WIDGET_TARGET_ON_NPC":
                    out = "widgetOnNpcPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + ");";
                    break;
                case "OP_WIDGET_TARGET_ON_PLAYER":
                    out = "widgetOnPlayerPacket(" + pd.getMap().get("selectedWidgetId") + ", " + pd.getMap().get("itemId") + ", " + pd.getMap().get("slot") + ", " + pd.getMap().get("identifier") + ", " + ((pd.getMap().get("ctrl") == 1) ? "true" : "false") + ");";
                    break;
                case "OP_INTERFACE_CLOSE":
                    out = "interfaceClosePacket();";
                    break;
            }
        }
        pb.release();

        if(out.isBlank() || out.startsWith("[OP_MOUSE_CLICK") || out.startsWith("[OP_FOCUS_CHANGED"))
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

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(name));

                NPC npc = client.getCachedNPCs()[entry.getIdentifier()];

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + npc.getId() + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(npc.getId() + ""));
            }
            else if(opcode == MenuAction.EXAMINE_OBJECT.getId())
            {

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(name));

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + entry.getIdentifier() + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(entry.getIdentifier() + ""));
            }
            else if(opcode == MenuAction.TRADE.getId())
            {
                MenuEntry objectHelper = client.createMenuEntry(1)
                        .setOption("Dev Helper ")
                        .setTarget("<col=ffff00>" + name + " ");

                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(name));
            }
            else if(entry.getOption().equals("Use")) //items
            {
                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(name));

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + entry.getItemId() + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(entry.getItemId() + ""));
            }
            else if(opcode == MenuAction.EXAMINE_ITEM_GROUND.getId())
            {
                client.createMenuEntry(1)
                        .setOption("Copy name ")
                        .setTarget(color + name + " ")
                        .setType(MenuAction.RUNELITE)
                        .onClick(c -> Logging.copyToClipboard(name));

                client.createMenuEntry(1)
                        .setOption("Copy ID ")
                        .setTarget(color + entry.getItemId() + " ")
                        .setType(MenuAction.RUNELITE)
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

                    /*client.createMenuEntry(1)
                            .setOption("Generate Local Path")
                            .setTarget(color + entry.getItemId() + " ")
                            .setType(MenuAction.RUNELITE)
                            .onClick(c -> {
                                ThreadPool.submit(() ->
                                {
                                    TWalker walker = new TWalker(worldPoint);
                                    overlays.updatePath(new ArrayList<>());
                                    List<Step> path = walker.getPath();
                                    overlays.setDest(path.get(path.size() - 1));
                                    overlays.updatePath(path);
                                    walker.walkThreaded();
                                });
                            });*/
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
