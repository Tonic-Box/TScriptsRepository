package net.runelite.client.plugins.tscripts.adapter.variable;

/**
 * Represents the type of assignment that is being performed.
 */
public enum AssignmentType
{
    ASSIGNMENT,
    INCREMENT,
    ADD_ONE, REMOVE_ONE, DECREMENT;

    public static AssignmentType of(String element)
    {
        switch (element)
        {
            case "++":
                return ADD_ONE;
            case "+=":
                return INCREMENT;
            case "-=":
                return DECREMENT;
            case "--":
                return REMOVE_ONE;
        }
        return ASSIGNMENT;
    }
}
