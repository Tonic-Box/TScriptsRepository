package net.runelite.client.plugins.tscripts.api.library;

import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;

public class TMagic
{
    public static Spell getSpell(String spellName) {
        SpellBook spellbook = SpellBook.getCurrent();
        if(spellbook == null)
            return null;

        Spell spell;

        switch(spellbook)
        {
            case STANDARD:
                spell = getSpell(SpellBook.Standard.class, spellName);
                break;
            case ANCIENT:
                spell = getSpell(SpellBook.Ancient.class, spellName);
                break;
            case LUNAR:
                spell = getSpell(SpellBook.Lunar.class, spellName);
                break;
            default:
                return null;
        }
        return spell;
    }

    private static <T extends Enum<T> & Spell> Spell getSpell(Class<T> spellEnum, String spellName) {
        spellName = spellName.toUpperCase().replace(" ", "_");
        for (T spell : spellEnum.getEnumConstants()) {
            if (spell.name().equalsIgnoreCase(spellName)) {
                return spell;
            }
        }
        return null;
    }
}
