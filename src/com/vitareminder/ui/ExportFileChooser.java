package com.vitareminder.ui;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.vitareminder.dao.DAOManager;


/**
 * This class displays a {@code JFileChooser} modal dialog that allows
 * the user to select a file to backup their data to.  The user should
 * only be allowed to backup to a file with a .vrdata extension.  A .vrdata
 * file is actually a SQL script that contains the commands to create and
 * populate the user's regimens and supplements tables.
 */
public class ExportFileChooser
{
    JFileChooser fileChooser;

    private final JFrame frame;
    private DAOManager daoManager;
    private boolean exportSuccess;


    /**
     * The sole constructor.  {@code exportSuccess} is set to <tt>false</tt>
     * and is only set to <tt>true</tt> if the database returns <tt>true</tt>
     * after successfully exporting the SQL script.
     *
     * @param frame  the owner of this dialog
     * @param daoManager  a reference to the application's DAO layer
     */
    @SuppressWarnings("serial")
    public ExportFileChooser(final JFrame frame, DAOManager daoManager)
    {
        this.frame = frame;
        this.daoManager = daoManager;

        exportSuccess = false;

        fileChooser = new JFileChooser() {

            @Override
            public void approveSelection()
            {
                File f = getSelectedFile();
                if (f.exists())
                {
                    String[] options = {"YES", "NO"};
                    int result = JOptionPane.showOptionDialog(frame,
                            "This file already exists.\nDo you want to replace it?",
                            "File already exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[1]);

                    switch (result)
                    {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CLOSED_OPTION:
                        return;
                    }
                }

                super.approveSelection();
            }
        };

        fileChooser.setDialogTitle("Export");

        // Build the default file name
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date();
        String fileName = "VitaReminder_" + dateFormat.format(date) + ".vrdata";

        fileChooser.setSelectedFile(new File(fileName));

        // Set the file filter to only display .vrdata files
        FileFilter filter = new FileNameExtensionFilter("VitaReminder data (.vrdata)", "vrdata");
        fileChooser.setFileFilter(filter);

        display();
    }


    /**
     * Displays the {@code JFileChooser} on the screen and allows the user
     * to select a file to back up their data to.  The {@code backupDatabase}
     * method attempts to generate a SQL script and save it at the {@code filePath}.
     * location.  If successful, this method returns <tt>true</tt> and this value is stored
     * in the present class's {@code exportSuccess} field.  After this dialog closes,
     * this field is accessed by the {@code importMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame}.
     */
    private void display()
    {
        int option = fileChooser.showSaveDialog(frame);

        if (option == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getPath();

            exportSuccess = daoManager.getDbDAO().backupDatabase(filePath);
        }
    }


    /**
     * Called by the {@code importMenuItem}'s {@code ActionListner} in {@code VitaReminderFrame}
     * to determine whether or not the user's backup file was successfully generated and saved.
     *
     * @return <tt>true</tt> if the backup file was successfully generated and saved,
     *         <tt>false</tt> otherwise
     */
    public boolean getExportSuccess()
    {
        return exportSuccess;
    }

}  // end class ExportFileChooser
