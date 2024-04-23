package net.runelite.client.plugins.tscripts.types;

import lombok.Getter;

@Getter
public class TriPair<L, C, R>
{
    private final L left;
    private final C center;
    private final R right;

    public TriPair(L left, C center, R right)
    {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    public static <L, C, R> TriPair<L, C, R> of(L left, C center, R right)
    {
        return new TriPair<>(left, center, right);
    }

    @Override
    public int hashCode()
    {
        return 31 * left.hashCode() + center.hashCode() + right.hashCode();
    }

    @Override
    public String toString()
    {
        return "TriPair{" +
                "left=" + left +
                ", center=" + center +
                ", right=" + right +
                '}';
    }
}
