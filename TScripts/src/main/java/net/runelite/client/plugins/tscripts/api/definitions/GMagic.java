package net.runelite.client.plugins.tscripts.api.definitions;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;

public class GMagic implements GroupDefinition {
    @Override
    public String groupName() {
        return "WidgetOn";
    }

    @Override
    public List<MethodDefinition> methods(MethodManager manager) {
        List<MethodDefinition> methods = new ArrayList<>();
        addMethod(methods, "spellOnItem",
                ImmutableMap.of(
                        0, Pair.of("spell", Type.STRING),
                        1, Pair.of("item", Type.ANY)
                ),
                function ->
                {
                    String _spell = function.getArg(0, manager);
                    Object _item = function.getArg(1, manager);

                    Item item = Api.getItem(_item);
                    if(item == null)
                        return;

                    SpellBook spellbook = SpellBook.getCurrent();
                    if(spellbook == null)
                        return;

                    Spell spell = Api.getSpell(_spell);
                    if(spell == null)
                        return;

                    Magic.cast(spell, item);
                }, "Casts a spell on an item");
        addMethod(methods, "spellOnPlayer",
                ImmutableMap.of(
                        0, Pair.of("spell", Type.STRING),
                        1, Pair.of("player", Type.STRING)
                ),
                function ->
                {
                    String _spell = function.getArg(0, manager);
                    Object _player = function.getArg(1, manager);

                    Player player = new PlayerQuery().filter(p -> p.getName().equals(_player)).result(Static.getClient()).first();
                    if(player == null)
                        return;

                    SpellBook spellbook = SpellBook.getCurrent();
                    if(spellbook == null)
                        return;

                    Spell spell = Api.getSpell(_spell);
                    if(spell == null)
                        return;

                    Magic.cast(spell, player);
                }, "Casts a spell on a player");
        addMethod(methods, "spellOnNpc",
                ImmutableMap.of(
                        0, Pair.of("spell", Type.STRING),
                        1, Pair.of("npc", Type.ANY)
                ),
                function ->
                {
                    String _spell = function.getArg(0, manager);
                    Object _npc = function.getArg(1, manager);

                    NPC npc = Api.getNpc(_npc);
                    if(npc == null)
                        return;

                    SpellBook spellbook = SpellBook.getCurrent();
                    if(spellbook == null)
                        return;

                    Spell spell = Api.getSpell(_spell);
                    if(spell == null)
                        return;

                    Magic.cast(spell, npc);
                }, "Casts a spell on an npc");
        addMethod(methods, "spellOnNpc",
                ImmutableMap.of(
                        0, Pair.of("spell", Type.STRING),
                        1, Pair.of("npc", Type.ANY)
                ),
                function ->
                {
                    String _spell = function.getArg(0, manager);
                    Object _object = function.getArg(1, manager);

                    TileObject object = Api.getObject(_object);
                    if(object == null)
                        return;

                    SpellBook spellbook = SpellBook.getCurrent();
                    if(spellbook == null)
                        return;

                    Spell spell = Api.getSpell(_spell);
                    if(spell == null)
                        return;

                    Magic.cast(spell, object);
                }, "Casts a spell on an object");
        addMethod(methods, "castSpell",
                ImmutableMap.of(
                        0, Pair.of("spell", Type.STRING)
                ),
                function ->
                {
                    String _spell = function.getArg(0, manager);

                    SpellBook spellbook = SpellBook.getCurrent();
                    if(spellbook == null)
                        return;

                    Spell spell = Api.getSpell(_spell);
                    if(spell == null)
                        return;

                    Magic.cast(spell);
                }, "Casts a spell");
        return methods;
    }
}
