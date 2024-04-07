package net.runelite.client.plugins.tscripts.ui.editor.debug;

import lombok.SneakyThrows;
import net.runelite.client.plugins.tscripts.lexer.Tokenizer;
import net.runelite.client.plugins.tscripts.lexer.models.Token;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

public class TokenDumper  extends JPanel
{
    private static TokenDumper instance;
    private final DefaultTableModel tableModel;

    public static TokenDumper getInstance() {
        if (instance == null)
            instance = new TokenDumper();
        return instance;
    }

    private TokenDumper() {
        tableModel = new DefaultTableModel(new Object[]{"Line Number", "Token Type", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable variableTable = new JTable(tableModel);
        variableTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(variableTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    @SneakyThrows
    public void dump(Path scriptPath)
    {
        List<Token> tokens = Tokenizer.parse(Files.readString(scriptPath));
        SwingUtilities.invokeLater(() -> {
            // Clear the existing table rows
            tableModel.setRowCount(0);

            // Add new rows for each variable
            for (Token token : tokens) {
                Vector<Object> row = new Vector<>();
                row.add(token.getLine());
                row.add(token.getType().name());
                row.add(token.getValue());
                tableModel.addRow(row);
            }
        });
    }
}
