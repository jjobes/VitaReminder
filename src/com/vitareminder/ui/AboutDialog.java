package com.vitareminder.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.vitareminder.reports.HtmlGenerator;


/**
 * A modal dialog containing general program information.
 */
public class AboutDialog implements PropertyChangeListener
{
    private JDialog dialog;
    private JOptionPane optionPane;
    private JPanel aboutPanel;
    private String[] buttonStrings = {"OK"};


    /**
     * The sole constructor.  Creates and displays a new {@code JDialog}.
     * It first creates a {@code JPanel} and then installs this panel in
     * the {@code JOptionPane}.  The {@code JOptionPane} is then set as
     * this dialog's content pane.
     *
     * @param frame  the owner of this dialog
     */
    public AboutDialog(JFrame frame)
    {
        dialog = new JDialog(frame, true);

        createPanel();

        optionPane = new JOptionPane(aboutPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
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

        dialog.setTitle("About");
        dialog.setResizable(false);
        dialog.setPreferredSize(new Dimension(450, 400));
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Creates a new {@code JPanel} called {@code aboutPanel} for the {@code AboutDialog}.
     * The main content of this panel is a {@code JScrollPane}.  This scroll pane
     * contains a panel with a simple {@code JLabel}.  This {@code JLabel} contains
     * the HTML document.
     * <p>
     * This HTML document is a {@code String} that is created by first filling
     * the {@code contextParams} {@code Map}.  That {@code Map} is passed to
     * the HtmlGenerator which uses Velocity to generate the {@code String}.
     */
    private void createPanel()
    {
        JPanel headerPanel = new JPanel();
        ImageIcon headerIcon = new ImageIcon(getClass().getResource("resources/images/vitareminder_header.png"));
        JLabel headerLabel = new JLabel(headerIcon);
        headerPanel.add(headerLabel);

        String message1 = "VitaReminder is a Java Swing application written in JavaSE 1.7.";
        String message2 = "It was developed using the following resources and APIs:";
        String[][] technologies = {{"Database", "H2 Database (embedded)", "1.3.174"},
                {"Job Scheduler", "Quartz Scheduler", "2.2.1"},
                {"E-mail API", "JavaMail", "1.4.7"},
                {"Web Telephony API", "Tropo", "0.3"},
                {"Templating Engine", "Apache Velocity", "1.7"},
                {"Java Excel API", "Apache POI", "3.10"},
                {"Logging", "Apache log4j", "1.2.16"},
                {"IDE", "Eclipse", "Kepler"}};
        String programmer = "Jason Jobes";
        Calendar now = Calendar.getInstance();
        String year = Integer.toString(now.get(Calendar.YEAR));

        Map<String, Object> contextParams = new HashMap<String, Object>();
        contextParams.put("message1", message1);
        contextParams.put("message2", message2);
        contextParams.put("technologies", technologies);
        contextParams.put("programmer", programmer);
        contextParams.put("year", year);

        String contentHtml = HtmlGenerator.getHtmlForAboutDialog(contextParams);

        JEditorPane htmlPane= new JEditorPane();

        // Set the background color of the JEditorPane.  When we set the editor
        // pane content to the HTML document below, this document does not fill
        // the entire viewport of the JEditorPane, and this results in a white
        // border around the document.  To remedy this, we'll set the background
        // color of the editor pane to match the background color of the inner
        // HTML document.  This appears to only be an issue with Nimbus.
        String currentLookAndFeel = UIManager.getLookAndFeel().toString();
        if (currentLookAndFeel.toLowerCase().contains("nimbus"))
        {
            Color backgroundColor = new Color(214, 217, 223);
            UIDefaults uiDefaults = new UIDefaults();
            uiDefaults.put("EditorPane[Enabled].backgroundPainter", backgroundColor);
            htmlPane.putClientProperty("Nimbus.Overrides", uiDefaults);
            htmlPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
            htmlPane.setBackground(backgroundColor);
        }

        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setText(contentHtml);
        htmlPane.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        scrollPane.setMinimumSize(new Dimension(400, 250));

        aboutPanel = new JPanel(new GridBagLayout());
        aboutPanel.add(headerLabel, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        aboutPanel.add(scrollPane, GBCFactory.getConstraints(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
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

}  // end class AboutDialog
