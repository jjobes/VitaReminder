package com.vitareminder.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import com.vitareminder.business.Supplement;


/**
 * The superclass for {@code AddSupplementDialog} and {@code EditSupplementDialog}.
 * Builds the main dialog along with its panel.  Contains the validation
 * methods that are used by its subclasses to validate the user input.  It also
 * contains the {@code isInputValidated()} and {@code getNewRegimen()} methods
 * that are accessed by the {@code VitaReminderPanel} when the dialog exits.
 */
public class SupplementDialog implements SupplementInputValidator
{
    protected JDialog dialog;

    protected JPanel supplementPanel,
        emptyPanel,
        suppAmountPanel,
        emptyAmountPanel,
        timePanel,
        remindersPanel,
        emailPanel,
        textPanel,
        voicePanel;

    protected JLabel titleLabel,
        suppNameLabel,
        suppAmountLabel,
        suppUnitsLabel,
        suppTimeLabel,
        suppNotesLabel,
        remindersLabel,
        emailLabel,
        textLabel,
        voiceLabel,
        questionMark;

    protected JTextField suppNameTextField,
        suppAmountTextField,
        suppUnitsTextField;

    protected Border timeBorder,
        remindersBorder;

    protected JComboBox<String> suppUnitsComboBox,
        hourComboBox,
        minuteComboBox,
        amPMComboBox;

    protected DefaultComboBoxModel<String> units;

    protected JRadioButton emailOnRadioButton,
        emailOffRadioButton,
        textOnRadioButton,
        textOffRadioButton,
        voiceOnRadioButton,
        voiceOffRadioButton;

    protected ButtonGroup emailButtonGroup,
        textButtonGroup,
        voiceButtonGroup;

    protected JTextArea suppNotesTextArea;

    protected JScrollPane suppNotesScrollPane;

    protected JOptionPane optionPane;

    protected String suppName = "";

    protected String suppNameText = "";
    protected String suppAmountText = "";
    protected double suppAmountDouble = 0.0;
    protected String suppUnitsText = "";
    protected String timeString = "";
    protected String suppNotesText = "";

    protected boolean emailEnabled = false;
    protected boolean textEnabled = false;
    protected boolean voiceEnabled = false;

    protected boolean suppNameValidated = false;
    protected boolean suppAmountValidated = false;
    protected boolean suppNotesValidated = false;

    protected Supplement newSupplement;

    protected boolean inputValidated = false;


    /**
     * The sole constructor.  Creates a new modal {@code JDialog}
     * with a {@code JOptionPane} set to its content pane.
     *
     * @param frame  the owner of this dialog
     */
    public SupplementDialog(JFrame frame)
    {
        dialog = new JDialog(frame, true);

        createAmountPanel();
        createTimePanel();
        createRemindersPanel();
        createMainPanel();

        optionPane = new JOptionPane(supplementPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);

        dialog.setContentPane(optionPane);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {

            // When the user closes the window, update the JOptionPane's
            // value to fire a propertyChangeEvent.
            @Override
            public void windowClosing(WindowEvent we)
            {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });
    }


    /**
     * Creates a {@code JPanel} that contains the {@code JTextField} for the
     * supplement amount and a {@code JComboBox} for the supplement units.
     */
    private void createAmountPanel()
    {
        suppAmountLabel = new JLabel("Amount:");
        suppAmountTextField = new JTextField(5);
        suppAmountTextField.setHorizontalAlignment(JTextField.RIGHT);

        emptyAmountPanel = new JPanel();
        emptyAmountPanel.setPreferredSize(new Dimension(10, 0));

        String[] units = {"g", "iu", "mcg", "mg", "oz", "tbsp",
                          "tsp", "capsule", "capsules",
                          "tablet", "tablets"};
        suppUnitsComboBox = new JComboBox<String>(units);

        suppAmountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ((FlowLayout) suppAmountPanel.getLayout()).setHgap(0);

        suppAmountPanel.add(suppAmountTextField);
        suppAmountPanel.add(emptyAmountPanel);
        suppAmountPanel.add(suppUnitsComboBox);
    }


    /**
     * Creates a {@code JPanel} that contains a {@code JLabel} and the {@code JComboBox}es
     * for the hour, minute, and AM/PM settings for when the user intends to take this
     * supplement each day.  This time is also used to send a reminder to the user, if any
     * reminders are turned on.  The components are placed in a {@code GridBayLayout}.
     */
    private void createTimePanel()
    {
        suppTimeLabel = new JLabel("Take at:");

        String[] hours = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

        String[] minutes = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

        String[] amPM = {"AM", "PM"};

        hourComboBox = new JComboBox<String>(hours);
        minuteComboBox = new JComboBox<String>(minutes);
        amPMComboBox = new JComboBox<String>(amPM);

        timePanel = new JPanel(new GridBagLayout());
        timeBorder = BorderFactory.createEtchedBorder();
        timePanel.setBorder(timeBorder);

        timePanel.add(hourComboBox, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        timePanel.add(minuteComboBox, GBCFactory.getConstraints(1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        timePanel.add(amPMComboBox, GBCFactory.getConstraints(2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
    }


    /**
     * Creates a {@JPanel} containing three radio button groups - one for
     * e-mail reminders, one for text message reminders and one for automated
     * voice reminders.  The components are placed in a {@code GridBayLayout}.
     */
    private void createRemindersPanel()
    {
        remindersLabel = new JLabel("Reminders:");

        emailLabel = new JLabel("E-Mail");

        EmailButtonListener emailButtonListener = new EmailButtonListener();
        emailOnRadioButton = new JRadioButton("On");
        emailOnRadioButton.addActionListener(emailButtonListener);

        emailOffRadioButton = new JRadioButton("Off", true);
        emailOffRadioButton.addActionListener(emailButtonListener);

        emailButtonGroup = new ButtonGroup();
        emailButtonGroup.add(emailOnRadioButton);
        emailButtonGroup.add(emailOffRadioButton);

        textLabel = new JLabel("Text");

        TextButtonListener textButtonListener = new TextButtonListener();
        textOnRadioButton = new JRadioButton("On");
        textOnRadioButton.addActionListener(textButtonListener);

        textOffRadioButton = new JRadioButton("Off", true);
        textOffRadioButton.addActionListener(textButtonListener);

        textButtonGroup = new ButtonGroup();
        textButtonGroup.add(textOnRadioButton);
        textButtonGroup.add(textOffRadioButton);

        voiceLabel = new JLabel("Voice");

        VoiceButtonListener voiceButtonListener = new VoiceButtonListener();
        voiceOnRadioButton = new JRadioButton("On");
        voiceOnRadioButton.addActionListener(voiceButtonListener);

        voiceOffRadioButton = new JRadioButton("Off", true);
        voiceOffRadioButton.addActionListener(voiceButtonListener);

        voiceButtonGroup = new ButtonGroup();
        voiceButtonGroup.add(voiceOnRadioButton);
        voiceButtonGroup.add(voiceOffRadioButton);

        remindersPanel = new JPanel(new GridBagLayout());
        remindersBorder = BorderFactory.createEtchedBorder();
        remindersPanel.setBorder(remindersBorder);

        remindersPanel.add(emailLabel, GBCFactory.getWeightedConstraints(0, 0, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(emailOnRadioButton, GBCFactory.getConstraints(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(emailOffRadioButton, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(textLabel, GBCFactory.getWeightedConstraints(1, 0, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(textOnRadioButton, GBCFactory.getConstraints(1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(textOffRadioButton, GBCFactory.getConstraints(1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(voiceLabel, GBCFactory.getWeightedConstraints(2, 0, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(voiceOnRadioButton, GBCFactory.getConstraints(2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        remindersPanel.add(voiceOffRadioButton, GBCFactory.getConstraints(2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
    }


    /**
     * Creates the main {@JPanel} with the text input fields for the supplement
     * name, amount, units and notes.  The {@code timePanel} and {@code remindersPanel}
     * are added to this panel.  The components are placed in a {@code GridBayLayout}.
     */
    private void createMainPanel()
    {
        titleLabel = new JLabel("");
        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        titleLabel.setFont(boldFont);

        emptyPanel = new JPanel();

        suppNameLabel = new JLabel("Supplement name:");
        suppNameTextField = new JTextField(20);

        suppNotesLabel = new JLabel("Supplement notes:");
        suppNotesTextArea = new JTextArea(4, 10);
        suppNotesTextArea.setLineWrap(true);
        suppNotesTextArea.setWrapStyleWord(true);
        suppNotesScrollPane = new JScrollPane(suppNotesTextArea);

        supplementPanel = new JPanel(new GridBagLayout());

        supplementPanel.add(titleLabel, GBCFactory.getConstraints(0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        supplementPanel.add(emptyPanel, GBCFactory.getConstraints(0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        supplementPanel.add(suppNameLabel, GBCFactory.getConstraints(0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        supplementPanel.add(suppNameTextField, GBCFactory.getConstraints(1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        supplementPanel.add(suppAmountLabel, GBCFactory.getConstraints(0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        supplementPanel.add(suppAmountPanel, GBCFactory.getConstraints(1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        supplementPanel.add(suppNotesLabel, GBCFactory.getConstraints(0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        supplementPanel.add(suppNotesScrollPane, GBCFactory.getConstraints(1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL));
        supplementPanel.add(suppTimeLabel, GBCFactory.getConstraints(0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        supplementPanel.add(timePanel, GBCFactory.getConstraints(1, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));
        supplementPanel.add(remindersLabel, GBCFactory.getConstraints(0, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE));
        supplementPanel.add(remindersPanel, GBCFactory.getConstraints(1, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH));
    }


    /**
     * Checks to see if a supplement name is present, and then checks
     * that this name is less than or equal to 60 characters.
     */
    public boolean validateSuppName()
    {
        if (suppNameText.length() > 0)
        {
            if (suppNameText.length() <= 60)
            {
                return true;
            }
            else
            {
                JOptionPane.showMessageDialog(dialog,
                        "Name cannot exceed 60 letters.",
                        "Invalid Data",
                        JOptionPane.ERROR_MESSAGE);

                suppNameTextField.requestFocusInWindow();

                return false;
            }
        }
        else  // No text entered.
        {
            JOptionPane.showMessageDialog(dialog,
                    "Please enter a supplement name.",
                    "Invalid Data",
                    JOptionPane.ERROR_MESSAGE);

            suppNameTextField.requestFocusInWindow();

            return false;
        }
    }


    /**
     * If the user decides to enter an amount for their supplement, this
     * method checks that their entry is less than 12 digits long, can be
     * parsed into a {@code double} and is non-negative.  A value of
     * <tt>0</tt> is acceptable.
     */
    public boolean validateSuppAmount()
    {
        if (!suppAmountText.isEmpty())
        {
            if (suppAmountText.length() <= 12)
            {
                try
                {
                    suppAmountDouble = Double.parseDouble(suppAmountText);

                    if (suppAmountDouble >= 0)
                    {
                        return true;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(dialog,
                                "Amount must be positive.",
                                "Invalid Data",
                                JOptionPane.ERROR_MESSAGE);

                        suppAmountTextField.selectAll();
                        suppAmountTextField.requestFocusInWindow();

                        return false;
                    }
                }
                catch (NumberFormatException nfe)
                {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid supplement amount.",
                            "Invalid Data",
                            JOptionPane.ERROR_MESSAGE);

                    suppAmountTextField.selectAll();
                    suppAmountTextField.requestFocusInWindow();

                    return false;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(dialog,
                        "The supplement amount is too high.",
                        "Invalid Data",
                        JOptionPane.ERROR_MESSAGE);

                suppAmountTextField.selectAll();
                suppAmountTextField.requestFocusInWindow();

                return false;
            }
        }
        else  // Nothing entered in amount, so count it as validated.
        {
            return true;
        }
    }


    /**
     * If the user decides to enter supplement notes, this method
     * checks that they do not exceed 1000 characters.
     */
    public boolean validateSuppNotes()
    {
        if (suppNotesText.length() <= 1000)
        {
            return true;
        }
        else
        {
            JOptionPane.showMessageDialog(dialog,
                    "Notes cannot exceed 1000 characters.",
                    "Invalid Data",
                    JOptionPane.ERROR_MESSAGE);

            suppNotesTextArea.requestFocusInWindow();

            return false;
        }
    }


    /**
     * Called by {@code VitaReminderPanel#addSupplement()} and {@code VitaReminderPanel#editSupplement()}
     * after this dialog returns.
     *
     * @return  <tt>true</tt> if the user pressed "OK"/"Apply Changes" and the input has been
     *          validated, <tt>false</tt> if the user cancels the dialog
     */
    public boolean isInputValidated()
    {
        return inputValidated;
    }


    /**
     * Called by {@code VitaReminderPanel#addSupplement()} and {@code VitaReminderPanel#editSupplement()}
     * after this dialog returns.
     *
     * @return  a {@code Supplement} object built using the values that the user
     *          has just input into the dialog
     */
    public Supplement getNewSupplement()
    {
        return newSupplement;
    }


    /**
     * The event handler shared by both the {@code emailOnRadioButton} and
     * {@code emailOffRadioButton}.  Placed in an inner class to avoid code
     * duplication.  Sets the {@code emailEnabled} class variable to <tt>true</tt>
     * or <tt>false</tt> depending on whether the "On" or "Off" radio button
     * is selected.
     */
    class EmailButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JRadioButton button = (JRadioButton) e.getSource();

            if (button.getText().equals("On"))
            {
                emailEnabled = true;
            }
            else
            {
                emailEnabled = false;
            }
        }
    }


    /**
     * The event handler shared by both the {@code textOnRadioButton} and
     * {@code textOffRadioButton}.  Placed in an inner class to avoid code
     * duplication.  Sets the {@code textEnabled} class variable to <tt>true</tt>
     * or <tt>false</tt> depending on whether the "On" or "Off" radio button
     * is selected.
     */
    class TextButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JRadioButton button = (JRadioButton) e.getSource();

            if (button.getText().equals("On"))
            {
                textEnabled = true;
            }
            else
            {
                textEnabled = false;
            }
        }
    }


    /**
     * The event handler shared by both the {@code voiceOnRadioButton} and
     * {@code voiceOffRadioButton}.  Placed in an inner class to avoid code
     * duplication.  Sets the {@code voiceEnabled} class variable to <tt>true</tt>
     * or <tt>false</tt> depending on whether the "On" or "Off" radio button
     * is selected.
     */
    class VoiceButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JRadioButton button = (JRadioButton) e.getSource();

            if (button.getText().equals("On"))
            {
                voiceEnabled = true;
            }
            else
            {
                voiceEnabled = false;
            }
        }
    }

}  // end class SupplementDialog
