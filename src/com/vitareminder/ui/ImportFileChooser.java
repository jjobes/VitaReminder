package com.vitareminder.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.vitareminder.dao.DAOManager;


/**
 * This class displays a {@code JFileChooser} modal dialog that allows
 * the user to select and load a backup file that they had previously
 * saved.  The user should only be allowed to select a file with a
 * .vrdata extension.  A .vrdata file is actually a SQL script that
 * contains the commands to create and populate the user's regimens
 * and supplements tables.
 */
public class ImportFileChooser
{
    JFileChooser fileChooser;

    private final JFrame frame;
    private DAOManager daoManager;

    private boolean importSuccess;


    /**
     * The sole constructor.  {@code importSuccess} is set to <tt>false</tt>
     * and is only set to <tt>true</tt> if the database returns <tt>true</tt>
     * after successfully running the script.
     *
     * @param frame  the owner of this dialog
     * @param daoManager  a reference to the application's DAO layer
     */
    public ImportFileChooser(final JFrame frame, DAOManager daoManager)
    {
        this.frame = frame;
        this.daoManager = daoManager;

        importSuccess = false;

        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import");

        // Set the file filter to only display .vrdata files
        FileFilter filter = new FileNameExtensionFilter("VitaReminder data (.vrdata)", "vrdata");
        fileChooser.setFileFilter(filter);

        display();
    }


    /**
     * Displays the {@code JFileChooser} on the screen and allows the user
     * to select a file to restore a previous database state.  The method
     * forces the user to select a file with a .vrdata extension, and will
     * redisplay the {@code JFileChooser} if they attempt to select a file
     * with any other extension.
     * <p>
     * The {@code restoreDatabase} method attempts to run the SQL script
     * located at {@code filePath}.  If successful, this method returns
     * <tt>true</tt> and this value is stored in the present class's
     * {@code importSuccess} field.  After this dialog closes, this field
     * is accessed by the {@code importMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame}.
     */
    private void display()
    {
        int option = fileChooser.showOpenDialog(frame);

        while (option == JFileChooser.APPROVE_OPTION && importSuccess == false)
        {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getPath();

            if (filePath.endsWith(".vrdata"))
            {
                importSuccess = daoManager.getDbDAO().restoreDatabase(filePath);
            }
            else
            {
                JOptionPane.showMessageDialog(null,
                        "Please select a valid .vrdata file.",
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);

                option = fileChooser.showOpenDialog(frame);
            }
        }
    }


    /**
     * Called by the {@code importMenuItem}'s {@code ActionListner} in {@code VitaReminderFrame}
     * to determine whether or not the user's backup file was successfully imported and
     * executed.
     *
     * @return <tt>true</tt> if the backup file was successfully imported and executed,
     *         <tt>false</tt> otherwise
     */
    public boolean getImportSuccess()
    {
        return importSuccess;
    }

}  // end class ImportFileChooser
