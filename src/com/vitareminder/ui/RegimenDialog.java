package com.vitareminder.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.vitareminder.business.Regimen;


/**
 * The superclass for {@code AddRegimenDialog} and {@code EditRegimenDialog}.
 * Builds the main dialog along with its panel.  Contains the validation
 * methods that are used by its subclasses to validate the user input.  It also
 * contains the {@code isInputValidated()} and {@code getNewRegimen()} methods
 * that are accessed by the {@code VitaReminderPanel} when the dialog exits.
 */
public class RegimenDialog implements RegimenInputValidator
{
    protected JDialog dialog;

    protected JPanel emptyPanel,
    regimenPanel;

    protected JLabel titleLabel, regimenNameLabel, regimenNotesLabel;

    protected JTextField regimenNameTextField;

    protected JTextArea regimenNotesTextArea;

    protected JScrollPane regimenNotesScrollPane;

    protected JOptionPane optionPane;

    protected String regimenNameText = "";
    protected String regimenNotesText = "";

    protected Regimen newRegimen;

    protected boolean inputValidated = false;


    /**
     * The sole constructor.  Creates a new modal {@code JDialog}.
     *
     * @param frame  the owner of this dialog
     */
    public RegimenDialog(JFrame frame)
    {
        dialog = new JDialog(frame, true);

        createPanel();

        optionPane = new JOptionPane(regimenPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);

        dialog.setContentPane(optionPane);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we)
            {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });
    }

    /**
     * Constructs a {@code JPanel} with the labels and input fields
     * placed in a {@code GridBayLayout}.
     */
    private void createPanel()
    {
        titleLabel = new JLabel("");
        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        titleLabel.setFont(boldFont);

        emptyPanel = new JPanel();

        regimenNameLabel = new JLabel("Regimen name:");

        regimenNameTextField = new JTextField(20);

        // Make sure regimenNameTextField always gets first focus.
        dialog.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent ce)
            {
                regimenNameTextField.requestFocusInWindow();
            }
        });

        regimenNotesLabel = new JLabel("Regimen notes:");

        regimenNotesTextArea = new JTextArea(7, 20);
        regimenNotesTextArea.setLineWrap(true);
        regimenNotesTextArea.setWrapStyleWord(true);
        regimenNotesScrollPane = new JScrollPane(regimenNotesTextArea);

        regimenPanel = new JPanel(new GridBagLayout());

        regimenPanel.add(titleLabel, GBCFactory.getConstraints(0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        regimenPanel.add(emptyPanel, GBCFactory.getConstraints(0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        regimenPanel.add(regimenNameLabel, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        regimenPanel.add(regimenNameTextField, GBCFactory.getConstraints(1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));
        regimenPanel.add(regimenNotesLabel, GBCFactory.getConstraints(0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        regimenPanel.add(regimenNotesScrollPane, GBCFactory.getConstraints(1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
    }


    /**
     * Checks that the regimen name is present and is between 1 and 60 characters long.
     */
    public boolean validateRegimenName()
    {
        if (regimenNameText.length() > 0)
        {
            if (regimenNameText.length() <= 60)
            {
                return true;
            }
            else
            {
                JOptionPane.showMessageDialog(dialog,
                        "Name cannot exceed 60 letters.",
                        "Invalid Data",
                        JOptionPane.ERROR_MESSAGE);

                regimenNameTextField.requestFocusInWindow();

                return false;
            }
        }
        else  // No text entered.
        {
            JOptionPane.showMessageDialog(dialog,
                    "Please enter a regimen name.",
                    "Invalid Data",
                    JOptionPane.ERROR_MESSAGE);

            regimenNameTextField.requestFocusInWindow();

            return false;
        }
    }


    /**
     * Checks that the regimen notes are 1000 characters or less.
     */
    public boolean validateRegimenNotes()
    {
        if (regimenNotesText.length() <= 1000)
        {
            return true;
        }
        else
        {
            JOptionPane.showMessageDialog(dialog,
                    "Notes cannot exceed 1000 characters.",
                    "Invalid Data",
                    JOptionPane.ERROR_MESSAGE);

            regimenNotesTextArea.requestFocusInWindow();

            return false;
        }
    }


    /**
     * After this dialog closes, called from {@code VitaReminderPanel#addRegimen()} or
     * {@code VitaReminderPanel#editRegimen()} to ensure that the user's input has been
     * validated.
     *
     * @return  <tt>true</tt> if the name and notes have been validated, <tt>false</tt> otherwise
     */
    public boolean isInputValidated()
    {
        return inputValidated;
    }


    /**
     * After this dialog closes, called from {@code VitaReminderPanel#addRegimen()} or
     * {@code VitaReminderPanel#editRegimen()} to access the new values for the name and
     * notes that the user has entered into this dialog.
     *
     * @return  the new {@code Regimen} object based on the user's dialog input
     */
    public Regimen getNewRegimen()
    {
        return newRegimen;
    }

}  // end class RegimenDialog
