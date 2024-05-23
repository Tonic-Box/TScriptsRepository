package net.runelite.client.plugins.tscripts.sevices.localpathfinder;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TileOverlay extends Overlay
{
    private final Client client;
    private final List<Step> path = new CopyOnWriteArrayList<>();
    private Step dest = null;

    public TileOverlay(Client client)
    {
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_MED);
    }

    public void updatePath(List<Step> path)
    {
        this.path.clear();
        this.path.addAll(path);
    }

    public void addStep(Step step)
    {
        this.path.add(step);
    }

    public void setStart(Step step)
    {
        this.path.add(0, step);
    }

    public void setDest(Step dest)
    {
        this.dest = dest;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(dest != null)
        {
            renderTile(graphics, LocalPoint.fromWorld(client, dest.getPosition()), Color.GREEN);
        }
        for (Step step : path)
        {
            renderTile(graphics, LocalPoint.fromWorld(client, step.getPosition()), Color.RED);
        }

        return null;
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, Color color)
    {
        if (dest == null)
        {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

        if (poly == null)
        {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, color, color, new BasicStroke((float) (double) 3));
    }
}
