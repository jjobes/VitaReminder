package com.vitareminder.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.vitareminder.reports.HtmlGenerator;


/**
 * A modal dialog containing general system information.
 */
public class SystemInformationDialog implements PropertyChangeListener
{
    private JDialog dialog;
    private JOptionPane optionPane;
    private JPanel systemInformationPanel;
    private String[] buttonStrings = {"OK"};


    /**
     * The sole constructor.  Creates and displays a new {@code JDialog}.
     * It first creates a {@code JPanel} and then installs this panel in
     * the {@code JOptionPane}.  The {@code JOptionPane} is then set as
     * this dialog's content pane.
     *
     * @param frame  the owner of this dialog
     */
    public SystemInformationDialog(JFrame frame)
    {
        dialog = new JDialog(frame, true);

        createPanel();

        optionPane = new JOptionPane(systemInformationPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
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

        dialog.setTitle("System Information");
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Creates a new {@code JPanel} for the {@code AboutDialog}.  The main
     * content of this panel is an HTML document stored in a {@code String}.
     * This {@code String} is then placed inside a {@code JLabel}, and this
     * {@code JLabel} is then placed inside the content panel.  The majority
     * of this method fills the {@code contextParams} {@code Map}, which is
     * used by Velocity (in {@code HtmlGenerator#getHtmlForSystemInformationDialog()}
     * to fill the template.
     */
    private void createPanel()
    {
        JPanel contentPanel = new JPanel(new GridBagLayout());

        String title = "System Information";
        String operatingSystem = System.getProperty("os.name");
        String operatingSystemVersion = System.getProperty("os.version");
        String operatingSystemString = operatingSystem + " (" + operatingSystemVersion + ")";
        String architecture = System.getProperty("os.arch");
        String javaVendor = System.getProperty("java.vendor");
        String javaVersion = System.getProperty("java.version");

        String[][] systemProperties = {{"Operating System", operatingSystemString},
                                       {"Architecture", architecture},
                                       {"Java Vendor", javaVendor},
                                       {"Java Version", javaVersion}};

        Map<String, Object> contextParams = new HashMap<String, Object>();
        contextParams.put("title", title);
        contextParams.put("systemProperties", systemProperties);

        String contentHtml = HtmlGenerator.getHtmlForSystemInformation(contextParams);
        JLabel contentLabel = new JLabel(contentHtml);
        contentPanel.add(contentLabel, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        systemInformationPanel = new JPanel(new GridBagLayout());
        systemInformationPanel.add(contentPanel, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
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

}  // end class SystemInformationDialog
