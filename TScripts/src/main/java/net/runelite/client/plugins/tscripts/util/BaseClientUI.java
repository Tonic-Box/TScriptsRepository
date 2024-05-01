package net.runelite.client.plugins.tscripts.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.types.DisableRenderCallbacks;
import net.runelite.client.plugins.tscripts.types.RestoreSize;
import net.runelite.client.ui.ClientPanel;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ContainableFrame;
import net.runelite.client.ui.NavigationButton;
import net.unethicalite.client.Static;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeSet;

@RequiredArgsConstructor
@Getter
public class BaseClientUI
{
    private final ClientUI ui;
    private DrawCallbacks drawCallbacks;
    private final DisableRenderCallbacks disableRenderCallbacks;
    private RestoreSize clientPanelSize;
    @Getter
    private boolean headless = false;

    public BaseClientUI(ClientUI ui)
    {
        this.ui = ui;
        this.drawCallbacks = Static.getClient().getDrawCallbacks();
        this.disableRenderCallbacks = new DisableRenderCallbacks();
    }

    public void toggleHeadless()
    {
        Client client = Static.getClient();
        ClientPanel clientPanel = reflect("clientPanel");;
        ContainableFrame frame = reflect("frame");
        JTabbedPane sidebar = reflect("sidebar");
        TreeSet<NavigationButton> sidebarEntries = reflect("sidebarEntries");

        if(client == null || clientPanel == null || frame == null || sidebar == null || sidebarEntries == null)
            return;

        headless = !headless;

        Boolean await = TGame.invoke(() -> {
            client.setLowCpu(headless);
            client.setDrawCallbacks(headless ? disableRenderCallbacks : drawCallbacks);
            client.changeMemoryMode(headless);
            client.setMinimapReceivesClicks(!headless);
            return true;
        });

        clientPanel.setVisible(!headless);
        if(headless)
        {
            if(!sidebar.isVisible() || sidebar.getSelectedIndex() < 0)
            {
                invoke("togglePluginPanel");
            }
            clientPanelSize = new RestoreSize(clientPanel);
            clientPanel.setPreferredSize(RestoreSize.HIDDEN);
            clientPanel.setSize(RestoreSize.HIDDEN);
            clientPanel.setMaximumSize(RestoreSize.HIDDEN);
            clientPanel.setMinimumSize(RestoreSize.HIDDEN);

        }
        else
        {
            clientPanelSize.restore(clientPanel);
        }

        frame.pack();
        frame.revalidateMinimumSize();
    }

    @SuppressWarnings("unchecked")
    private <T> T reflect(String name)
    {
        try {
            Class<?> clazz = ui.getClass();
            Field clientPanelField = clazz.getDeclaredField(name);
            clientPanelField.setAccessible(true);
            return (T)clientPanelField.get(ui);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }


    private void invoke(String methodName, Object... args) {
        try {
            Class<?> clazz = ui.getClass();
            Class<?>[] argClasses = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argClasses[i] = args[i].getClass();
            }
            Method method = clazz.getDeclaredMethod(methodName, argClasses);
            method.setAccessible(true);
            method.invoke(ui, args);
        }
        catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
        }
    }
}
