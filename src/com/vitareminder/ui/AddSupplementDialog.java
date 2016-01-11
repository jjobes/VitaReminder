package com.vitareminder.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.vitareminder.business.Supplement;


/**
 * A {@code JDialog} with input fields that allows the user to create a new {@code Supplement}.
 */
public class AddSupplementDialog extends SupplementDialog implements ActionListener, PropertyChangeListener
{
    String[] buttonStrings = {"OK", "Cancel"};


    /**
     * The sole constructor.  Calls the superclass {@code SupplementDialog},
     * which creates the modal {@code JDialog} that contains the input components.
     * This constructor modifies the dialog by adding a custom title and window
     * icon.  It retrives the user's global preferences and either enables or disables
     * the reminder radio buttons depending on these preferences.
     *
     * @param frame  the owner of this dialog
     * @param regimenName  the name of the regimen that this supplement belongs to
     */
    public AddSupplementDialog(JFrame frame, String regimenName)
    {
        super(frame);

        titleLabel.setText("Add Supplement to \"" + regimenName + "\"");

        Preferences userPreferences = Preferences.userRoot();
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);

        // Enable or disable the reminder radio buttons based on user preferences.
        emailOnRadioButton.setEnabled(emailRemindersEnabled);
        emailOffRadioButton.setEnabled(emailRemindersEnabled);
        textOnRadioButton.setEnabled(textRemindersEnabled);
        textOffRadioButton.setEnabled(textRemindersEnabled);
        voiceOnRadioButton.setEnabled(voiceRemindersEnabled);
        voiceOffRadioButton.setEnabled(voiceRemindersEnabled);

        suppNameTextField.addActionListener(this);
        suppAmountTextField.addActionListener(this);

        optionPane.setOptions(buttonStrings);
        optionPane.addPropertyChangeListener(this);

        ImageIcon addIcon = new ImageIcon(getClass().getResource("resources/icons/add_icon_16x16.png"));
        dialog.setIconImage(addIcon.getImage());

        dialog.setTitle("Add Supplement");
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * If the user presses Enter inside any of the text fields,
     * programatically press the "OK" button.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        optionPane.setValue(buttonStrings[0]);
    }


    /**
     * Processes the input when the user presses the "OK" button; otherwise
     * close the dialog.  When the user presses the "OK" button, construct the
     * {@code timeString} from the combo boxes, then validate the remaining
     * input fields.  If all fields are validated, set {@code inputValidated}
     * to <tt>true</tt>, then close the dialog.
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

            if (value.equals(buttonStrings[0]))  // If the user pressed "OK"
            {
                suppNameText = suppNameTextField.getText().trim();

                suppAmountText = suppAmountTextField.getText().trim();
                suppAmountText = suppAmountText.replace(",", "");

                suppUnitsText = (String) suppUnitsComboBox.getSelectedItem();

                // Get the (24-hour) timeString based on comboBox states (e.g., "13:00:00")
                // timeString is converted to java.sql.Time below and set to this supplement's suppTime.
                String hour = (String) hourComboBox.getSelectedItem();
                String minute = (String) minuteComboBox.getSelectedItem();
                int suppHours = Integer.parseInt(hour);
                String suppAMPM = (String) amPMComboBox.getSelectedItem();

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

                suppNotesText = suppNotesTextArea.getText().trim();

                // Validate name, amount and notes, then check if all four have been validated:
                suppNameValidated = false;
                suppAmountValidated = false;
                suppNotesValidated = false;

                suppNameValidated = validateSuppName();

                if (suppNameValidated)
                {
                    suppAmountValidated = validateSuppAmount();
                }

                if (suppNameValidated &&
                    suppAmountValidated)
                {
                    suppNotesValidated = validateSuppNotes();
                }

                if (suppNameValidated &&
                    suppAmountValidated &&
                    suppNotesValidated)
                {
                    inputValidated = true;

                    newSupplement = new Supplement();
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
            else  // The user closed the dialog or clicked "Cancel".
            {
                dialog.dispose();
            }
        }
    }

}  // end class AddSupplementDialog
