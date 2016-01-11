package com.vitareminder.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.apache.log4j.Logger;


/**
 * A non-modal dialog that displays the help_for_dialog.html document.
 * This dialog is only displayed if the current platform does not support
 * the Java Desktop API.  Normally, the help document is displayed in the
 * user's default browser.
 */
public class HelpDialog implements PropertyChangeListener
{
    private JFrame frame;
    private JDialog dialog;
    private JOptionPane optionPane;
    private JPanel helpPanel;
    private String[] buttonStrings = {"OK"};

    private Logger logger = Logger.getLogger(HelpDialog.class);


    /**
     * The sole constructor.  Creates and displays a new {@code JDialog}.
     * It first creates a {@code JPanel} and then installs this panel in
     * the {@code JOptionPane}.  The {@code JOptionPane} is then set as
     * this dialog's content pane.
     *
     * @param frame  the owner of this dialog
     */
    public HelpDialog(JFrame frame)
    {
        this.frame = frame;

        dialog = new JDialog(frame, false);

        createPanel();

        optionPane = new JOptionPane(helpPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
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

        dialog.setTitle("Help");
        dialog.setResizable(false);
        dialog.setPreferredSize(new Dimension(860, 600));
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Creates a new {@code JPanel} called {@code helpPanel} for the {@code HelpDialog}.
     * The main content of this panel is a {@code JScrollPane}.  This scroll pane
     * contains a {@code JEditorPane} with the help_for_dialog.html dialog set to it.
     * The help_for_dialog.html document is used instead of the normal help.html
     * document, because {@code JEditorPane}s only supports HTML 3.2.
     */
    private void createPanel()
    {
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

        File file = new File("res/help/help_for_dialog.html");

        try
        {
            htmlPane.setPage(file.toURI().toURL());
        }
        catch (IOException e)
        {
            logger.error("An error occurred while displaying the help dialog.", e);

            JOptionPane.showMessageDialog(frame,
                    "Sorry, an error occurred while displaying the help dialog.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        htmlPane.setCaretPosition(0);
        htmlPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        scrollPane.setMinimumSize(new Dimension(820, 500));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        helpPanel = new JPanel(new GridBagLayout());
        helpPanel.add(scrollPane, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
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

}  // end class HelpDialog
