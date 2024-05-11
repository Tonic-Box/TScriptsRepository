package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.plugins.tscripts.util.iterators.AlphabetIterator;

public class ScriptThread implements Runnable
{
    private final static AlphabetIterator iterator = new AlphabetIterator("ScriptThread_");
    private final Runnable runnable;
    @Getter
    private final String name = iterator.getNextLetter();

    public ScriptThread(Runnable runnable)
    {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(name);
        runnable.run();
    }
}
