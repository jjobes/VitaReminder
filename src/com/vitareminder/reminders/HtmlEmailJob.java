package com.vitareminder.reminders;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * A Quartz Job that sends an e-mail.  The {@code JobDataMap} parameters
 * are set in {@code ReminderManager#loadEmailReminder()} and then
 * extracted here to create a new {@code HtmlEmail} object.
 */
public class HtmlEmailJob implements Job
{
    /**
     * All Quartz jobs must have a no-argument constructor.
     */
    public HtmlEmailJob()
    {

    }


    /**
     * Executes this {@code HtmlEmailJob} by sending the actual e-mail.
     * It extracts the job parameters that we placed in the {@code JobDataMap}
     * when we created the job in {@code ReminderManager}.  It then uses
     * these parameters to construct a new {@code HtmlEmail} object, and calls
     * {@code HtmlEmail#send()}.
     * <p>
     * Called automatically by Quartz scheduler at the time that is set in this
     * job's trigger.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String from = dataMap.getString("from");
        String password = dataMap.getString("password");
        String to = dataMap.getString("to");
        String subject = dataMap.getString("subject");
        String body = dataMap.getString("body");

        HtmlEmail email = new HtmlEmail(from, password, to, subject, body);

        email.send();
    }

}  // end class HtmlEmailJob
