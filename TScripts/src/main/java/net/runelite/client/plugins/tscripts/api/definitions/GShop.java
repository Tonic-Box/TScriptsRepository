package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TShop;
import net.runelite.client.plugins.tscripts.types.*;

import java.util.ArrayList;
import java.util.List;

public class GShop implements GroupDefinition
{
    @Override
    public String groupName() {
        return "Shops";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();

        addMethod(methods, "buyItem1",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Integer)
                        TShop.buy1((int) item);
                    else if(item instanceof String)
                        TShop.buy1((String) item);
                },
                "Buys 1 of an item from a shop"
        );
        addMethod(methods, "buyItem5",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Integer)
                        TShop.buy5((int) item);
                    else if(item instanceof String)
                        TShop.buy5((String) item);
                },
                "Buys 5 of an item from a shop"
        );
        addMethod(methods, "buyItem10",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Integer)
                        TShop.buy10((int) item);
                    else if(item instanceof String)
                        TShop.buy10((String) item);
                },
                "Buys 10 of an item from a shop"
        );
        addMethod(methods, "buyItem50",
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Integer)
                        TShop.buy50((int) item);
                    else if(item instanceof String)
                        TShop.buy50((String) item);
                },
                "Buys 50 of an item from a shop"
        );

        addMethod(methods, "isShopOpen", Type.BOOL,
                ImmutableMap.of(),
                function -> ShopID.getCurrent() != null,
                "Returns true if a shop is open"
        );

        addMethod(methods, "shopQuantityOf", Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Integer)
                        return TShop.getStockQuantity((int) item);
                    else if(item instanceof String)
                        return TShop.getStockQuantity((String) item);
                    else
                        return 0;
                },
                "Returns the quantity of an item in the shop"
        );

        addMethod(methods, "shopContains", Type.BOOL,
                ImmutableMap.of(
                        0, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    Object item = function.getArg(0, manager);
                    if(item instanceof Integer)
                        return TShop.shopContains((int) item);
                    else if(item instanceof String)
                        return TShop.shopContains((String) item);
                    else
                        return 0;
                },
                "Returns true if a shop contains an item"
        );

        return methods;
    }
}
