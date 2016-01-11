package com.vitareminder;

import java.awt.EventQueue;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.apache.log4j.Logger;

import com.vitareminder.dao.DAOManager;
import com.vitareminder.db.DataSource;
import com.vitareminder.reminders.ReminderManager;
import com.vitareminder.ui.VitaReminderFrame;


/**
 * This class contains the entry point for the application and
 * contains the application initialization code.
 *
 * @author Jason Jobes
 */
public class VitaReminder
{
    private static DAOManager daoManager;
    private static ReminderManager reminderManager;

    private static Logger logger = Logger.getLogger(VitaReminder.class);

    /**
     * The entry point for the application.  This method first attempts to make sure
     * that there is no other instance of the application already running.  It does
     * so by attempting to create a file and place a lock on it. If this fails, another
     * instance is running and the application exits.  If the file and lock are successfully
     * created, a connection to the database is made and the DAO layer is established by
     * creating an instance of the {@code DAOManager} class.  An instance of the
     * {@code ReminderManager} class is created, which initializes Quartz Scheduler.
     * The application pauses for one second to allow the splash screen to display
     * properly.  After this, the application's main frame ({@code VitaReminderFrame})
     * is created within Swing's Event Dispatch Thread.
     *
     * @param args  command-line arguments are ignored
     */
    public static void main(String[] args)
    {
        logger.info("VitaReminder application started.");

        boolean lockSuccess = createLockFile("user/data/application.lock");
        if (!lockSuccess)
        {
            logger.error("An attempt to create another instance of the application has been detected. " +
                         "That instance will now terminate.");
            System.exit(-1);
        }
        else
        {
            logger.info("Lock file application.lock successfully created.");
        }

        DataSource dataSource = new DataSource();
        daoManager = new DAOManager(dataSource);

        reminderManager = new ReminderManager(daoManager);
        reminderManager.loadStartupReminders();

        // Let the splash screen display for an additional 1 second
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            logger.error("Thread.sleep() was interrupted while the splash screen was loading.", e);
        }

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run()
            {
                new VitaReminderFrame(daoManager, reminderManager);
            }
        });
    }


    /**
     * This method creates a file and attempts to place a lock on it.  This is used to
     * ensure that only a single instance of the application is running at a time.  If
     * the file and lock are successfully created, it creates a shutdown hook for the
     * application that does some clean-up work when the JVM terminates (when System.exit()
     * is called, the user logs off, or when the system shuts down).
     *
     * @param lockFile  the name of the lock file to be created
     * @return <tt>true</tt> if the file and lock were successfully created,
     * 		   <tt>false</tt> otherwise
     */
    private static boolean createLockFile(String lockFile)
    {
        try
        {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();

            if (fileLock != null)
            {
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    public void run()
                    {
                        try
                        {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                            logger.info("Lock file application.lock deleted.");

                            daoManager.closeDatabaseConnection();
                            reminderManager.shutdownScheduler();
                            logger.info("VitaReminder application closing.");
                        }
                        catch (Exception e)
                        {
                            logger.error("Could not delete lock file: application.lock", e);
                        }
                    }
                });

                return true;
            }
        }
        catch (Exception e)
        {
            logger.error("An error has occurred while creating or locking the file: application.lock", e);
        }

        return false;
    }

}  // end class VitaReminder
