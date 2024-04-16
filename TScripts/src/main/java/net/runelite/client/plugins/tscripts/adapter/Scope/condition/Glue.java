package net.runelite.client.plugins.tscripts.adapter.Scope.condition;

public enum Glue
{
    AND,
    OR;

    public static Glue of(String text)
    {
        switch (text)
        {
            case "&&":
                return AND;
            case "||":
                return OR;
            default:
                throw new IllegalArgumentException("Unknown glue: " + text);
        }
    }
}
