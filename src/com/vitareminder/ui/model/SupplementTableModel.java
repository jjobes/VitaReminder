package com.vitareminder.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.vitareminder.business.Regimen;
import com.vitareminder.business.Supplement;
import com.vitareminder.dao.DAOManager;


/**
 * This class specifies the table model that underlies the {@code supplementTable}
 * in the {@code VitaReminderPanel}.  This class extends {@code AbstractTableModel}
 * as opposed to a {@code DefaultTableModel} because we will be using a {@code List}
 * for our underlying data model as opposed to a {@code Vector} of {@code Vector}s.
 * The data model, {@code regimens}, is actually a {@code List} of {@code Regimen}
 * objects, where each {@code Regimen} contains a {@code List} of its {@code Supplement}s.
 * These {@code List}s are implemented internally as {@code ArrayList}s.  {@code ArrayList}s
 * were chosen because there is currently no need for them to be synchronized.
 */
@SuppressWarnings("serial")
public class SupplementTableModel extends AbstractTableModel
{
    private JFrame frame;
    private List<Regimen> regimens;
    private DAOManager daoManager;
    private String[] columnNames;

    /** the {@code regimenID} of the presently selected row in the {@code regimenTable} */
    private int currentRegimenID;

    private Preferences userPreferences;


    /**
     * The constructor for the {@code SupplementTableModel}.  In addition to setting the
     * parameters to class variables, the constructor also acquires a reference to the
     * user's root preference node {@code userPreferences}, which is used by the methods
     * of the current class in determining how to display the cells in the {@code JTable}
     * that have to do with reminders.
     *
     * @param frame  a reference to the application's main {@code JFrame}, used to set the parent
     *               window of the {@code JOptionPane}s.
     * @param regimens  the {@code List} of {@code Regimen} objects that will become this
     *                  table model's data model
     * @param daoManager  a reference to the application's DAO layer and used to access the database
     * @param columnNames  the column names that will be visible in the {@code JTable}'s column header
     */
    public SupplementTableModel(JFrame frame, List<Regimen> regimens,
                                DAOManager daoManager, String[] columnNames)
    {
        this.frame = frame;
        this.regimens = regimens;
        this.daoManager = daoManager;
        this.columnNames = columnNames;

        this.userPreferences = Preferences.userRoot();
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
     * This method uses the {@code currentRegimenID} to first select the current
     * {@code Regimen} object from the {@code regimens} data model.  It then retrieves
     * the {@code supplements} {@code ArrayList} field from this regimen.  The number
     * of {@code Supplement}s in this list is the number of rows.
     *
     * @return  the number of rows in the data model (the number of {@code Regimen}s
     *          in the {@code ArrayList})
     */
    @Override
    public int getRowCount()
    {
        ArrayList<Supplement> supplements = null;

        for (int i = 0; i < regimens.size(); i++)
        {
            if (regimens.get(i).getRegimenID() == currentRegimenID)
            {
                supplements = regimens.get(i).getSupplements();
            }
        }

        if (supplements != null)
        {
            return supplements.size();
        }
        else
        {
            return 0;
        }
    }


    /**
     * Called by the {@code JTable} to retrieve the value at the specified cell.
     * This implementation first retrieves the user's global e-mail, text message
     * and voice reminder preferences.  When the cell to be retrieved falls under
     * any of these columns, the user's global reminder preference for that reminder
     * type is consulted to determine whether or not to display the check mark for
     * that cell.  In other words, the check mark is only displayed in the {@code JTable}
     * if the user has that reminder turned on for that supplement, <i>and</i> they
     * have reminders of that type globally enabled.  This has the effect that only
     * those reminders that are "active" are displayed as check marks.
     *
     * @param row  the row in the model of the desired cell
     * @param col  the column in the model of the desired cell
     * @return  the value at the specified cell, cast to an {@code Object}
     */
    @Override
    public Object getValueAt(int row, int col)
    {
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);

        Object object = null;

        ArrayList<Supplement> supplements = null;

        for (int i = 0; i < regimens.size(); i++)
        {
            if (regimens.get(i).getRegimenID() == currentRegimenID)
            {
                supplements = regimens.get(i).getSupplements();
            }
        }

        if (supplements.size() > 0)
        {
            switch (col)
            {
            case 0:
                object = supplements.get(row).getSuppID();
                break;
            case 1:
                object = supplements.get(row).getRegimenID();
                break;
            case 2:
                object = supplements.get(row).getSuppName();
                break;
            case 3:
                object = supplements.get(row).getSuppAmount();
                break;
            case 4:
                object = supplements.get(row).getSuppUnits();
                break;
            case 5:
                object = supplements.get(row).getSuppTime();
                break;
            case 6:  // E-mail column
            {
                if (!emailRemindersEnabled)
                {
                    object = false;
                }
                else
                {
                    object = supplements.get(row).getEmailEnabled();
                }

                break;
            }
            case 7:  // Text column
            {
                if (!textRemindersEnabled)
                {
                    object = false;
                }
                else
                {
                    object = supplements.get(row).getTextEnabled();
                }

                break;
            }
            case 8:  // Voice column
            {
                if (!voiceRemindersEnabled)
                {
                    object = false;
                }
                else
                {
                    object = supplements.get(row).getVoiceEnabled();
                }

                break;
            }
            case 9:
                object = supplements.get(row).getSuppNotes();
            }
        }

        return object;
    }


    /**
     * Returns the value of the specified cell, without filtering the values in
     * the reminder columns as in {@code getValueAt()}.
     * <p>
     * This method is called from {@code VitaReminderPanel#editSupplement()} when
     * building the {@code oldSupplement} object that is passed in to the
     * {@code EditSupplementDialog}.  This allows the {@code EditSupplementDialog}
     * to display the actual settings for reminders in its radio buttons, even
     * if those radio buttons are disabled due to that reminder being globally disabled.
     *
     * @param row  the row in the model of the desired cell
     * @param col  the column in the model of the desired cell
     * @return  the value at the specified cell, cast to an {@code Object}
     */
    public Object getValueAtUnfiltered(int row, int col)
    {
        Object object = null;

        ArrayList<Supplement> supplements = null;

        for (int i = 0; i < regimens.size(); i++)
        {
            if (regimens.get(i).getRegimenID() == currentRegimenID)
            {
                supplements = regimens.get(i).getSupplements();
            }
        }

        if (supplements.size() > 0)
        {
            switch (col)
            {
            case 0:
                object = supplements.get(row).getSuppID();
                break;
            case 1:
                object = supplements.get(row).getRegimenID();
                break;
            case 2:
                object = supplements.get(row).getSuppName();
                break;
            case 3:
                object = supplements.get(row).getSuppAmount();
                break;
            case 4:
                object = supplements.get(row).getSuppUnits();
                break;
            case 5:
                object = supplements.get(row).getSuppTime();
                break;
            case 6:
                object = supplements.get(row).getEmailEnabled();
                break;
            case 7:
                object = supplements.get(row).getTextEnabled();
                break;
            case 8:
                object = supplements.get(row).getVoiceEnabled();
                break;
            case 9:
                object = supplements.get(row).getSuppNotes();
            }
        }

        return object;
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
        // Acquire suppID and field name because these
        // are required to update the database.

        ArrayList<Supplement> supplements = null;

        for (int i = 0; i < regimens.size(); i++)
        {
            if (regimens.get(i).getRegimenID() == currentRegimenID)
            {
                supplements = regimens.get(i).getSupplements();
            }
        }

        int suppID = supplements.get(row).getSuppID();

        String field = "";

        switch (col)
        {
        case 2:
            field = "supp_name";
            break;
        case 3:
            field = "supp_amount";
            break;
        case 4:
            field = "supp_units";
            break;
        case 5:
            field = "supp_time";
            break;
        case 6:
            field = "supp_email_enabled";
            break;
        case 7:
            field = "supp_text_enabled";
            break;
        case 8:
            field = "supp_voice_enabled";
            break;
        case 9:
            field = "supp_notes";
        }

        // First, update database
        boolean rowUpdated = daoManager.getSupplementDAO().updateSupplement(suppID, field, value);

        // Second, update model if database operation was succesful

        if (rowUpdated)
        {
            switch (col)
            {
            case 2:
                supplements.get(row).setSuppName((String) value);
                break;
            case 3:
                supplements.get(row).setSuppAmount((double) value);
                break;
            case 4:
                supplements.get(row).setSuppUnits((String) value);
                break;
            case 5:
                supplements.get(row).setSuppTime((java.sql.Time) value);
                break;
            case 6:
                supplements.get(row).setEmailEnabled((boolean) value);
                break;
            case 7:
                supplements.get(row).setTextEnabled((boolean) value);
                break;
            case 8:
                supplements.get(row).setVoiceEnabled((boolean) value);
                break;
            case 9:
                supplements.get(row).setSuppNotes((String) value);
            }

            fireTableCellUpdated(row, col);
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Error updating supplement data.",
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
     * Adds a new row to the supplements database table and table model.  In this
     * implementation, we're adding a new {@code Supplement} object to the
     * current {@code Regimen} in the {@code regimens} {@code List}.  But before
     * the new {@code Supplement} is added to the {@code regimens} model, it is
     * first added to the supplements table in the database.  If ths is successful,
     * this {@code Supplement} is then added to the model.
     * <p>
     * The operations are performed in this order for two reasons.  First, we
     * don't want the data model to be out of sync with the database table.
     * Second, we need a new primary key for the new {@code Supplement} object's
     * {@code suppID} field.
     *
     * @param supplement  the {@code Suplement} object to be added to the database
     *                    and model
     */
    public void addRow(Supplement supplement)
    {
        // First, update database
        Supplement newRowWithPK = daoManager.getSupplementDAO().addSupplement(supplement);

        if (newRowWithPK != null)
        {
            ArrayList<Supplement> supplements = null;

            for (int i = 0; i < regimens.size(); i++)
            {
                if (regimens.get(i).getRegimenID() == currentRegimenID)
                {
                    supplements = regimens.get(i).getSupplements();
                }
            }

            supplements.add(newRowWithPK);

            fireTableRowsInserted(supplements.size()-1, supplements.size()-1);
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Error adding supplement data.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Removes a row from the supplements database table and table model.  It
     * first attempts to remove the row from the database table, and if this
     * is successful, it then removes the row from the model.
     *
     * @param row  the row to be removed from the database table and table model
     */
    public void removeRow(int row)
    {
        ArrayList<Supplement> supplements = null;

        for (int i = 0; i < regimens.size(); i++)
        {
            if (regimens.get(i).getRegimenID() == currentRegimenID)
            {
                supplements = regimens.get(i).getSupplements();
            }
        }

        int suppID = (int) supplements.get(row).getSuppID();

        // First, update database
        boolean rowRemoved = daoManager.getSupplementDAO().deleteSupplement(suppID);


        // If database operation was successful, update data model and inform JTable of update
        if (rowRemoved)
        {
            supplements.remove(row);

            fireTableDataChanged();
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Error deleting supplement data.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Sets the class variable {@code currentRegimenID} to the specified value.
     * This method is called by the {@code regimenTableModel}'s {@code ListSelectionListener}
     * in {@code VitaReminderPanel}.  As a result, this value in the present class
     * is constantly updated to reflect the row in the regimens table that the
     * user has selected.  This value is used in {@code getValueAt()} to return the
     * supplements from the currently selected regimen.  It is in this way that the
     * currently selected regimen's supplements are displayed in the {@code supplementTable}.
     *
     * @param currentRegimenID  the regimenID of the currently selected regimen in the
     *                          {@code regimenTable}
     */
    public void setCurrentRegimenID(int currentRegimenID)
    {
        this.currentRegimenID = currentRegimenID;

        // Notify JTable that model has changed
        fireTableStructureChanged();
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

}  // end class SupplementTableModel
