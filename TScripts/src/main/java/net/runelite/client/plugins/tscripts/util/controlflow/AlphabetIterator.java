package net.runelite.client.plugins.tscripts.util.controlflow;

/**
 * This class is used to iterate through the alphabet.
 */
public class AlphabetIterator
{
    private char currentChar;
    private char prefixChar = '*';
    private String userPrefix = "";

    /**
     * Creates a new AlphabetIterator with the given prefix.
     *
     * @param prefix The prefix to use.
     */
    public AlphabetIterator(String prefix)
    {
        this.currentChar = 'A' - 1;
        this.userPrefix = prefix;
    }

    public void reset()
    {
        this.currentChar = 'A' - 1;
        this.prefixChar = '*';
    }

    /**
     * Gets the next letter in the alphabet.
     *
     * @return The next letter in the alphabet.
     */
    public String getNextLetter()
    {
        currentChar++;

        if (currentChar > 'Z')
        {
            currentChar = 'A';
            if(prefixChar == '*')
            {
                prefixChar = 'A';
            }
            else
            {
                prefixChar++;
            }
        }

        if(prefixChar != '*')
            return userPrefix + prefixChar + currentChar;
        else
            return userPrefix + currentChar;
    }
}