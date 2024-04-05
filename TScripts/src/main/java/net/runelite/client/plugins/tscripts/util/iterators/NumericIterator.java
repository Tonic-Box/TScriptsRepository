package net.runelite.client.plugins.tscripts.util.iterators;

/**
 * This class is used to iterate through numbers.
 */
public class NumericIterator
{
    private int currentNum;

    /**
     * Constructor for the NumericIterator class.
     */
    public NumericIterator()
    {
        this.currentNum = -1;
    }

    public void reset()
    {
        this.currentNum = -1;
    }

    /**
     * Gets the next number in the sequence.
     *
     * @return The next number in the sequence.
     */
    public int getNextNumber()
    {
        currentNum++;

        return currentNum;
    }
}
