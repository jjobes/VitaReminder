package com.vitareminder.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.vitareminder.business.Regimen;
import com.vitareminder.business.Supplement;
import com.vitareminder.dao.DAOManager;
import com.vitareminder.reports.HtmlGenerator;


/**
 * A modal dialog containing general system information.
 */
public class ShowActiveRemindersDialog implements PropertyChangeListener
{
    private JDialog dialog;
    private JOptionPane optionPane;
    private JPanel panel;
    private String[] buttonStrings = {"OK"};

    DAOManager daoManager;


    /**
     * The sole constructor.  Creates and displays a new {@code JDialog}.
     * It first creates a {@code JPanel} and then installs this panel in
     * the {@code JOptionPane}.  The {@code JOptionPane} is then set as
     * this dialog's content pane.
     *
     * @param frame  the owner of this dialog
     */
    public ShowActiveRemindersDialog(JFrame frame, DAOManager daoManager)
    {
        dialog = new JDialog(frame, true);

        this.daoManager = daoManager;

        createPanel();

        optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
        optionPane.setOptions(buttonStrings);
        optionPane.addPropertyChangeListener(this);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we)
            {
                // When the user closes the window, update the JOptionPane's
                // value to fire a propertyChangeEvent.
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        dialog.setTitle("Active Reminders");
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Creates a new {@code JPanel} for the {@code AboutDialog} that contains
     * all of the supplements with active reminders placed in an HTML table.
     */
    private void createPanel()
    {
        List<Regimen> regimens = getRegimensWithReminderSupplements();

        String contentHtml = HtmlGenerator.getHtmlForActiveReminders(regimens);
        JLabel contentLabel = new JLabel(contentHtml);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.add(contentLabel, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        scrollPane.setMinimumSize(new Dimension(300, 250));

        panel = new JPanel(new GridBagLayout());
        panel.add(scrollPane, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
    }


    /**
     * Processes all of the user's regimens and removes all supplements
     * that lack reminders, then returns the resulting regimens.  Any
     * regimens that turn out to have no supplements with reminders are
     * removed from the list.
     */
    private List<Regimen> getRegimensWithReminderSupplements()
    {
        List<Regimen> regimens = daoManager.getRegimenDAO().getRegimens();

        // Remove all supplements that don't have any reminders scheduled
        for (Iterator<Regimen> rIt = regimens.iterator(); rIt.hasNext(); )
        {
            Regimen r = rIt.next();

            for (Iterator<Supplement> sIt = r.getSupplements().iterator(); sIt.hasNext(); )
            {
                Supplement s = sIt.next();

                if (s.getEmailEnabled() == false &&
                    s.getTextEnabled() == false &&
                    s.getVoiceEnabled() == false)
                {
                    sIt.remove();
                }
            }

            // If this regimen has no supplements left, remove it
            if (r.getSupplements().size() == 0)
            {
                rIt.remove();
            }
        }

        return regimens;
    }


    /**
     * Closes this dialog when the user closes the window or
     * presses the "OK" button.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();

        if (dialog.isVisible() && (e.getSource() == optionPane) &&
            (JOptionPane.VALUE_PROPERTY.equals(prop) ||
             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
        {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE)
            {
                return;  // Ignore reset.
            }

            // Reset the value of the JOptionPane to ensure that, when
            // the user presses the same button again, a property change
            // event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            dialog.dispose();
        }
    }

}  // end class ShowActiveRemindersDialog
