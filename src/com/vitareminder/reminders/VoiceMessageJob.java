package com.vitareminder.reminders;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * A Quartz Job that sends an automated voice message.  The {@code JobDataMap}
 * parameters are set in {@code ReminderManager#loadVoiceReminder()}
 * and then extracted here to create a new {@code VoiceMessage} object.
 */
public class VoiceMessageJob implements Job
{
    /**
     * All Quartz jobs must have a no-argument constructor.
     */
    public VoiceMessageJob()
    {

    }


    /**
     * Executes this {@code VoiceMessageJob} by sending the actual text message.
     * It extracts the job parameters that we placed in the {@code JobDataMap}
     * when we created the job in {@code ReminderManager}).  It then uses
     * these parameters to construct a new {@code VoiceMessage} object, and calls
     * {@code VoiceMessage#send()}.
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

        VoiceMessage voiceMessage = new VoiceMessage(phoneNumber, message);

        voiceMessage.send();
    }

}  // end class VoiceMessageJob
