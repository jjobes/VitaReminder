package com.vitareminder.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;


/**
 * A convenience class that aids in building a {@code GridBagLayout}.
 * Contains methods that return a {@code GridBagConstraints} object for a
 * particular Swing component.
 */
public class GBCFactory
{
    /**
     * Builds and returns a {@code GridBagConstraints} object with the specified
     * properties.
     *
     * @param gridx  the cell that the left edge of the component occupies
     * @param gridy  the cell that the top edge of the component occupies
     * @param gridwidth  the number of horizontal cells that the component occupies
     * @param gridheight  the number of vertical cells that the component occupies
     * @param anchor  the alignment of the component within the cell
     * @param fill  specifies what to do with the extra space inside the cell
     * @return the {@code GridBagConstraints} object based on the input fields
     */
    public static GridBagConstraints getConstraints(int gridx, int gridy,
                                                    int gridwidth, int gridheight,
                                                    int anchor, int fill)
    {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.anchor = anchor;
        gbc.fill = fill;

        return gbc;
    }


    /**
     * Builds and returns a {@code GridBagConstraints} object with the specified
     * properties.  Offers the ability to specify the {@code weightx} and
     * {@code weighty}.
     *
     * @param gridx  the cell that the left edge of the component occupies
     * @param gridy  the cell that the top edge of the component occupies
     * @param gridwidth  the number of horizontal cells that the component occupies
     * @param gridheight  the number of vertical cells that the component occupies
     * @param weightx  specifies how extra horizontal space should be distributed
     * @param weighty  specifies how extra vertical space should be distributed
     * @param anchor  the alignment of the component within the cell
     * @param fill  specifies what to do with the extra space inside the cell
     * @return the {@code GridBagConstraints} object based on the input fields
     */
    public static GridBagConstraints getWeightedConstraints(int gridx, int gridy,
                                                            int gridwidth, int gridheight,
                                                            double weightx, double weighty,
                                                            int anchor, int fill)
    {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = anchor;
        gbc.fill = fill;

        return gbc;
    }

}  // end class GBCFactory
