package com.vitareminder.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.vitareminder.business.Regimen;


/**
 * A modal dialog that allows the user to edit the currently selected regimen
 * in the regimen table.
 */
public class EditRegimenDialog extends RegimenDialog implements PropertyChangeListener
{
    String[] buttonStrings = {"Apply Changes", "Cancel"};

    private boolean regimenNameValidated = false;
    private boolean regimenNotesValidated = false;
    private boolean changesMade = false;

    private String oldRegimenName;
    private String oldRegimenNotes;


    /**
     * The sole constructor.  Calls the superclass {@code RegimenDialog} to build
     * the main dialog, then modifies it by setting its own title and window icon.
     *
     * @param frame  the owner of this dialog
     * @param oldRegimen  the regimen in its state prior to the user opening this dialog
     */
    public EditRegimenDialog(JFrame frame, Regimen oldRegimen)
    {
        super(frame);

        this.oldRegimenName = oldRegimen.getRegimenName();
        this.oldRegimenNotes = oldRegimen.getRegimenNotes();

        titleLabel.setText("Edit Regimen \"" + oldRegimenName + "\"");

        regimenNameTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                optionPane.setValue(buttonStrings[0]);
            }
        });

        optionPane.setOptions(buttonStrings);
        optionPane.addPropertyChangeListener(this);

        fillComponentsWithOldValues(oldRegimenName, oldRegimenNotes);

        ImageIcon editIcon = new ImageIcon(getClass().getResource("resources/icons/edit_icon_16x16.png"));
        dialog.setIconImage(editIcon.getImage());

        dialog.setTitle("Edit Regimen");
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * After this dialog closes, called from {@code VitaReminderPanel#editRegimen()}
     * to determine whether or not the regimen table model should be updated.
     *
     * @return  <tt>true</tt> if changes were made to the regimen name or notes, <tt>false</tt> otherwise
     */
    public boolean getChangesMade()
    {
        return changesMade;
    }


    /**
     * When the dialog is created, fills the input fields with the values already
     * present in this regimen.
     *
     * @param oldRegimenName  the regimen name that the user had previously set
     * @param oldRegimenNotes  the regimen notes that the user had previously set
     */
    private void fillComponentsWithOldValues(String oldRegimenName, String oldRegimenNotes)
    {
        regimenNameTextField.setText(oldRegimenName);
        regimenNotesTextArea.setText(oldRegimenNotes);
    }


    /**
     * Specifies the actions to take when the user presses "Apply Changes"
     * or "Cancel", or closes the window.  If "Apply Changes" is pressed,
     * the method validates the name and notes and then checks to see if
     * any changes to either of these have been made.
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

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            // If the user pressed "Apply Changes"
            if (value.equals(buttonStrings[0]))
            {
                // Get currently entered values
                regimenNameText = regimenNameTextField.getText().trim();
                regimenNotesText = regimenNotesTextArea.getText().trim();

                regimenNameValidated = false;
                regimenNotesValidated = false;

                // Validate name
                regimenNameValidated = validateRegimenName();

                // If name validated, validate notes
                if (regimenNameValidated)
                {
                    regimenNotesValidated = validateRegimenNotes();
                }

                // See if both fields that need validating have been validated.
                if (regimenNameValidated &&
                    regimenNotesValidated)
                {
                    inputValidated = true;

                    newRegimen = new Regimen();
                    newRegimen.setRegimenName(regimenNameText);
                    newRegimen.setRegimenNotes(regimenNotesText);

                    // All input validated, now see if any changes have been made.
                    if (changesMade())
                    {
                        changesMade = true;
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
     * Checks to see if the user changed the regimen name or notes.
     *
     * @return  <tt>true</tt> if the user changed the name or notes, <tt>false</tt> otherwise
     */
    private boolean changesMade()
    {
        // If none of the fields have changed
        if (regimenNameText.equals(oldRegimenName) &&
            regimenNotesText.equals(oldRegimenNotes))
        {
            JOptionPane.showMessageDialog(dialog,
                    "Please make a change or press Cancel.",
                    "No changes detected",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        else
        {
            return true;
        }
    }

}  // end class EditRegimenDialog
