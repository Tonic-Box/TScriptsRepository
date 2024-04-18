package net.runelite.client.plugins.tscripts.adapter;

import net.runelite.client.plugins.tscripts.util.Logging;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class ErrorListener extends BaseErrorListener {

    public static final ErrorListener INSTANCE = new ErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        Logging.errorLog(new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg));
    }
}
