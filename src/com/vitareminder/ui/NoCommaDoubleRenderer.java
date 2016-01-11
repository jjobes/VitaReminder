package com.vitareminder.ui;

import java.math.RoundingMode;
import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;


/**
 * A {@code DefaultTableCellRenderer} used to format the double values
 * in the supplement table's amount column.
 */
@SuppressWarnings("serial")
public class NoCommaDoubleRenderer extends DefaultTableCellRenderer
{
    private Number numberValue;
    private NumberFormat numberFormat;


    /**
     * The sole constructor.  Creates a {@code NumberFormat} instance that
     * removes decimal places for whole numbers, sets the maximum fraction
     * digits to 2, removes commas and specifies the {@code RoundingMode.HALF_UP}
     * rounding mode.
     */
    public NoCommaDoubleRenderer()
    {
        super();

        setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(0);  // Don't display decimal places for whole numbers.
        numberFormat.setMaximumFractionDigits(2);  // Set maximum decimal places to 2.
        numberFormat.setGroupingUsed(false);       // Remove commas.
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
    }


    /**
     * Applies the formatting to each cell of the amount column in
     * the supplement table.
     */
    @Override
    public void setValue(Object value)
    {
        if ((value != null) && (value instanceof Number))
        {
            numberValue = (Number) value;
            value = numberFormat.format(numberValue.doubleValue());
        }

        super.setValue(value);  // Set the value to be displayed.
    }

}  // end class NoCommaDoubleRenderer
