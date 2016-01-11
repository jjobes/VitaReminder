package com.vitareminder.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.vitareminder.business.Supplement;


/**
 * A modal dialog that allows the user to edit the currently selected supplement
 * in the supplement table.
 */
public class EditSupplementDialog extends SupplementDialog implements ActionListener, PropertyChangeListener
{
    String[] buttonStrings = {"Apply Changes", "Cancel"};

    private int suppHours;
    private int suppMinutes;
    private String suppAMPM;

    private boolean changesMade = false;

    private Supplement oldSupplement;

    private String oldSuppName;
    private double oldSuppAmount;
    private String oldSuppUnits;
    private int oldSuppHours;
    private int oldSuppMinutes;
    private String oldSuppAMPM;
    private boolean oldEmailEnabled;
    private boolean oldTextEnabled;
    private boolean oldVoiceEnabled;
    private String oldSuppNotes;


    /**
     * The constructor for the {@code EditSupplementDialog}.
     * Calls the superclass {@code SupplementDialog} to build the main dialog,
     * then modifies it by adding a custom title and window icon.  The user's
     * reminder preferences are retrieved and used to set the initial state
     * of the reminder radio buttons.  The old supplement values are also stored
     * in variables, which allows us to determine if the user has made any
     * actual changes.
     *
     * @param frame  the owner of this dialog
     * @param oldSupplement  contains the properties of the supplement prior to the user
     *                       opening this dialog
     */
    public EditSupplementDialog(JFrame frame, Supplement oldSupplement)
    {
        super(frame);

        this.oldSupplement = oldSupplement;

        suppName = oldSupplement.getSuppName();

        emailEnabled = oldSupplement.getEmailEnabled();
        textEnabled = oldSupplement.getTextEnabled();
        voiceEnabled = oldSupplement.getVoiceEnabled();

        titleLabel.setText("Edit Supplement \"" + suppName + "\"");

        Preferences userPreferences = Preferences.userRoot();
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);
        boolean emailVerified = userPreferences.getBoolean("EMAIL_VERIFIED", false);
        boolean phoneVerified = userPreferences.getBoolean("PHONE_VERIFIED", false);

        // Enable or disable the reminder radiobuttons based on the user preferences.
        emailOnRadioButton.setEnabled(emailRemindersEnabled && emailVerified);
        emailOffRadioButton.setEnabled(emailRemindersEnabled && emailVerified);
        textOnRadioButton.setEnabled(textRemindersEnabled && phoneVerified);
        textOffRadioButton.setEnabled(textRemindersEnabled && phoneVerified);
        voiceOnRadioButton.setEnabled(voiceRemindersEnabled && phoneVerified);
        voiceOffRadioButton.setEnabled(voiceRemindersEnabled && phoneVerified);

        suppNameTextField.addActionListener(this);
        suppAmountTextField.addActionListener(this);

        optionPane.setOptions(buttonStrings);

        optionPane.addPropertyChangeListener(this);

        // The following values are used for comparisons below
        // to see if any changes have been made.
        this.oldSuppName = oldSupplement.getSuppName();
        this.oldSuppAmount = oldSupplement.getSuppAmount();
        this.oldSuppUnits = oldSupplement.getSuppUnits();
        this.oldSuppHours = oldSupplement.getScheduledHour();
        this.oldSuppMinutes = oldSupplement.getScheduledMinute();
        this.oldSuppAMPM = oldSupplement.getScheduledAmPm();
        this.oldEmailEnabled = oldSupplement.getEmailEnabled();
        this.oldTextEnabled = oldSupplement.getTextEnabled();
        this.oldVoiceEnabled = oldSupplement.getVoiceEnabled();
        this.oldSuppNotes = oldSupplement.getSuppNotes();

        fillComponentsWithOldValues();

        ImageIcon editIcon = new ImageIcon(getClass().getResource("resources/icons/edit_icon_16x16.png"));
        dialog.setIconImage(editIcon.getImage());

        dialog.setTitle("Edit Supplement");
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Populates this dialog's input fields and sets the state of its
     * combo boxes based on the supplement's values prior to the user
     * opening this dialog.
     */
    private void fillComponentsWithOldValues()
    {
        suppNameTextField.setText(oldSuppName);

        // Remove the decimal point from whole numbers and display a maximum of two
        // decimal places if necessary.
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        suppAmountTextField.setText(decimalFormat.format(oldSuppAmount));

        suppUnitsComboBox.setSelectedItem(oldSuppUnits);

        hourComboBox.setSelectedIndex(oldSuppHours-1);
        minuteComboBox.setSelectedIndex(oldSuppMinutes);

        if (oldSuppAMPM.equals("AM"))
        {
            amPMComboBox.setSelectedIndex(0);
        }
        else
        {
            amPMComboBox.setSelectedIndex(1);
        }

        if (oldEmailEnabled)
        {
            emailEnabled = true;
            emailOnRadioButton.setSelected(true);
        }
        else
        {
            emailEnabled = false;
            emailOffRadioButton.setSelected(true);
        }

        if (oldTextEnabled)
        {
            textEnabled = true;
            textOnRadioButton.setSelected(true);
        }
        else
        {
            textEnabled = false;
            textOffRadioButton.setSelected(true);
        }

        if (oldVoiceEnabled)
        {
            voiceEnabled = true;
            voiceOnRadioButton.setSelected(true);
        }
        else
        {
            voiceEnabled = false;
            voiceOffRadioButton.setSelected(true);
        }

        suppNotesTextArea.setText(oldSuppNotes);
    }


    /**
     * Programatically presses the "Apply Changes" button if the user
     * presses Enter in the name, amount or units text fields.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        optionPane.setValue(buttonStrings[0]);
    }


    /**
     * Specifies the actions to take when the user presses "Apply Changes"
     * or "Cancel", or closes the window.  If "Apply Changes" is pressed,
     * the method validates the name, amount and units.  It then checks to
     * see if the user changed any of the supplement data.
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
                return;
            }

            // Reset the value of the JOptionPane to ensure that, when
            // the user presses the same button again, a property change
            // event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (value.equals(buttonStrings[0]))  // If the user pressed "Apply Changes".
            {
                // Get currently entered values
                suppNameText = suppNameTextField.getText().trim();

                suppAmountText = suppAmountTextField.getText().trim();
                suppAmountText = suppAmountText.replace(",", "");

                suppUnitsText = (String) suppUnitsComboBox.getSelectedItem();

                // Get the (24-hour) timeString based on comboBox states (e.g., "13:00:00")
                // timeString is converted to java.sql.Time below and set to this supplement's suppTime.
                String hour = (String) hourComboBox.getSelectedItem();
                String minute = (String) minuteComboBox.getSelectedItem();
                suppHours = Integer.parseInt(hour);
                suppMinutes = Integer.parseInt(minute);

                suppAMPM = (String) amPMComboBox.getSelectedItem();

                if (suppAMPM.equals("PM"))
                {
                    if (suppHours >= 1 && suppHours <= 11)
                    {
                        timeString = Integer.toString(suppHours + 12) + ":" + minute + ":" + "00";
                    }
                    else if (suppHours == 12)  // 12 PM should just be 12
                    {
                        timeString = Integer.toString(suppHours) + ":" + minute + ":" + "00";
                    }
                }
                else // AM
                {
                    if (suppHours == 12)  // 12 AM should be translated to "00"
                    {
                        hour = "00";
                    }

                    timeString = hour + ":" + minute + ":" + "00";
                }

                // Prepend a '0' to timeString if hour between 0 and 9.
                // This is necessary in order to compare this new time string
                // to the old time String, which is of the format "00:00:00".
                // The allows us to reliably detect if any changes have been
                // made to the time when we compare the new time String to the
                // old time String.
                if (Integer.parseInt(hour) >= 1 && Integer.parseInt(hour) <= 9)
                {
                    timeString = "0" + timeString;
                }

                // emailEnabled - automatically set by event handler
                // textEnabled   - automatically set by event handler
                // voiceEnabled - automatically set by event handler

                suppNotesText = suppNotesTextArea.getText().trim();

                // Validate name, amount, notes and units, then check if all four have been validated:

                suppNameValidated = false;
                suppAmountValidated = false;
                suppNotesValidated = false;

                // Validate name
                suppNameValidated = validateSuppName();

                // If name validated, validate amount
                if (suppNameValidated)
                {
                    suppAmountValidated = validateSuppAmount();
                }

                // If name and amount validated, validate notes
                if (suppNameValidated &&
                    suppAmountValidated)
                {
                    suppNotesValidated = validateSuppNotes();
                }

                // Check if everything has been validated
                if (suppNameValidated &&
                    suppAmountValidated &&
                    suppNotesValidated)
                {
                    inputValidated = true;

                    // All input validated, now see if any changes have been made
                    if (changesMade())
                    {
                        changesMade = true;

                        // Create newSupplement so it can be retrieved by the calling
                        // VitaReminderPanel#editSupplement() and compared with oldSupplement.
                        newSupplement = new Supplement();
                        newSupplement.setSuppID(oldSupplement.getSuppID());
                        newSupplement.setRegimenID(oldSupplement.getRegimenID());
                        newSupplement.setSuppName(suppNameText);
                        newSupplement.setSuppAmount(Double.parseDouble(suppAmountText));
                        newSupplement.setSuppUnits(suppUnitsText);
                        newSupplement.setSuppTime(java.sql.Time.valueOf(timeString));
                        newSupplement.setEmailEnabled(emailEnabled);
                        newSupplement.setTextEnabled(textEnabled);
                        newSupplement.setVoiceEnabled(voiceEnabled);
                        newSupplement.setSuppNotes(suppNotesText);

                        dialog.dispose();
                    }
                }
            }
            else  // The user closed the dialog or clicked "Cancel".
            {
                dialog.dispose();
            }
        }
    }


    /**
     * Checks to see if the user changed any of the values.
     *
     * @return  <tt>true</tt> if the user changed any of the values, <tt>false</tt> otherwise
     */
    private boolean changesMade()
    {
        double currentSuppAmount = Double.parseDouble(suppAmountText);

        // If none of the normal fields have changed
        if (suppNameText.equals(oldSuppName) &&
            currentSuppAmount == oldSuppAmount &&
            suppUnitsText.equals(oldSuppUnits) &&
            emailEnabled == oldEmailEnabled &&
            textEnabled == oldTextEnabled &&
            voiceEnabled == oldVoiceEnabled &&
            suppNotesText.equals(oldSuppNotes))
        {
            // If time is the same
            if (suppHours == oldSuppHours &&
                suppMinutes == oldSuppMinutes &&
                suppAMPM.equals(oldSuppAMPM))
            {
                JOptionPane.showMessageDialog(dialog,
                        "Please make a change or press Cancel.",
                        "No changes detected",
                        JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            else  // Time was changed
            {
                return true;
            }
        }
        else  // Changes have been made
        {
            return true;
        }
    }


    /**
     * After this dialog closes, called from {@code VitaReminderPanel#editSupplement()}
     * to see if the user made any changes to the supplement data.
     *
     * @return  <tt>true</tt> if the user made any changes, <tt>false</tt> otherwise
     */
    public boolean getChangesMade()
    {
        return changesMade;
    }

}  // end class EditSupplementDialog
