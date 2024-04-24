package net.runelite.client.plugins.tscripts.sevices;

import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.types.ShopID;
import net.unethicalite.client.Static;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ItemContainer Query class
 * @param <T> container
 */
public class ItemContainerQuery<T extends ItemContainer> {
    private final List<Item> cache;

    /**
     * ItemContainerQuery constructor
     * @param itemContainer container
     */
    public ItemContainerQuery(T itemContainer)
    {
        if(itemContainer != null)
            this.cache = TGame.invoke(() ->
                    Arrays.stream(itemContainer.getItems())
                            .filter(i -> i.getId() != -1)
                            .collect(Collectors.toList())
            );
        else
            this.cache = new ArrayList<>();
    }

    /**
     * ItemContainerQuery constructor 2
     * @param inventoryId InventoryID
     */
    public ItemContainerQuery(InventoryID inventoryId)
    {
        ItemContainer itemContainer = TGame.invoke(() -> Static.getClient().getItemContainer(inventoryId));
        if(itemContainer != null)
            this.cache = TGame.invoke(() ->
                    Arrays.stream(itemContainer.getItems())
                            .filter(i -> i.getId() != -1)
                            .collect(Collectors.toList())
            );
        else
            this.cache = new ArrayList<>();
    }

    /**
     * ItemContainerQuery constructor 2
     * @param inventoryId InventoryID
     */
    public ItemContainerQuery(ShopID inventoryId)
    {
        if(inventoryId == null)
        {
            this.cache = new ArrayList<>();
            return;
        }
        ItemContainer itemContainer = TGame.invoke(() -> Static.getClient().getItemContainer(inventoryId.getItemContainerId()));
        if(itemContainer != null)
            this.cache = TGame.invoke(() ->
                    Arrays.stream(itemContainer.getItems())
                            .filter(i -> i.getId() != -1)
                            .collect(Collectors.toList())
            );
        else
            this.cache = new ArrayList<>();
    }

    /**
     * filter by item id
     * @param id item id
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> withId(int id)
    {
        cache.removeIf(o -> o.getId() != id);
        return this;
    }

    /**
     * filter by item id/noted id
     * @param id item id
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> withId2(int id)
    {
        cache.removeIf(o -> o.getId() != id && o.getNotedId() != id);
        return this;
    }

    /**
     * filter by item name
     * @param name item name
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> withName(String name)
    {
        cache.removeIf(o -> !o.getName().equalsIgnoreCase(name));
        return this;
    }

    public ItemContainerQuery<T> withAction(String action)
    {
        cache.removeIf(o -> o.getActions() != null && Arrays.stream(o.getActions()).noneMatch(a -> a != null && a.contains(action)));
        return this;
    }

    /**
     * exclude items in the specified slots
     * @param slots slots
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> excludeSlots(int... slots)
    {
        cache.removeIf(i -> ArrayUtils.contains(slots, i.getSlot()));
        return this;
    }

    /**
     * exclude items not in the specified slots
     * @param slots slots
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> includeSlots(int... slots)
    {
        cache.removeIf(i -> !ArrayUtils.contains(slots, i.getSlot()));
        return this;
    }

    /**
     * filter by partial name match (case insensitive)
     * @param namePart name partial
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> withPartialName(String namePart)
    {
        cache.removeIf(o -> o.getName().toLowerCase().contains(namePart.toLowerCase()));
        return this;
    }

    /**
     * remove by predicate
     * @param predicate condition
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> removeIf(Predicate<Item> predicate)
    {
        cache.removeIf(predicate);
        return this;
    }

    /**
     * keep by predicate
     * @param predicate condition
     * @return ItemContainerQuery\<T\>
     */
    public ItemContainerQuery<T> keepIf(Predicate<Item> predicate)
    {
        cache.removeIf(predicate.negate());
        return this;
    }

    /**
     * get current list from query
     * @return list
     */
    public List<Item> collect()
    {
        return cache;
    }

    /**
     * equivalent of findFirst().orElse(null);
     * @return list
     */
    public Item findFirst()
    {
        if(cache.isEmpty())
            return null;
        return cache.get(0);
    }

    /**
     * get the quantity of all items left in the list
     * @return quantity
     */
    public int getQuantity()
    {
        int count = 0;
        for(Item item : cache)
        {
            count += item.getQuantity();
        }
        return count;
    }
}