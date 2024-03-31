package net.runelite.client.plugins.tscripts.ui.debug;

import net.runelite.client.plugins.tscripts.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.eventbus.events.FlagChanged;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

public class RuntimeInspector extends JPanel
{
    private static RuntimeInspector instance;
    private final DefaultTableModel tableModel;

    public static RuntimeInspector getInstance() {
        if (instance == null)
            instance = new RuntimeInspector();
        return instance;
    }

    private RuntimeInspector() {
        tableModel = new DefaultTableModel(new Object[]{"Flag", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable variableTable = new JTable(tableModel);
        variableTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(variableTable);
        add(scrollPane, BorderLayout.CENTER);
        TEventBus.register(this);
    }

    @_Subscribe
    public void onRuntimeTelemetry(FlagChanged event) {
        SwingUtilities.invokeLater(() -> {
            // Clear the existing table rows
            tableModel.setRowCount(0);

            // Add new rows for each variable
            for (Map.Entry<String, Object> entry : event.getFlags().entrySet()) {
                Vector<Object> row = new Vector<>();
                row.add(entry.getKey());
                row.add(entry.getValue());
                tableModel.addRow(row);
            }
        });
    }
}
