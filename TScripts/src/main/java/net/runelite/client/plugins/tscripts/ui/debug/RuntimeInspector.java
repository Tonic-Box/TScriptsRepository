package net.runelite.client.plugins.tscripts.ui.debug;

import net.runelite.client.plugins.tscripts.runtime.Runtime;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

public class RuntimeInspector extends JPanel
{
    private static RuntimeInspector instance;
    private final JTable variableTable;
    private final DefaultTableModel tableModel;
    private final Runtime runtime;

    public static RuntimeInspector getInstance(Runtime runtime) {
        if (instance == null)
            instance = new RuntimeInspector(runtime);
        return instance;
    }

    private RuntimeInspector(Runtime runtime) {
        this.runtime = runtime;
        tableModel = new DefaultTableModel(new Object[]{"Flag", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variableTable = new JTable(tableModel);
        variableTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(variableTable);
        add(scrollPane, BorderLayout.CENTER);

        updateVariables();
    }

    private void updateVariables() {
        SwingUtilities.invokeLater(() -> {
            // Clear the existing table rows
            tableModel.setRowCount(0);

            // Add new rows for each variable
            for (Map.Entry<String, Object> entry : runtime.dumpFlags().entrySet()) {
                Vector<Object> row = new Vector<>();
                row.add(entry.getKey());
                row.add(entry.getValue());
                tableModel.addRow(row);
            }
        });
    }

    public static void updateTelemetry()
    {
        if(instance == null)
            return;
        instance.updateVariables();
    }
}
