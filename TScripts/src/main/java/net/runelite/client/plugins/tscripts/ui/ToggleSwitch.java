package net.runelite.client.plugins.tscripts.ui;

import lombok.Getter;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple toggle switch component
 */
public class ToggleSwitch extends JPanel {
    @Getter
    private boolean activated = false;
    private final Color switchColor = ColorScheme.DARK_GRAY_COLOR;//new Color(200, 200, 200);
    private final Color buttonColor = ColorScheme.DARKER_GRAY_COLOR;//new Color(255, 255, 255);
    private Color borderColor = new Color(50, 50, 50);
    private final Color activeSwitch = ColorScheme.MEDIUM_GRAY_COLOR;//new Color(0, 125, 255);
    private BufferedImage puffer;
    private Graphics2D g;
    private final List<ActionListener> actionListeners = new ArrayList<>();

    /**
     * Creates a new toggle switch
     */
    public ToggleSwitch() {
        super();
        setVisible(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                activated = !activated;
                repaint();
                // Notify all registered ActionListeners
                for (ActionListener listener : actionListeners) {
                    listener.actionPerformed(null);
                }
            }
        });
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBounds(0, 0, 41, 21);
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    @Override
    public void paint(Graphics gr) {
        if(g == null || puffer.getWidth() != getWidth() || puffer.getHeight() != getHeight()) {
            puffer = (BufferedImage) createImage(getWidth(), getHeight());
            g = (Graphics2D)puffer.getGraphics();
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHints(rh);
        }
        g.setColor(activated?activeSwitch:switchColor);
        int borderRadius = 5;
        g.fillRoundRect(0, 0, this.getWidth()-1,getHeight()-1, 5, borderRadius);
        g.setColor(borderColor);
        g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, borderRadius);
        g.setColor(buttonColor);
        if(activated) {
            g.fillRoundRect(getWidth()/2, 1,  (getWidth()-1)/2 -2, (getHeight()-1) - 2, borderRadius, borderRadius);
            g.setColor(borderColor);
            g.drawRoundRect((getWidth()-1)/2, 0, (getWidth()-1)/2, (getHeight()-1), borderRadius, borderRadius);
        }
        else {
            g.fillRoundRect(1, 1, (getWidth()-1)/2 -2, (getHeight()-1) - 2, borderRadius, borderRadius);
            g.setColor(borderColor);
            g.drawRoundRect(0, 0, (getWidth()-1)/2, (getHeight()-1), borderRadius, borderRadius);
        }

        gr.drawImage(puffer, 0, 0, null);
    }

    /**
     * Set the state of the switch
     * @param activated true if the switch should be activated
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Border-color of whole switch and switch-button
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

}
