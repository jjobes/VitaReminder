package com.vitareminder.ui.model;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.vitareminder.business.Regimen;
import com.vitareminder.dao.DAOManager;


/**
 * This class specifies the table model that underlies the {@code regimenTable}
 * in the {@code VitaReminderPanel}.  This class extends {@code AbstractTableModel}
 * as opposed to a {@code DefaultTableModel} because we will be using a {@code List}
 * for our underlying data model as opposed to a {@code Vector} of {@code Vector}s.
 * The data model, {@code regimens}, is actually a {@code List} of {@code Regimen}
 * objects, where each {@code Regimen} contains a {@code List} of its {@code Supplement}s.
 * These {@code List}s are implemented internally as {@code ArrayList}s.  {@code ArrayList}s
 * were chosen because there is currently no need for them to be synchronized.
 */
@SuppressWarnings("serial")
public class RegimenTableModel extends AbstractTableModel
{
    private List<Regimen> regimens;
    private JFrame frame;
    private DAOManager daoManager;
    private String[] columnNames;


    /**
     * The constructor for the {@code RegimenTableModel}.
     *
     * @param frame  a reference to the application's main {@code JFrame}, used to set the parent
     *               window of the {@code JOptionPane}s.
     * @param regimens  the {@code List} of {@code Regimen} objects that will become this
     *                  table model's data model
     * @param daoManager  a reference to the application's DAO layer and used to access the database
     * @param columnNames  the column names that will be visible in the {@code JTable}'s column header
     */
    public RegimenTableModel(JFrame frame, List<Regimen> regimens,
            DAOManager daoManager, String[] columnNames)
    {
        this.frame = frame;
        this.regimens = regimens;
        this.daoManager = daoManager;
        this.columnNames = columnNames;
    }


    /**
     * Used by the {@code JTable} to determine how many columns it should display.
     *
     * @return  the number of columns, determined by the length of the {@code columnNames} array
     */
    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }


    /**
     * Called by the {@code JTable} to determine how many rows it should display.
     *
     * @return  the number of rows in the data model (the number of {@code Regimen}s
     *          in the {@code ArrayList})
     */
    @Override
    public int getRowCount()
    {
        return regimens.size();
    }


    /**
     * Called by the {@code JTable} to retrieve the value at the specified cell.
     *
     * @param row  the row in the model of the desired cell
     * @param col  the column in the model of the desired cell
     * @return  the value at the specified cell, cast to an {@code Object}
     */
    @Override
    public Object getValueAt(int row, int col)
    {
        if (regimens.size() > 0)
        {
            switch (col)
            {
            case 0:
                return (Object) regimens.get(row).getRegimenID();
            case 1:
                return (Object) regimens.get(row).getRegimenName();
            case 2:
                return (Object) regimens.get(row).getRegimenNotes();
            default:
                return null;
            }
        }
        else
        {
            return null;
        }
    }


    /**
     * Called by the {@code JTable} to set the column's header name
     *
     * @param col  the column whose name should be returned
     * @return  the column's name from the {@code columnNames} array
     */
    @Override
    public String getColumnName(int col)
    {
        return columnNames[col];
    }


    /**
     * Called by the {@code JTable} to determine the type of renderer that should be
     * used to display the cells in this column.
     *
     * @param col  the column whose class should be returned
     * @return  the class of the column
     */
    @Override
    public Class<?> getColumnClass(int col)
    {
        return getValueAt(0, col).getClass();
    }


    /**
     * Sets the value of the cell at the specified row and column.  This
     * implementation first updates the database and only if this is
     * successful will it update the table model.
     * <p>
     * The operations are performed in this order to avoid ending up with
     * a table model that is out of sync with the regimens table in the
     * database.
     *
     * @param value  the new value to be set
     * @param row  the row in the model where the target cell resides
     * @param col  the column in the model where the target cell resides
     */
    @Override
    public void setValueAt(Object value, int row, int col)
    {
        int regimenID = (int) regimens.get(row).getRegimenID();

        String field = "";

        switch (col)
        {
        case 1:
            field = "regimen_name";
            break;
        case 2:
            field = "regimen_notes";
            break;
        }

        boolean rowUpdated = daoManager.getRegimenDAO().updateRegimen(regimenID, field, value);

        if (rowUpdated)
        {
            switch (col)
            {
            case 1:
                regimens.get(row).setRegimenName((String) value);
                break;
            case 2:
                regimens.get(row).setRegimenNotes((String) value);
                break;
            }

            fireTableCellUpdated(row, col);
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Error updating regimen data.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Called by the {@code JTable} to determine which cells, if any, are editable
     * through the table interface.  All direct editing of the table model via
     * the {@code JTable} has been disabled.
     */
    @Override
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }


    /**
     * Adds a new row to the regimens database table and table model.  In this
     * implementation, we're adding a new {@code Regimen} object to the
     * {@code regimens} {@code List}.  But before the new {@code Regimen}
     * is added to the {@code regimens} model, it is first added to the
     * regimens table in the database.  If ths is successful, this {@code Regimen}
     * is then added to the model.
     * <p>
     * The operations are performed in this order for two reasons.  First, we
     * don't want the data model to be out of sync with the database table.
     * Second, we need a new primary key for the new {@code Regimen} object's
     * {@code regimenID} field.
     *
     * @param regimen  the {@code Regimen} object to be added to the database and model
     */
    public void addRow(Regimen regimen)
    {
        Regimen newRowWithPK = daoManager.getRegimenDAO().addRegimen(regimen);

        if (newRowWithPK != null)
        {
            regimens.add(newRowWithPK);

            fireTableRowsInserted(regimens.size()-1, regimens.size()-1);
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Error adding regimen data.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Removes a row from the regimens database table and table model.  It
     * first attempts to remove the row from the database table, and if this
     * is successful, it then removes the row from the model.
     *
     * @param row  the row to be removed from the database table and table model
     */
    public void removeRow(int row)
    {
        int regimenID = (int) regimens.get(row).getRegimenID();

        boolean rowRemoved = daoManager.getRegimenDAO().deleteRegimen(regimenID);

        if (rowRemoved)
        {
            regimens.remove(row);

            fireTableDataChanged();
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Error deleting regimen.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     *
     *
     * @param newRegimens
     */
    public void setModel(List<Regimen> newRegimens)
    {
        this.regimens = newRegimens;

        fireTableDataChanged();
    }

}  // end class RegimenTableModel
