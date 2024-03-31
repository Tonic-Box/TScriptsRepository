package net.runelite.client.plugins.tscripts.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fife.ui.rtextarea.GutterIconInfo;

@AllArgsConstructor
@Getter
public class BreakPoint
{
    private final int line;
    private final int offset;
    private final String method;
    private final GutterIconInfo icon;
    private final Object tag;
}
