package com.vitareminder.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * This dialog is displayed when the user presses the "Verify" button
 * on the {@code ConfigureRemindersDialog}.  It displays a dialog that
 * requests that the user input the 4-digit code that was sent as a text
 * message to the phone number that they just entered.
 */
public class VerifyPhoneDialog implements PropertyChangeListener
{
    private JDialog dialog;

    private JPanel panel;

    private JLabel messageLabel1,
        messageLabel2,
        messageLabel3;

    private JTextField codeTextField;

    String[] buttonStrings = {"OK", "Cancel"};

    private JButton okayButton,
        cancelButton,
        verifyPhoneButton;

    private JOptionPane optionPane;

    private String phoneNumber;
    private String code;

    private boolean codeVerified;


    /**
     * The {@code VerifyPhoneDialog} constructor creates and displays the
     * modal {@code JDialog}.
     *
     * @param parentWindow  the owner of this dialog
     * @param title  the title displayed on the dialog's border
     * @param phoneNumber  the phone number to be verified
     * @param code  the random 4-digit code sent to the phone number
     * @param verifyPhoneButton  a reference to the {@code verifyPhoneButton}
     *                           in the parent dialog
     */
    public VerifyPhoneDialog(Window parentWindow, String title, String phoneNumber,
                             String code, JButton verifyPhoneButton)
    {
        dialog = new JDialog(parentWindow);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setTitle(title);

        this.phoneNumber = phoneNumber;
        this.code = code;
        this.verifyPhoneButton = verifyPhoneButton;

        codeVerified = false;

        createPanel();

        okayButton = new JButton("OK");
        okayButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(okayButton);
            }
        });
        okayButton.setEnabled(false);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(cancelButton);
            }
        });

        optionPane = new JOptionPane(panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                new Object[] {okayButton, cancelButton},
                okayButton);

        // Register an event handler that reacts to optionPane state changes.
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
        dialog.pack();
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setVisible(true);
    }


    /**
     * Creates the main {@code JPanel} for the {@code VerifyPhoneDialog}
     * and fills it with the label, text field and button components.  The
     * components are arranged in the panel using a {@code GridBagLayout}.
     */
    private void createPanel()
    {
        messageLabel1 = new JLabel("A text message with a 4-digit verification code has been sent to:");
        messageLabel2 = new JLabel(phoneNumber);
        messageLabel3 = new JLabel("Please enter the code below:");

        codeTextField = new JTextField(4);
        codeTextField.setHorizontalAlignment(JTextField.CENTER);

        // Limit input to 4 characters
        codeTextField.setDocument(new MaxInputDocument(4));

        // Listen for changes in the text
        codeTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                checkInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                checkInput();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                checkInput();
            }

            public void checkInput()
            {
                if (codeTextField.getText().trim().equals(code))
                {
                    dialog.setTitle("Phone Number Verified!");

                    codeTextField.setEnabled(false);

                    okayButton.setEnabled(true);
                    cancelButton.setEnabled(false);

                    // Set button state in parent dialog to avoid the flicker
                    // that results if it is set when control returns to it.
                    verifyPhoneButton.setEnabled(false);
                    verifyPhoneButton.setText("Verified");

                    codeVerified = true;
                }
            }
        });

        // Make sure regimenNameTextField always gets first focus.
        dialog.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent ce)
            {
                codeTextField.requestFocusInWindow();
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(codeTextField);

        panel = new JPanel(new GridBagLayout());

        panel.add(messageLabel1, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        panel.add(messageLabel2, GBCFactory.getConstraints(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        panel.add(messageLabel3, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        panel.add(inputPanel, GBCFactory.getConstraints(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
    }


    /**
     * A helper function used by the {@code okayButton} and {@code cancelButton}
     * event handlers for these buttons to retrieve a reference to their
     * {@code JOptionPane} and manually set the pane's value whenever they are
     * pressed.
     *
     * @param component  the component (in this case, the {@code JButton}) whose
     *                   optionPane we are requesting
     * @return  a reference to the {@code JOptionPane}
     */
    private JOptionPane getOptionPane(JComponent parent)
    {
        JOptionPane pane = null;

        if (!(parent instanceof JOptionPane))
        {
            pane = getOptionPane((JComponent)parent.getParent());
        }
        else
        {
            pane = (JOptionPane) parent;
        }

        return pane;
    }


    /**
     * Defines the actions to take when the user presses the "OK"
     * or "Cancel" buttons or closes the dialog.  The {@code okayButton}
     * is only enabled once the user enters the correct 4-digit code.
     * Because the {@code DocumentListener} will automatically set
     * {@code codeVerified} to <tt>true</tt> as soon as the correct code
     * is entered and then enable the {@code okayButton}, all we need
     * to do when the user presses this is close the dialog.
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

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            // The okayButton is by default set to false, and only becomes
            // activated when the user types in the correct verification code.
            // So when the user presses either the okayButton or cancelButton,
            // simply close the dialog.
            dialog.dispose();
        }
    }

    /**
     * After this dialog exits, this is called from {@code ConfigureRemindersDialog}
     * to determine whether or not the user entered the correct code.
     *
     * @return  <tt>true</tt> if the user entered the correct 4-digit code,
     * 			<tt>false</tt> otherwise
     */
    public boolean getCodeVerified()
    {
        return codeVerified;
    }


    /**
     * A custom {@code PlainDocument} object that is set to the {@code codeTextField}
     * that limits the number of input characters to {@code max} characters.
     */
    @SuppressWarnings("serial")
    class MaxInputDocument extends PlainDocument
    {
        private int max;

        MaxInputDocument(int max)
        {
            super();

            this.max = max;
        }

        @Override
        public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException
        {
            if (string == null)
            {
                return;
            }

            if ((getLength() + string.length()) <= max)
            {
                super.insertString(offset, string, attributeSet);
            }
        }
    }

}  // end class VerifyPhoneDialog

