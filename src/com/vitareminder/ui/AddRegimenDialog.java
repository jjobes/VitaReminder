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
 * A {@code JDialog} with input fields that allows the user to create a new {@code Regimen}.
 */
public class AddRegimenDialog extends RegimenDialog implements PropertyChangeListener
{
    String[] buttonStrings = {"OK", "Cancel"};

    private boolean regimenNameValidated = false;
    private boolean regimenNotesValidated = false;


    /**
     * The sole constructor.  Calls the superclass (@code RegimenDialog},
     * which creates the modal {@code JDialog} that contains the input
     * components.  This constructor modifies that dialog by adding a custom
     * title and window icon before displaying the dialog.
     *
     * @param frame  the owner of this dialog
     */
    public AddRegimenDialog(JFrame frame)
    {
        super(frame);

        titleLabel.setText("Add a New Regimen");

        regimenNameTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                optionPane.setValue(buttonStrings[0]);
            }
        });

        optionPane.setOptions(buttonStrings);
        optionPane.addPropertyChangeListener(this);

        ImageIcon addIcon = new ImageIcon(getClass().getResource("resources/icons/add_icon_16x16.png"));
        dialog.setIconImage(addIcon.getImage());

        dialog.setTitle("Add Regimen");
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    /**
     * Defines the actions to take when the user presses the "OK"
     * or "Cancel" buttons or closes the dialog.  If the user presses
     * the "OK" button, retrieve the values from the input fields and
     * validate them.  If they are both validated, set {@code inputValidated}
     * to <tt>true</tt> and close the dialog.  Otherwise, simply close the
     * dialog.
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

            if (value.equals(buttonStrings[0]))  // If the user pressed "OK"
            {
                regimenNameText = regimenNameTextField.getText().trim();
                regimenNotesText = regimenNotesTextArea.getText().trim();

                regimenNameValidated = false;
                regimenNotesValidated = false;

                regimenNameValidated = validateRegimenName();

                if (regimenNameValidated)
                {
                    regimenNotesValidated = validateRegimenNotes();
                }

                if (regimenNameValidated &&
                    regimenNotesValidated)
                {
                    inputValidated = true;

                    newRegimen = new Regimen();
                    newRegimen.setRegimenName(regimenNameText);
                    newRegimen.setRegimenNotes(regimenNotesText);

                    dialog.dispose();
                }
            }
            else  // The user closed the dialog or clicked "Cancel".
            {
                dialog.dispose();
            }
        }
    }

}  // end class AddRegimenDialog
