package net.runelite.client.plugins.tscripts.types;

import lombok.Getter;

/**
 * A simple key-value pair.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@Getter
public class Pair<K, V> {
    private final K key;
    private final V value;

    /**
     * Creates a new pair.
     *
     * @param key the key
     * @param value the value
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new pair.
     * @param key the key
     * @param value the key
     * @return the pair
     */
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) && value.equals(pair.value);
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + value.hashCode();
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}