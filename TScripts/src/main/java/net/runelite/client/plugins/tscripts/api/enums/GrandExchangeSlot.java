package net.runelite.client.plugins.tscripts.api.enums;

import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.util.TextUtil;
import net.unethicalite.client.Static;

public enum GrandExchangeSlot
{
    SLOT_1(30474247, 1, WidgetInfo.GRAND_EXCHANGE_OFFER1),
    SLOT_2(30474248, 2, WidgetInfo.GRAND_EXCHANGE_OFFER2),
    SLOT_3(30474249, 3, WidgetInfo.GRAND_EXCHANGE_OFFER3),
    SLOT_4(30474250, 4, WidgetInfo.GRAND_EXCHANGE_OFFER4),
    SLOT_5(30474251, 5, WidgetInfo.GRAND_EXCHANGE_OFFER5),
    SLOT_6(30474252, 6, WidgetInfo.GRAND_EXCHANGE_OFFER6),
    SLOT_7(30474253, 7, WidgetInfo.GRAND_EXCHANGE_OFFER7),
    SLOT_8(30474254, 8, WidgetInfo.GRAND_EXCHANGE_OFFER8);

    @Getter
    private final int id;

    @Getter
    private final int slot;

    @Getter
    private final int buyChild = 3;

    @Getter
    private final int sellChild = 4;

    @Getter
    private final WidgetInfo info;

    GrandExchangeSlot(int id, int slot, WidgetInfo info)
    {
        this.id = id;
        this.slot = slot;
        this.info = info;
    }

    public boolean isDone()
    {
        return TGame.invoke(() ->{
            Widget widget = Static.getClient().getWidget(info);
            if(widget == null)
            {
                return false;
            }
            Widget child = widget.getChild(22);
            if(child == null || child.isHidden())
            {
                return false;
            }
            return TextUtil.getHex(child.getTextColor()).equals("5f00");
        });
    }

    public static GrandExchangeSlot getBySlot(int slot)
    {
        for(GrandExchangeSlot s : GrandExchangeSlot.values()) {
            if(s.getSlot() == slot) {
                return s;
            }
        }
        return null;
    }
}
