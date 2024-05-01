package net.runelite.client.plugins.tscripts.types;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
public class RestoreSize
{
    private final Dimension size;
    private final Dimension preferredSize;
    private final Dimension minimumSize;
    private final Dimension maximumSize;
    public final static Dimension HIDDEN = new Dimension(1, 200);
    public RestoreSize(JComponent component)
    {
        this.size = component.getSize();
        this.preferredSize = component.getPreferredSize();
        this.minimumSize = component.getMinimumSize();
        this.maximumSize = component.getMaximumSize();
    }

    public void restore(JComponent component)
    {
        component.setSize(size);
        component.setPreferredSize(preferredSize);
        component.setMinimumSize(minimumSize);
        component.setMaximumSize(maximumSize);
    }
}
