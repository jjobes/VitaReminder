package com.vitareminder.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;

import com.vitareminder.reminders.HtmlEmail;
import com.vitareminder.reminders.ReminderManager;
import com.vitareminder.reminders.TextMessage;
import com.vitareminder.reports.HtmlGenerator;


/**
 * Allows the user to verify their e-mail address or phone number for reminders,
 * as well as globally enabling or disabling each type of reminder.
 */
public class ConfigureRemindersDialog implements PropertyChangeListener
{
    private String[] buttonStrings = {"Save", "Cancel"};

    private JPanel mainPanel,
    emailPanel,
    emailInputPanel,
    emptyPanel1,
    emptyPanel2,
    phonePanel,
    phoneInputPanel;

    private JLabel titleLabel,
    emailAddressLabel,
    phoneNumberLabel;

    private JButton verifyEmailButton,
    verifyPhoneButton;

    private JTextField emailTextField;

    private MaskFormatter maskFormatter;
    private JFormattedTextField phoneFormattedTextField;

    private JCheckBox emailCheckBox, textCheckBox, voiceCheckBox;

    private JOptionPane optionPane;

    private String oldEmail;
    private String oldPhone;
    private String currentEmail = "";
    private String currentPhone = "";
    private String newlyVerifiedEmail = "";
    private String oldVerifiedEmail = "";

    private String newlyVerifiedPhone = "";
    private String oldVerifiedPhone = "";

    private boolean oldEmailEnabled;
    private boolean oldTextEnabled;
    private boolean oldVoiceEnabled;
    private boolean currentEmailEnabled = false;
    private boolean currentTextEnabled = false;
    private boolean currentVoiceEnabled = false;

    private boolean emailValidated = false;
    private boolean phoneValidated = false;

    private boolean oldEmailVerified;
    private boolean oldPhoneVerified;
    private boolean currentEmailVerified = false;
    private boolean currentPhoneVerified = false;

    private JDialog dialog;

    private VitaReminderPanel panel;

    private ReminderManager reminderManager;

    private Preferences userPreferences;

    private Logger logger = Logger.getLogger(ConfigureRemindersDialog.class);


    /**
     * The sole constructor.  Creates a modal dialog.  It stores the reminder data
     * and preferences into local variables to represent the old values, which enables
     * us to detect any changes to these values when the user exits the dialog.
     *
     * @param frame  the owner of this dialog
     * @param panel  the frame's panel, used to update the supplement table model
     * @param reminderManager  used to update any reminders if necessary
     */
    public ConfigureRemindersDialog(JFrame frame, VitaReminderPanel panel,
                                    ReminderManager reminderManager)
    {
        this.panel = panel;
        this.reminderManager = reminderManager;

        dialog = new JDialog(frame, true);

        userPreferences = Preferences.userRoot();
        String emailAddress = userPreferences.get("EMAIL_ADDRESS", "");
        String phoneNumber = userPreferences.get("PHONE_NUMBER", "");
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);
        boolean emailVerified = userPreferences.getBoolean("EMAIL_VERIFIED", false);
        boolean phoneVerified = userPreferences.getBoolean("PHONE_VERIFIED", false);

        oldEmail = emailAddress;
        if (emailVerified)
        {
            oldVerifiedEmail = emailAddress;
        }

        oldPhone = phoneNumber;
        if (phoneVerified)
        {
            oldVerifiedPhone = phoneNumber;
        }

        oldEmailEnabled = emailRemindersEnabled;
        oldTextEnabled = textRemindersEnabled;
        oldVoiceEnabled = voiceRemindersEnabled;
        oldEmailVerified = emailVerified;
        oldPhoneVerified = phoneVerified;

        // Set the current values.  When the user closes the dialog, we'll compare
        // the current values to the old ones to detect any changes that the user
        // has made.
        currentEmail = oldEmail;
        currentPhone = oldPhone;
        currentEmailEnabled = oldEmailEnabled;
        currentTextEnabled = oldTextEnabled;
        currentVoiceEnabled = oldVoiceEnabled;
        currentEmailVerified = oldEmailVerified;
        currentPhoneVerified = oldPhoneVerified;

        mainPanel = createPanel();

        optionPane = new JOptionPane(mainPanel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null);
        optionPane.setOptions(buttonStrings);
        optionPane.addPropertyChangeListener(this);

        dialog.setContentPane(optionPane);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we)
            {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        ImageIcon configureIcon = new ImageIcon(getClass().getResource("resources/icons/configure_icon_16x16.png"));
        dialog.setIconImage(configureIcon.getImage());

        dialog.setTitle("Configure Reminders");
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Creates the main panel for the {@code ConfigureRemindersDialog}.
     * Uses the old reminder data and preferences to pre-fill the dialog
     * components.
     *
     * @return the {@code JPanel} that was just created
     */
    @SuppressWarnings("serial")
    private JPanel createPanel()
    {
        mainPanel = new JPanel(new GridBagLayout());

        titleLabel = new JLabel("Configure Reminders");
        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        titleLabel.setFont(boldFont);

        emptyPanel1 = new JPanel();

        emailCheckBox = new JCheckBox("Enable e-mail reminders");
        emailCheckBox.setSelected(oldEmailEnabled);
        emailCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e)
            {
                if (currentEmailEnabled)
                {
                    currentEmailEnabled = false;

                    emailTextField.setEnabled(false);

                    verifyEmailButton.setEnabled(false);
                }
                else  // They're enabling e-mails
                {
                    currentEmailEnabled = true;

                    emailTextField.setEnabled(true);
                    emailTextField.requestFocus();

                    if (currentEmailVerified)
                    {
                        verifyEmailButton.setEnabled(false);
                        verifyEmailButton.setText("Verified");
                    }
                    else
                    {
                        verifyEmailButton.setEnabled(true);
                        verifyEmailButton.setText("Verify");
                    }
                }
            }
        });

        emailAddressLabel = new JLabel("E-mail Address:");

        emailTextField = new JTextField(15);
        if (!oldEmail.isEmpty())  // Always display old e-mail if present.
        {
            emailTextField.setText(oldEmail);
        }

        emailTextField.setEnabled(oldEmailEnabled);

        // Make sure regimenNameTextField always gets first focus.
        dialog.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent ce)
            {
                emailTextField.requestFocusInWindow();
            }
        });

        // Listen for changes in the e-mail text so we can update the verify button
        // in real time.
        emailTextField.getDocument().addDocumentListener(new DocumentListener() {

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

            // This checks the text the user is typing into emailTextBox in real time,
            // and updates the verifyEmailButton based on whether or not the current
            // text is a verified e-mail address.
            public void checkInput()
            {
                String emailText = emailTextField.getText().trim();

                // For a newly-verified e-mail that hasn't yet been saved.
                // In other words, the e-mail was just verified, but the user
                // hasn't pressed the Save button.

                // Always check if the text equals a newly verified e-mail first.
                if (!newlyVerifiedEmail.isEmpty())
                {
                    if (emailText.equalsIgnoreCase(newlyVerifiedEmail))
                    {
                        currentEmailVerified = true;
                        verifyEmailButton.setEnabled(false);
                        verifyEmailButton.setText("Verified");
                    }
                    else
                    {
                        currentEmailVerified = false;
                        verifyEmailButton.setEnabled(true);
                        verifyEmailButton.setText("Verify");
                    }
                }  // If there is no newly verified e-mail, the user may already have a verified e-mail stored,
                // and if he does, we need to compare the live text to that.
                else if (!oldVerifiedEmail.isEmpty())  // The user already has a verified e-mail address.
                {
                    if (emailText.equalsIgnoreCase(oldVerifiedEmail))
                    {
                        currentEmailVerified = true;
                        verifyEmailButton.setEnabled(false);
                        verifyEmailButton.setText("Verified");
                    }
                    else
                    {
                        currentEmailVerified = false;
                        verifyEmailButton.setEnabled(true);
                        verifyEmailButton.setText("Verify");
                    }
                }
            }
        });

        verifyEmailButton = new JButton("Verify");
        verifyEmailButton.addActionListener(new VerifyEmailButtonListener());

        if (oldEmailEnabled)
        {
            if (oldEmailVerified)
            {
                verifyEmailButton.setEnabled(false);
                verifyEmailButton.setText("Verified");
            }
            else
            {
                verifyEmailButton.setEnabled(true);
                verifyEmailButton.setText("Verify");
            }
        }
        else
        {
            verifyEmailButton.setEnabled(false);

            if (oldEmailVerified)
            {
                verifyEmailButton.setText("Verified");
            }
            else
            {
                verifyEmailButton.setText("Verify");
            }
        }

        emailInputPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        emailInputPanel.setBorder(BorderFactory.createEmptyBorder(0, 17, 0, 0));  // Indent the label and text field
        emailInputPanel.add(emailAddressLabel);
        emailInputPanel.add(emailTextField);

        emailPanel = new JPanel(new GridBagLayout());
        Border emailBorder = BorderFactory.createEtchedBorder();
        emailBorder = BorderFactory.createTitledBorder(emailBorder, "E-mail");
        emailPanel.setBorder(emailBorder);
        emailPanel.add(emailCheckBox, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));
        emailPanel.add(emailInputPanel, GBCFactory.getConstraints(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));
        emailPanel.add(verifyEmailButton, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        emptyPanel2 = new JPanel();

        textCheckBox = new JCheckBox("Enable text-message reminders");
        textCheckBox.setSelected(oldTextEnabled);
        textCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (currentTextEnabled)  // They're disabling text messages.
                {
                    currentTextEnabled = false;

                    if (!currentVoiceEnabled)
                    {
                        phoneFormattedTextField.setEnabled(false);
                        verifyPhoneButton.setEnabled(false);
                    }
                    else
                    {
                        phoneFormattedTextField.requestFocus();
                    }
                }
                else  // They're enabling text messages.
                {
                    currentTextEnabled = true;

                    if (!currentVoiceEnabled)
                    {
                        phoneFormattedTextField.setEnabled(true);
                        phoneFormattedTextField.requestFocus();

                        if (currentPhoneVerified)
                        {
                            verifyPhoneButton.setEnabled(false);
                            verifyPhoneButton.setText("Verified");
                        }
                        else
                        {
                            verifyPhoneButton.setEnabled(true);
                            verifyPhoneButton.setText("Verify");
                            phoneFormattedTextField.requestFocus();
                        }
                    }
                    else
                    {
                        phoneFormattedTextField.requestFocus();
                    }
                }
            }
        });

        voiceCheckBox = new JCheckBox("Enable voice reminders");
        voiceCheckBox.setSelected(oldVoiceEnabled);
        voiceCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (currentVoiceEnabled)
                {
                    currentVoiceEnabled = false;

                    if (!currentTextEnabled)
                    {
                        phoneFormattedTextField.setEnabled(false);

                        verifyPhoneButton.setEnabled(false);
                    }
                    else
                    {
                        phoneFormattedTextField.requestFocus();
                    }
                }
                else  // They're enabling voice.
                {
                    currentVoiceEnabled = true;

                    if (!currentTextEnabled)
                    {
                        phoneFormattedTextField.setEnabled(true);

                        if (currentPhoneVerified)
                        {
                            verifyPhoneButton.setEnabled(false);
                            verifyPhoneButton.setText("Verified");
                        }
                        else
                        {
                            verifyPhoneButton.setEnabled(true);
                            verifyPhoneButton.setText("Verify");

                            phoneFormattedTextField.requestFocus();
                        }
                    }
                    else
                    {
                        phoneFormattedTextField.requestFocus();
                    }
                }
            }
        });

        phoneNumberLabel = new JLabel("Phone Number:");

        try
        {
            maskFormatter = new MaskFormatter("(###) ###-####");
            maskFormatter.setPlaceholderCharacter('_');
            phoneFormattedTextField = new JFormattedTextField(maskFormatter);
            phoneFormattedTextField.setMargin(new Insets(0, 3, 0, 5));
            phoneFormattedTextField.setFocusLostBehavior(JFormattedTextField.COMMIT);

            if (!oldPhone.isEmpty()) // Always display old phone if present.
            {
                phoneFormattedTextField.setText(oldPhone);
            }

            if (oldTextEnabled || oldVoiceEnabled)
            {
                phoneFormattedTextField.setEnabled(true);
            }
            else
            {
                phoneFormattedTextField.setEnabled(false);
            }
        }
        catch (ParseException pe)
        {
            logger.warn("A parse exception has been caught.", pe);
            JOptionPane.showMessageDialog(null,
                    "Sorry, an error has occurred while processing this dialog.",
                    "Dialog Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Listen for changes in the phone text.
        phoneFormattedTextField.getDocument().addDocumentListener(new DocumentListener() {

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

            // Set verifyPhoneButton state based on whether or not the current text is a verified phone number.
            // This checks the text the user is typing into phoneFormattedTextField in real time, and
            // updates the verifyPhoneButton button based on whether or not the current text is a verified
            // phone number.
            public void checkInput()
            {
                String phoneText = phoneFormattedTextField.getText().trim();

                // For a newly-verified phone that hasn't yet been written to userData.
                // In other words, the phone was just verified, but user hasn't pressed Save button.

                // Always check if the text equals a newly verified phone first.
                if (!newlyVerifiedPhone.isEmpty())
                {
                    if (phoneText.equalsIgnoreCase(newlyVerifiedPhone))
                    {
                        currentPhoneVerified = true;
                        verifyPhoneButton.setEnabled(false);
                        verifyPhoneButton.setText("Verified");
                    }
                    else
                    {
                        currentPhoneVerified = false;
                        verifyPhoneButton.setEnabled(true);
                        verifyPhoneButton.setText("Verify");
                    }
                }
                // If there is no newly verified phone, the user may already have a verified phone stored,
                // and if he does, we need to compare the live text to that.
                else if (!oldVerifiedPhone.isEmpty())  // The user already has a verified phone.
                {
                    if (phoneText.equalsIgnoreCase(oldVerifiedPhone))
                    {
                        currentPhoneVerified = true;
                        verifyPhoneButton.setEnabled(false);
                        verifyPhoneButton.setText("Verified");
                    }
                    else
                    {
                        currentPhoneVerified = false;
                        verifyPhoneButton.setEnabled(true);
                        verifyPhoneButton.setText("Verify");
                    }
                }
            }
        });

        verifyPhoneButton = new JButton("Verify");
        verifyPhoneButton.addActionListener(new VerifyPhoneButtonListener());

        if (oldTextEnabled || oldVoiceEnabled)
        {
            if (oldPhoneVerified)
            {
                verifyPhoneButton.setEnabled(false);
                verifyPhoneButton.setText("Verified");
            }
            else
            {
                verifyPhoneButton.setEnabled(true);
                verifyPhoneButton.setText("Verify");
            }
        }
        else
        {
            verifyPhoneButton.setEnabled(false);

            if (oldPhoneVerified)
            {
                verifyPhoneButton.setText("Verified");
            }
            else
            {
                verifyPhoneButton.setText("Verify");
            }
        }

        // Ensure that the phoneInputPanel is the same size as the emailInputPanel.
        phoneInputPanel = new JPanel(new FlowLayout(FlowLayout.LEADING)) {

            Dimension dim = new Dimension(emailInputPanel.getPreferredSize());

            @Override
            public Dimension getPreferredSize()
            {
                return dim;
            }
        };
        phoneInputPanel.setBorder(BorderFactory.createEmptyBorder(0, 17, 0, 0));  // Indent the label and textfield.
        phoneInputPanel.add(phoneNumberLabel);
        phoneInputPanel.add(phoneFormattedTextField);

        phonePanel = new JPanel(new GridBagLayout());
        Border phoneBorder = BorderFactory.createEtchedBorder();
        phoneBorder = BorderFactory.createTitledBorder(phoneBorder, "Text & Voice");
        phonePanel.setBorder(phoneBorder);
        phonePanel.add(textCheckBox, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));
        phonePanel.add(voiceCheckBox, GBCFactory.getConstraints(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));
        phonePanel.add(phoneInputPanel, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE));
        phonePanel.add(verifyPhoneButton, GBCFactory.getConstraints(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        mainPanel.add(titleLabel, GBCFactory.getConstraints(0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        mainPanel.add(emptyPanel1, GBCFactory.getConstraints(0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        mainPanel.add(emailPanel, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        mainPanel.add(emptyPanel2, GBCFactory.getConstraints(0, 3, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        mainPanel.add(phonePanel, GBCFactory.getConstraints(0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));

        return mainPanel;
    }


    /**
     * Specifies the actions to take when the use presses "OK" or "Cancel", or
     * closes the window.
     * <p>
     * If the user presses "Save", the e-mail and phone number are validated.
     * If they are, it checks to see if any changes have been made, and updates
     * the current reminders if necessary.
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
                return;  // Ignore reset
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (value.equals(buttonStrings[0]))  // If the user pressed "Save"
            {
                // Get currently entered values
                currentEmail = emailTextField.getText().trim();
                currentPhone = phoneFormattedTextField.getText().trim();

                currentEmailEnabled = emailCheckBox.isSelected();
                currentTextEnabled = textCheckBox.isSelected();
                currentVoiceEnabled = voiceCheckBox.isSelected();

                emailValidated = false;
                phoneValidated = false;

                // If certain types of reminders aren't enabled, simply count
                // them as validated.
                if (!currentEmailEnabled)
                {
                    emailValidated = true;
                }
                if (!currentTextEnabled && !currentVoiceEnabled)
                {
                    phoneValidated = true;
                }

                // Validate email
                if (currentEmailEnabled)
                {
                    emailValidated = validateEmail(currentEmail);
                }

                if (emailValidated)
                {
                    if (currentTextEnabled || currentVoiceEnabled)
                    {
                        phoneValidated = validatePhone();
                    }
                }

                if (emailValidated &&
                    phoneValidated)
                {
                    // All input validated, now see if any changes have been made.
                    if (changesMade())
                    {
                        userPreferences.put("EMAIL_ADDRESS", currentEmail);
                        userPreferences.put("PHONE_NUMBER", currentPhone);
                        userPreferences.putBoolean("EMAIL_REMINDERS_ENABLED", currentEmailEnabled);
                        userPreferences.putBoolean("TEXT_REMINDERS_ENABLED", currentTextEnabled);
                        userPreferences.putBoolean("VOICE_REMINDERS_ENABLED", currentVoiceEnabled);
                        userPreferences.putBoolean("EMAIL_VERIFIED", currentEmailVerified);
                        userPreferences.putBoolean("PHONE_VERIFIED", currentPhoneVerified);

                        // We need to update active reminders if necessary:
                        boolean emailChanged = currentEmail != oldEmail;
                        boolean phoneChanged = currentPhone != oldPhone;
                        boolean emailRemindersChanged = currentEmailEnabled != oldEmailEnabled;
                        boolean textRemindersChanged = currentTextEnabled != oldTextEnabled;
                        boolean voiceRemindersChanged = currentVoiceEnabled != oldVoiceEnabled;

                        if (emailRemindersChanged)
                        {
                            panel.fireSupplementTableModelUpdates();  // Tell supplementTableModel to update its view.

                            if (!currentEmailEnabled)  // If e-mail reminders were disabled
                            {
                                reminderManager.unloadActiveReminders("email");  // Remove reminders from memory
                            }
                            else  // E-mail reminders were enabled
                            {
                                if (currentEmailVerified)
                                {
                                    reminderManager.loadAllReminders("email");  // Load reminders from db
                                }
                            }
                        }
                        else  // Reminders didn't change
                        {
                            if (currentEmailEnabled &&
                                emailChanged)  // If they changed e-mail address, and they're still enabled
                            {                  // (this implies they were enabled before w/ diff. e-mail address)

                                if (currentEmailVerified)
                                {
                                    reminderManager.unloadActiveReminders("email");
                                    reminderManager.loadAllReminders("email");
                                }
                            }
                        }

                        if (textRemindersChanged)
                        {
                            panel.fireSupplementTableModelUpdates();  // Tell supplementTableModel to update its view.

                            if (!currentTextEnabled)  // If text reminders were disabled
                            {
                                reminderManager.unloadActiveReminders("text");  // Remove reminders from memory
                            }
                            else  // Text reminders were enabled
                            {
                                if (currentPhoneVerified)
                                {
                                    reminderManager.loadAllReminders("text");  // Load reminders from db
                                }
                            }
                        }
                        else  // Reminders didn't change
                        {
                            if (phoneChanged && currentTextEnabled)    // If they changed phone, and they're still enabled
                            {                                          // (this implies they were enabled before w/ diff. phone)
                                if (currentPhoneVerified)
                                {
                                    reminderManager.unloadActiveReminders("text");
                                    reminderManager.loadAllReminders("text");
                                }
                            }
                        }

                        if (voiceRemindersChanged)
                        {
                            panel.fireSupplementTableModelUpdates();  // Tell supplementTableModel to update its view.

                            if (!currentVoiceEnabled)  // If voice reminders were disabled
                            {
                                reminderManager.unloadActiveReminders("voice");  // Remove reminders from memory
                            }
                            else  // Voice reminders were enabled
                            {
                                if (currentPhoneVerified)
                                {
                                    reminderManager.loadAllReminders("voice");  // Load reminders from db
                                }
                            }
                        }
                        else  // Reminders didn't change
                        {
                            if (phoneChanged &&
                                currentVoiceEnabled)           // If they changed phone, and they're still enabled
                            {                                  // (this implies they were enabled before w/ diff. phone)
                                if (currentPhoneVerified)
                                {
                                    reminderManager.unloadActiveReminders("voice");
                                    reminderManager.loadAllReminders("voice");
                                }
                            }
                        }

                        dialog.dispose();
                    }
                    else  // No changes made
                    {
                        JOptionPane.showMessageDialog(dialog,
                                "Please make a change or press Cancel.",
                                "No Changes Made",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
            else  // The user closed the dialog or clicked "Cancel"
            {
                dialog.dispose();
            }
        }
    }


    /**
     * Checks to see if the user-input e-mail address is of the form:
     * "user@host.domain".
     *
     * @param email  the user-input e-mail address to be validated
     * @return  <tt>true</tt> if the e-mail address is valid, <tt>false</tt> otherwise
     */
    public boolean validateEmail(String email)
    {
        boolean isValid = true;

        try
        {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        }
        catch (AddressException ae)
        {
            JOptionPane.showMessageDialog(dialog,
                    "Please enter a valid e-mail address.",
                    "Invalid Data",
                    JOptionPane.ERROR_MESSAGE);
            emailTextField.requestFocusInWindow();
            isValid = false;
        }

        return isValid;
    }


    /**
     * Checks to see if the user-input phone number is of the form: "(###) ###-####".
     * This format {@code String} was passed in to the {@code MaskFormatter} that was
     * used to create the {@code JFormattedTextField} that accepts the phone number.
     *
     * @return  <tt>true</tt> if the phone number is of the form "(###) ###-####", <tt>false</tt> otherwise
     */
    public boolean validatePhone()
    {
        try
        {
            phoneFormattedTextField.commitEdit();  // Try to commit text

            return true;
        }
        catch (ParseException pe)
        {
            JOptionPane.showMessageDialog(dialog,
                    "Please enter a valid phone number.",
                    "Invalid Phone Number",
                    JOptionPane.ERROR_MESSAGE);
            phoneFormattedTextField.requestFocus();

            return false;
        }
    }


    /**
     * Checks to see if any changes have been made to the reminder preferences.
     *
     * @return  <tt>true</tt> if any changes have been made, <tt>false</tt> otherwise
     */
    private boolean changesMade()
    {
        // If none of the fields have changed
        if (currentEmail.equals(oldEmail) &&
            currentPhone.equals(oldPhone) &&
            oldEmailEnabled == currentEmailEnabled &&
            oldTextEnabled == currentTextEnabled &&
            oldVoiceEnabled == currentVoiceEnabled &&
            oldEmailVerified == currentEmailVerified &&
            oldPhoneVerified == currentPhoneVerified)
        {
            return false;
        }
        else  // Changes have been made
        {
            return true;
        }
    }


    /**
     * An inner class {@code ActionListener} for the {@code verifyEmailButton}.
     * It first validates the e-mail address, and if it is valid, sends a random
     * 4-digit code to that e-mail address.  The user is then prompted to enter
     * this code.
     */
    class VerifyEmailButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            // Get currently entered value
            currentEmail = emailTextField.getText().trim();

            boolean isEmailValid = validateEmail(currentEmail);

            if (isEmailValid)
            {
                // The e-mail address has a valid form, now we need to verify it
                // by sending a verification code to it

                // Generate random 4-digit code
                Random r = new Random();
                int code = r.nextInt(9999);
                String strCode = String.format("%04d", code);

                // Create an HtmlEmail object and send it to the address that was entered
                String subject = "VitaReminder E-mail Verification";
                String message = "Your verification code is: " + strCode;
                String body = HtmlGenerator.getHtmlForEmail(message);

                HtmlEmail verificationEmail = new HtmlEmail("VitaReminderApp",
                        "RzQ572w$?",
                        currentEmail,
                        subject,
                        body);

                verificationEmail.send();  // Have the EmailProgressDialog send this

                Window parentWindow = SwingUtilities.windowForComponent(verifyEmailButton);

                VerifyEmailDialog verifyEmailDialog = new VerifyEmailDialog(parentWindow,
                        "Verify your e-mail address",
                        currentEmail,
                        strCode,
                        verifyEmailButton);

                if (verifyEmailDialog.getCodeVerified())
                {
                    // Verification was successful
                    newlyVerifiedEmail = currentEmail;
                    currentEmailVerified = true;
                }
            }
        }
    }


    /**
     * An inner class {@code ActionListener} for the {@code verifyPhoneButton}.
     * It first validates the phone number, and if it is valid, sends a random
     * 4-digit code in a text message to that number.  The user is then prompted
     * to enter this code.
     */
    class VerifyPhoneButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            // Get currently entered value
            currentPhone = phoneFormattedTextField.getText().trim();

            boolean isPhoneValid = validatePhone();

            if (isPhoneValid)
            {
                // Phone valid, now we need to verify it

                // Generate random 4-digit code
                Random r = new Random();
                int code = r.nextInt(9999);
                String strCode = String.format("%04d", code);

                // Create a TextMessage object and send it to the phone number that was entered
                String message = "Your verification code is: " + strCode;

                String formattedPhoneNumber = currentPhone;
                formattedPhoneNumber = formattedPhoneNumber.replace("(", "");
                formattedPhoneNumber = formattedPhoneNumber.replace(")", "");
                formattedPhoneNumber = formattedPhoneNumber.replace("-", "");
                formattedPhoneNumber = formattedPhoneNumber.replace(" ", "");
                formattedPhoneNumber = "+1" + formattedPhoneNumber;

                TextMessage verificationTextMessage = new TextMessage(formattedPhoneNumber, message);
                verificationTextMessage.send();

                Window parentWindow = SwingUtilities.windowForComponent(verifyPhoneButton);

                VerifyPhoneDialog verifyPhoneDialog = new VerifyPhoneDialog(parentWindow,
                        "Verify your phone number",
                        currentPhone,
                        strCode,
                        verifyPhoneButton);

                if (verifyPhoneDialog.getCodeVerified())
                {
                    // Verification was successful
                    newlyVerifiedPhone = currentPhone;
                    currentPhoneVerified = true;
                }
            }
        }
    }

}  // end class ConfigureRemindersDialog
