package net.runelite.client.plugins.tscripts.ui.editor.debug;

import net.runelite.client.plugins.tscripts.runtime.variables.ArrayVariable;
import net.runelite.client.plugins.tscripts.runtime.variables.Frame;
import net.runelite.client.plugins.tscripts.runtime.variables.Frames;
import net.runelite.client.plugins.tscripts.runtime.variables.Variable;
import net.runelite.client.plugins.tscripts.sevices.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.sevices.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.FramePopped;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.VariableUpdated;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.VariablesCleared;
import net.runelite.client.plugins.tscripts.runtime.Runtime;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * A panel that displays a table of variables and their values.
 */
public class VariableInspector extends JPanel {
    private static VariableInspector instance;
    private final JTable variableTable;
    private final DefaultTableModel tableModel;
    private int selectedRow = -1;
    private final List<Integer> frozenRows = new ArrayList<>();
    private final Frames variableMap;

    public static VariableInspector getInstance(Runtime runtime) {
        if (instance == null)
            instance = new VariableInspector(runtime);
        return instance;
    }

    private VariableInspector(Runtime runtime) {
        // Set up the table model
        tableModel = new DefaultTableModel(new Object[]{"Variable", "Value", "Frame"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variableTable = new JTable(tableModel);
        variableTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = variableTable.getColumnModel();
        columnModel.getColumn(2).setMinWidth(0);

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
            String hash = (String) tableModel.getValueAt(selectedRow, 2);
            runtime.getVariableMap().toggleFreeze(variableName, hash);
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

        this.variableMap = runtime.getVariableMap().getFrames();
        updateVariables();
        TEventBus.register(this);
    }

    public void updateVariables() {
        SwingUtilities.invokeLater(() -> {
            // Clear the existing table rows
            tableModel.setRowCount(0);
            frozenRows.clear();

            for (int i = 0; i < variableMap.getFrames().size(); i++) {
                Frame frame = variableMap.getFrames().get(i);
                for (Map.Entry<String, Variable> entry : frame.getVariableMap().entrySet()) {
                    Vector<Object> row = new Vector<>();
                    if (entry.getValue().isFrozen())
                        frozenRows.add(tableModel.getRowCount());
                    row.add(entry.getValue().getName());
                    Object value = entry.getValue().getValue();
                    Object trueValue;
                    if(value instanceof Variable)
                        trueValue = ((Variable) value).getValue();
                    else
                        trueValue = value;
                    row.add(trueValue);
                    row.add("Frame [" + i + "]");
                    tableModel.addRow(row);
                }

//                for (Map.Entry<String, ArrayVariable> entry : frame.getArrayMap().entrySet()) {
//                    Vector<Object> row = new Vector<>();
//                    row.add(entry.getValue().getName());
//                    Object value = entry.getValue().getValues();
//                    Object trueValue;
//                    if(value instanceof Variable)
//                        trueValue = ((Variable) value).getValue();
//                    else
//                        trueValue = value;
//                    row.add(trueValue);
//                    row.add("Frame [" + i + "]");
//                    tableModel.addRow(row);
//                }
            }
        });
    }

    @_Subscribe
    public void onVariableUpdate(VariableUpdated event)
    {
        variableMap.put(event.getName(), new Variable(event.getName(), event.getValue()));
        updateVariables();
    }

    @_Subscribe
    public void onVariablesCleared(VariablesCleared event)
    {
        variableMap.clean();
        updateVariables();
    }

    @_Subscribe
    public void onFramePopped(FramePopped event)
    {
        updateVariables();
    }
}