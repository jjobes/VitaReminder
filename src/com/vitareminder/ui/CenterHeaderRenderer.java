package com.vitareminder.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;


/**
 * Applied to the supplement and regimen {@code JTables} in {@code VitaReminderPanel}
 * to center the column names.
 */
public class CenterHeaderRenderer implements TableCellRenderer
{
    DefaultTableCellRenderer renderer;

    public CenterHeaderRenderer(JTable table)
    {
        renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
    }


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column)
    {
        return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}  // end class CenterHeaderRenderer
