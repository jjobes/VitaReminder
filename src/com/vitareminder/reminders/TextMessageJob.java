package com.vitareminder.reminders;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * A Quartz Job that sends a text message.  The {@code JobDataMap}
 * parameters are set in {@code ReminderManager#loadTextReminder()}
 * and then extracted here to create a new {@code TextMessage} object.
 */
public class TextMessageJob implements Job
{
    /**
     * All Quartz jobs must have a no-argument constructor.
     */
    public TextMessageJob()
    {

    }


    /**
     * Executes this {@code TextMessageJob} by sending the actual text message.
     * It extracts the job parameters that we placed in the {@code JobDataMap}
     * when we created the job (see {@code ReminderManager}).  It then uses
     * these parameters to construct a new {@code TextMessage} object, and calls
     * {@code TextMessage#send()}.
     * <p>
     * Called automatically by Quartz scheduler at the time that is set in this
     * job's trigger.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String phoneNumber = dataMap.getString("phoneNumber");
        String message = dataMap.getString("message");

        TextMessage text = new TextMessage(phoneNumber, message);

        text.send();
    }

}  // end class TextMessageJob
