package com.vitareminder.ui;

import java.text.SimpleDateFormat;

import javax.swing.table.DefaultTableCellRenderer;


/**
 * A {@code DefaultTableCellRenderer} used to properly format the values
 * in the supplement table's time column.  This displays the time in the
 * standard 12-hour format, e.g., "12:00 AM".
 */
@SuppressWarnings("serial")
public class TimeFormatRenderer extends DefaultTableCellRenderer
{
    private SimpleDateFormat simpleDateFormat;


    /**
     * The constructor for the {@code TimeFormatRenderer} class.  Instantiates
     * the class variable {@code simpleDateFormat}.
     */
    public TimeFormatRenderer()
    {
        super();

        simpleDateFormat = new SimpleDateFormat("h:mm a");
    }


    /**
     * Applies the formatting to each cell of the supplement table's
     * time column.
     */
    @Override
    public void setValue(Object value)
    {
        if (value instanceof java.sql.Time)
        {
            value = simpleDateFormat.format(value);
        }

        super.setValue(value);
    }

}  // end class TimeFormatRenderer

