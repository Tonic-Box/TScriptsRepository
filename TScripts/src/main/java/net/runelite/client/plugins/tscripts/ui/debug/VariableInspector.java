package net.runelite.client.plugins.tscripts.ui.debug;

import net.runelite.client.plugins.tscripts.util.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.util.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.util.eventbus.events.VariablesCleared;
import net.runelite.client.plugins.tscripts.runtime.Runtime;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VariableInspector extends JPanel {
    private static VariableInspector instance;
    private final JTable variableTable;
    private final DefaultTableModel tableModel;
    private final Runtime runtime;
    private int selectedRow = -1;
    private final List<Integer> frozenRows = new ArrayList<>();
    private final Map<String, Object> variableMap;

    public static VariableInspector getInstance(Runtime runtime) {
        if (instance == null)
            instance = new VariableInspector(runtime);
        return instance;
    }

    private VariableInspector(Runtime runtime) {
        // Set up the table model
        tableModel = new DefaultTableModel(new Object[]{"Variable", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variableTable = new JTable(tableModel);
        variableTable.setFillsViewportHeight(true);

        // Custom cell renderer to change row background colors
        variableTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (frozenRows.contains(row)) {
                    c.setBackground(Color.cyan);
                    c.setForeground(Color.black);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        });

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(variableTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add right-click context menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem freezeItem = new JMenuItem("Toggle Freeze");
        freezeItem.addActionListener(e -> {
            String variableName = (String) tableModel.getValueAt(selectedRow, 0);
            runtime.getVariableMap().toggleFreeze(variableName);
        });
        popupMenu.add(freezeItem);
        variableTable.setComponentPopupMenu(popupMenu);

        // Enable row selection on right-click
        variableTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = variableTable.rowAtPoint(e.getPoint());
                    if (row >= 0) { // Check if a valid row is under the mouse
                        selectedRow = row;
                    }
                }
            }
        });

        this.runtime = runtime;
        this.variableMap = runtime.getVariableMap().getVariableMap();
        updateVariables();
        TEventBus.register(this);
    }

    public void updateVariables() {
        SwingUtilities.invokeLater(() -> {
            // Clear the existing table rows
            tableModel.setRowCount(0);
            frozenRows.clear();

            // Add new rows for each variable
            for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
                Vector<Object> row = new Vector<>();
                if (runtime.getVariableMap().isFrozen(entry.getKey()))
                    frozenRows.add(tableModel.getRowCount());
                row.add(entry.getKey());
                row.add(entry.getValue());
                tableModel.addRow(row);
            }
        });
    }

    @_Subscribe
    public void onVariableUpdate(VariableUpdated event) {
        variableMap.put(event.getName(), event.getValue());
        updateVariables();
    }

    @_Subscribe
    public void onVariablesCleared(VariablesCleared event) {
        variableMap.clear();
        updateVariables();
    }
}