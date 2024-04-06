package net.runelite.client.plugins.tscripts.ui;

import lombok.Getter;
import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.types.BreakPoint;
import net.runelite.client.plugins.tscripts.util.Logging;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ExRSyntaxTextArea extends RSyntaxTextArea
{
    @Getter
    private final Map<Integer, BreakPoint> breakpoints = new HashMap<>();
    private DocumentListener scriptListener;

    public ExRSyntaxTextArea(int rows, int cols) {
        super(rows, cols);

        setTheme();
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        setCodeFoldingEnabled(true);
        setCodeFoldingEnabled(true);
        setAnimateBracketMatching(true);
        setAutoIndentEnabled(true);
        setHighlighter(new RSyntaxTextAreaHighlighter());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handleRightClick(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handleRightClick(e);
                }
            }

            private void handleRightClick(MouseEvent e) {
                if (getSelectedText() == null) {
                    int offset = viewToModel2D(e.getPoint());
                    setCaretPosition(offset);
                }
            }
        });

        JPopupMenu popupMenu = getPopupMenu();
        popupMenu.addSeparator();
        popupMenu.add(getToggleBreakpointItem());
    }

    private JMenuItem getToggleBreakpointItem() {
        JMenuItem toggleBreakpointItem = new JMenuItem("Toggle Breakpoint");
        toggleBreakpointItem.addActionListener((ActionEvent e) -> {
            int line = getCaretLineNumber();
            int offset = getWordStartPosAtCaret();
            String word = getWordAtCaret();
            boolean isMethod = MethodManager.getInstance().getMethods().containsKey(word.toLowerCase());
            if(!isMethod)
                return;
            try {
                toggleBreakpoint(line, offset, word);
            } catch (BadLocationException ex) {
                Logging.errorLog(ex);
            }
            repaint();
        });
        return toggleBreakpointItem;
    }

    public String getWordAtCaret() {
        try {
            int caretPosition = getCaretPosition();
            String text = getText();

            // Find the start of the word
            int wordStart = caretPosition;
            while(wordStart > 0 && Character.isLetter(text.charAt(wordStart - 1))) {
                wordStart--;
            }

            // Find the end of the word
            int wordEnd = caretPosition;
            while (wordEnd < text.length() && Character.isLetter(text.charAt(wordEnd))) {
                wordEnd++;
            }

            // Extract and return the word
            return text.substring(wordStart, wordEnd);
        } catch (Exception ex) {
            Logging.errorLog(ex);
            return "";
        }
    }

    public int getWordStartPosAtCaret() {
        try {
            int caretPosition = getCaretPosition();
            String text = getText();

            // Find the start of the word
            int wordStart = caretPosition;
            while(wordStart > 0 && Character.isLetter(text.charAt(wordStart - 1))) {
                wordStart--;
            }
            return wordStart;
        } catch (Exception ex) {
            Logging.errorLog(ex);
            return 0;
        }
    }

    public void toggleBreakpoint(int line, int offset, String word) throws BadLocationException {
        Gutter gutter = ((RTextScrollPane) getParent().getParent()).getGutter();

        if (breakpoints.containsKey(line)) {
            gutter.removeTrackingIcon(breakpoints.get(line).getIcon());
            removeLineHighlight(breakpoints.get(line).getTag());
            breakpoints.remove(line);
        } else {
            GutterIconInfo iconInfo = gutter.addLineTrackingIcon(line, new CircleIcon(Color.RED));
            Object tag = addLineHighlight(line, Color.DARK_GRAY);
            BreakPoint breakPoint = new BreakPoint(line, offset, word, iconInfo, tag);
            breakpoints.put(line, breakPoint);
        }
        repaint();
    }

    public void setScript(String path) throws IOException
    {
        if(scriptListener != null)
            getDocument().removeDocumentListener(scriptListener);

        setText(Files.readString(Paths.get(path)));

        scriptListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                clearBreakpoints();
            }

            public void removeUpdate(DocumentEvent e) {
                clearBreakpoints();
            }

            @SneakyThrows
            public void changedUpdate(DocumentEvent e) {
                Writer fileWriter = new FileWriter(path, false);
                fileWriter.write(getText());
                fileWriter.close();
            }
        };
        getDocument().addDocumentListener(scriptListener);
        clearBreakpoints();
    }

    private void clearBreakpoints()
    {
        if(breakpoints.isEmpty())
            return;
        Gutter gutter = ((RTextScrollPane) getParent().getParent()).getGutter();
        for (BreakPoint breakPoint : breakpoints.values()) {
            gutter.removeTrackingIcon(breakPoint.getIcon());
            removeLineHighlight(breakPoint.getTag());
        }
        breakpoints.clear();
        repaint();
    }

    private void setTheme()
    {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(this);
        } catch (IOException ex) { // Never happens
            Logging.errorLog(ex);
        }
    }

    private static class CircleIcon implements Icon {
        private final Color color;

        public CircleIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, getIconWidth(), getIconHeight());
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}
