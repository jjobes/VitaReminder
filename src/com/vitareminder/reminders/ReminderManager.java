package com.vitareminder.reminders;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.vitareminder.business.Supplement;
import com.vitareminder.dao.DAOManager;
import com.vitareminder.reports.HtmlGenerator;


/**
 * Centrally handles the loading and unloading of all reminders, which
 * are implemented as Quartz jobs.  The static imports are necessary to
 * use the job and trigger builders in Quartz's DSL style.
 */
public class ReminderManager
{
    private DAOManager daoManager;
    private List<Supplement> remindersFromDB = null;
    private Scheduler scheduler;
    private Preferences userPreferences;

    private Logger logger = Logger.getLogger(ReminderManager.class);


    /**
     * The sole constructor.  Points Quartz to the quartz.properties file for
     * proper initialization.  Creates and starts the scheduler.  A single
     * instance of {@code ReminderManager} is created in {@code VitaReminderApp}.
     *
     * @param daoManager  used to access the DAO layer in retrieving the {@code Supplement}s
     *                    from the database that have reminders set on them
     */
    public ReminderManager(DAOManager daoManager)
    {
        System.setProperty("org.quartz.properties", "res/quartz/quartz.properties");

        this.daoManager = daoManager;

        userPreferences = Preferences.userRoot();

        try
        {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            logger.info("Quartz Scheduler started.");
        }
        catch (SchedulerException e)
        {
            logger.error("Unable to start Quartz Scheduler.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to start the scheduler.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Called on application startup from {@code VitaReminder} to load all
     * {@code Supplement}s from the database that have reminders set on them,
     * and to create a new Quartz job for each one.  A reminder for a particular
     * {@code Supplement} is only loaded provided that the user has this type
     * of reminder globally enabled in {@code ConfigureRemindersDialog}, and the
     * relevant contact method (e-mail or phone) has been verified.
     */
    public void loadStartupReminders()
    {
        logger.info("Loading startup reminders ...");

        // Grab preferences from the current OS's backing store.
        // This should be the registry in Windows,
        // ~/Library/Preferences/com.apple.java.util.prefs.plist in OS X, and
        // ~/.java/.userPrefs/ in Linux.
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);
        boolean emailVerified = userPreferences.getBoolean("EMAIL_VERIFIED", false);
        boolean phoneVerified = userPreferences.getBoolean("PHONE_VERIFIED", false);

        if (emailRemindersEnabled)
        {
            if (emailVerified)
            {
                loadAllReminders("email");
            }
        }

        if (textRemindersEnabled)
        {
            if (phoneVerified)
            {
                loadAllReminders("text");
            }
        }

        if (voiceRemindersEnabled)
        {
            if (phoneVerified)
            {
                loadAllReminders("voice");
            }
        }

        logger.info("Startup reminders successfully loaded.");
    }


    /**
     * Loads all reminders from the database of the specified type.
     * <p>
     * Called from {@code ConfigureRemindersDialog} when the user
     * enables all reminders of a certain type.
     *
     * @param type  the type of reminder ("email", "text" or "voice")
     */
    public void loadAllReminders(String type)
    {
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);
        boolean emailVerified = userPreferences.getBoolean("EMAIL_VERIFIED", false);
        boolean phoneVerified = userPreferences.getBoolean("PHONE_VERIFIED", false);

        remindersFromDB = daoManager.getSupplementDAO().getSupplementsWithReminders();

        if (remindersFromDB != null)
        {
            for (int i = 0; i < remindersFromDB.size(); i++)
            {
                Supplement supplement = remindersFromDB.get(i);

                if (type.equals("email"))
                {
                    if (supplement.getEmailEnabled() &&
                        emailRemindersEnabled &&
                        emailVerified)
                    {
                        loadEmailReminder(supplement);
                    }
                }

                if (type.equals("text"))
                {
                    if (supplement.getTextEnabled() &&
                        textRemindersEnabled &&
                        phoneVerified)
                    {
                        loadTextReminder(supplement);
                    }
                }

                if (type.equals("voice"))
                {
                    if (supplement.getVoiceEnabled() &&
                        voiceRemindersEnabled &&
                        phoneVerified)
                    {
                        loadVoiceReminder(supplement);
                    }
                }
            }
        }
    }


    /**
     * Loads an e-mail reminder by scheduling a new Quartz job.
     * <p>
     * Each Quartz job is given a specific {@code jobName} and
     * {@code groupName} which enables us to uniquely identify
     * and remove that job if necessary.
     * <p>
     * For example, the {@code Supplement} with {@code suppID} 1
     * will have the following {@code String} as its {@code jobName}:
     * <p>suppID_1_job
     * <p>
     * And if it is an e-mail job, it will have the following
     * {@code String} as its {@code groupName}:
     * <p>email_group
     * <p>
     * Called by {@code loadAllReminders()} on application startup
     * for any {@code Supplement}s requiring an e-mail reminder, and
     * also called by {@code VitaReminderPanel#addSupplement()} and
     * {@code VitaReminderPanel#editSupplement()}.
     *
     * @param supplement  the supplement for which the e-mail reminder
     *                    is being scheduled
     */
    public void loadEmailReminder(Supplement supplement)
    {
        String emailAddress = userPreferences.get("EMAIL_ADDRESS", "");

        String jobName = supplement.getJobName();  // "suppID_1_job"
        String groupName = "email_group";
        String from = System.getenv("VITAREMINDER_EMAIL_NAME");
        String password = System.getenv("VITAREMINDER_EMAIL_PASSWORD");
        String to = emailAddress;
        String subject = "VitaReminder";

        String message = "This is a reminder to take: "
                       + supplement.getSuppName() + ", "
                       + supplement.getFormattedAmount() + " "
                       + supplement.getSuppUnits();

        String body = HtmlGenerator.getHtmlForEmail(message);

        int hour = supplement.getScheduledHourOfDay();
        int minute = supplement.getScheduledMinute();

        JobDetail job = newJob(HtmlEmailJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("from", from)
                .usingJobData("password", password)
                .usingJobData("to", to)
                .usingJobData("subject", subject)
                .usingJobData("body", body)
                .build();

        Trigger trigger = newTrigger()
                .withSchedule(dailyAtHourAndMinute(hour, minute))
                .build();

        // Schedule the job
        try
        {
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            logger.warn("An exception has occured while attempting to schedule a job.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to schedule a job.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Loads a text message reminder by scheduling a new Quartz job.
     * <p>
     * Each Quartz job is given a specific {@code jobName} and
     * {@code groupName} which enables us to uniquely identify
     * and remove that job if necessary.
     * <p>
     * For example, the {@code Supplement} with {@code suppID} 1
     * will have the following {@code String} as its {@code jobName}:
     * <p>suppID_1_job
     * <p>
     * And if it is a text message job, it will have the following
     * {@code String} as its {@code groupName}:
     * <p>text_group
     * <p>
     * Called by {@code loadAllReminders()} on application startup
     * for any {@code Supplement}s requiring a text message reminder, and
     * also called by {@code VitaReminderPanel#addSupplement()} and
     * {@code VitaReminderPanel#editSupplement()}.
     *
     * @param supplement  the supplement for which the text message reminder
     *                    is being scheduled
     */
    public void loadTextReminder(Supplement supplement)
    {
        String jobName = supplement.getJobName();  // "suppID_1_job"
        String groupName = "text_group";
        String phoneNumber = userPreferences.get("PHONE_NUMBER", "");

        String amountString = supplement.getFormattedAmount();
        String message = "This is a reminder to take "
                       + supplement.getSuppName() + ", "
                       + amountString + " "
                       + supplement.getSuppUnits();

        int hour = supplement.getScheduledHourOfDay();
        int minute = supplement.getScheduledMinute();

        JobDetail job = newJob(TextMessageJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("phoneNumber", phoneNumber)
                .usingJobData("message", message)
                .build();

        Trigger trigger = newTrigger()
                .withSchedule(dailyAtHourAndMinute(hour, minute))
                .build();

        // Schedule the job
        try
        {
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            logger.warn("An exception has occurred while attempting to schedule a job.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to schedule a job.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Loads an automated voice reminder by scheduling a new Quartz job.
     * <p>
     * Each Quartz job is given a specific {@code jobName} and
     * {@code groupName} which enables us to uniquely identify
     * and remove that job if necessary.
     * <p>
     * For example, the {@code Supplement} with {@code suppID} 1
     * will have the following {@code String} as its {@code jobName}:
     * <p>suppID_1_job
     * <p>
     * And if it is an automated voice job, it will have the following
     * {@code String} as its {@code groupName}:
     * <p>voice_group
     * <p>
     * Called by {@code loadAllReminders()} on application startup
     * for any {@code Supplement}s requiring an automated reminder, and
     * also called by {@code VitaReminderPanel#addSupplement()} and
     * {@code VitaReminderPanel#editSupplement()}.
     *
     * @param supplement  the supplement for which the automated voice reminder
     *                    is being scheduled
     */
    public void loadVoiceReminder(Supplement supplement)
    {
        String jobName = supplement.getJobName();  // "suppID_1_job"
        String groupName = "voice_group";
        String phoneNumber = userPreferences.get("PHONE_NUMBER", "");

        String amountString = supplement.getFormattedAmount();
        String message = "Hello, this is a reminder to take "
                       + supplement.getSuppName() + ", "
                       + amountString + " "
                       + supplement.getSuppUnits();

        int hour = supplement.getScheduledHourOfDay();
        int minute = supplement.getScheduledMinute();

        JobDetail job = newJob(VoiceMessageJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("phoneNumber", phoneNumber)
                .usingJobData("message", message)
                .build();

        Trigger trigger = newTrigger()
                .withSchedule(dailyAtHourAndMinute(hour, minute))
                .build();

        // Schedule the job
        try
        {
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            logger.warn("An exception has occurred while attempting to schedule a job.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to schedule a job.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Removes the specified reminder (Quartz job) with the specified
     * supplement ID and type.
     * <p>
     * Valid types are:
     * <ul>
     * <li>"email"</li>
     * <li>"text"</li>
     * <li>"voice"</li>
     * </ul>
     * <p>
     * Called by {@code VitaReminderPanel#deleteSupplement()}
     * and {@code VitaReminderPanel#editSupplement()}.
     *
     * @param suppID  the supplement ID of the supplement for which this job
     *                is scheduled
     * @param type  the type of reminder to remove
     */
    public void unloadReminder(int suppID, String type)
    {
        String jobName = "suppID_" + suppID + "_job";
        String groupName = type + "_group";

        try
        {
            // Delete the job and all of its triggers
            scheduler.deleteJob(jobKey(jobName, groupName));
        }
        catch (SchedulerException e)
        {
            logger.warn("An exception has occurred while attempting to delete a job.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to delete a job.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Removes all active reminders (Quartz jobs) of the specified type.
     * Called by {@code ConfigureRemindersDialog} when the user globally
     * disables reminders of a specific type.
     * <p>
     * Valid types are:
     * <ul>
     * <li>"email"</li>
     * <li>"text"</li>
     * <li>"voice"</li>
     * </ul>
     *
     * @param type  the type of reminder to remove
     */
    public void unloadActiveReminders(String type)
    {
        String jobName = "";

        String groupName = type + "_group";

        try
        {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)))
            {
                jobName = jobKey.getName();

                // Delete the job and all of its triggers
                scheduler.deleteJob(jobKey(jobName, groupName));
            }
        }
        catch (SchedulerException e)
        {
            logger.warn("An exception has occurred while attempting to delete a job.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to delete a job.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Shuts down Quartz Scheduler.  It shuts down immediately, and does
     * not wait for any currently executing jobs to finish.
     * <p>
     * Called by the shutdown hook in {@code VitaReminderApp}.
     */
    public void shutdownScheduler()
    {
        try
        {
            scheduler.shutdown(false);  // false = do not wait for executing jobs to finish

            logger.info("Quartz Scheduler shut down.");
        }
        catch (SchedulerException e)
        {
            logger.warn("An exception has occurred while attempting to shut down the scheduler.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to shut down the scheduler.",
                                          "Scheduler Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

}  // end class ReminderManager
