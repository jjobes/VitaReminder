package com.vitareminder.business;

import java.io.Serializable;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A {@code Supplement} represents any nutritional supplement, vitamin or
 * medicine that the user wants to keep track of. It contains fields for the
 * supplement's basic characteristics such as its name, amount, units, time to
 * take and notes. It also contains {@code boolean} fields that specify whether
 * or not the user wants to receive a particular kind of reminder to take this
 * supplement.
 */
public class Supplement implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int suppID;
    private int regimenID;
    private String suppName;
    private double suppAmount;
    private String suppUnits;
    private java.sql.Time suppTime;
    private boolean emailEnabled;
    private boolean textEnabled;
    private boolean voiceEnabled;
    private String suppNotes;

    private NumberFormat numberFormat = NumberFormat.getNumberInstance();;

    /**
     * The default, no-argument constructor. The default values are:
     * <ul>
     * <li>suppID = 0</li>
     * <li>regimenID = 0</li>
     * <li>suppName = ""</li>
     * <li>suppAmount = 0</li>
     * <li>suppUnits = ""</li>
     * <li>suppTime = null</li>
     * <li>emailEnabled = false</li>
     * <li>textEnabled = false</li>
     * <li>voiceEnabled = false</li>
     * <li>suppNotes = ""</li>
     * </ul>
     */
    public Supplement()
    {
        suppID = 0;
        regimenID = 0;
        suppName = "";
        suppAmount = 0;
        suppUnits = "";
        suppTime = null;
        emailEnabled = false;
        textEnabled = false;
        voiceEnabled = false;
        suppNotes = "";
    }

    /**
     * The multiple-argument constructor.
     *
     * @param suppID
     *            the unique ID for this supplement, which corresponds to its
     *            primary key in the supplements table in the database
     * @param regimenID
     *            the ID of the regimen this supplement is associated with,
     *            which corresponds to the foreign key in the supplements table
     * @param suppName
     *            the user-assigned name for this supplement
     * @param suppAmount
     *            the user-assigned amount for this supplement
     * @param suppUnits
     *            the user-assigned units (e.g., "mg") for this supplement
     * @param suppTime
     *            the user-assigned time for this supplement
     * @param emailEnabled
     *            the user-assigned value that designates whether the user wants
     *            to recieve e-mail reminders
     * @param textEnabled
     *            the user-assigned value that designates whether the user wants
     *            to recieve text message reminders
     * @param voiceEnabled
     *            the user-assigned value that designates whether the user wants
     *            to recieve automated voice reminders
     * @param suppNotes
     *            the user-assigned notes for this supplement
     */
    public Supplement(int suppID, int regimenID, String suppName,
                      double suppAmount, String suppUnits, java.sql.Time suppTime,
                      boolean emailEnabled, boolean textEnabled, boolean voiceEnabled,
                      String suppNotes)
    {
        this.suppID = suppID;
        this.regimenID = regimenID;
        this.suppName = suppName;
        this.suppAmount = suppAmount;
        this.suppUnits = suppUnits;
        this.suppTime = suppTime;
        this.emailEnabled = emailEnabled;
        this.textEnabled = textEnabled;
        this.voiceEnabled = voiceEnabled;
        this.suppNotes = suppNotes;
    }

    public void setSuppID(int suppID)
    {
        this.suppID = suppID;
    }

    public int getSuppID()
    {
        return suppID;
    }

    public void setRegimenID(int regimenID)
    {
        this.regimenID = regimenID;
    }

    public int getRegimenID()
    {
        return regimenID;
    }

    public void setSuppName(String suppName)
    {
        this.suppName = suppName;
    }

    public String getSuppName()
    {
        return suppName;
    }

    public void setSuppAmount(double suppAmount)
    {
        this.suppAmount = suppAmount;
    }

    public double getSuppAmount()
    {
        return suppAmount;
    }

    /**
     * Gets the formatted amount for this {@code Supplement}. The decimal point
     * is removed if it is a whole number, and any commas, if present, are also
     * removed. Fractions, if present, are rounded using the more intuitive
     * {@code RoundingMode.HALF_UP} method.
     * <p>
     * Called by {@code ReminderManager} when building the message
     * {@code String} for the reminder.
     *
     * @return the formatted amount for this {@code Supplement}
     */
    public String getFormattedAmount()
    {
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setGroupingUsed(false);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);

        String amountString = numberFormat.format(suppAmount);

        return amountString;
    }

    public void setSuppUnits(String suppUnits)
    {
        this.suppUnits = suppUnits;
    }

    public String getSuppUnits()
    {
        return suppUnits;
    }

    public void setSuppTime(Time suppTime)
    {
        this.suppTime = suppTime;
    }

    public Time getSuppTime()
    {
        return suppTime;
    }

    /**
     * Gets the formatted time for this {@code Supplement}, in the common
     * 12-hour format (e.g., "7:30 PM"). Called by
     * {@code VitaReminderPanel#editSupplement()} and used to compare two times
     * for equality.
     *
     * @return the formatted time for this {@code Supplement}
     */
    public String getFormattedTime()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a"); // e.g.,
                                                                            // "12:00 AM"
        return simpleDateFormat.format(getSuppTime()).toString();
    }

    public void setEmailEnabled(boolean emailEnabled)
    {
        this.emailEnabled = emailEnabled;
    }

    public boolean getEmailEnabled()
    {
        return emailEnabled;
    }

    public void setTextEnabled(boolean textEnabled)
    {
        this.textEnabled = textEnabled;
    }

    public boolean getTextEnabled()
    {
        return textEnabled;
    }

    public void setVoiceEnabled(boolean voiceEnabled)
    {
        this.voiceEnabled = voiceEnabled;
    }

    public boolean getVoiceEnabled()
    {
        return voiceEnabled;
    }

    public void setSuppNotes(String suppNotes)
    {
        this.suppNotes = suppNotes;
    }

    public String getSuppNotes()
    {
        return suppNotes;
    }

    /**
     * Gets the hour to take this {@code Supplement} in 24-hour format. Called
     * by {@code ReminderManager} when creating the Quartz trigger for each
     * reminder.
     *
     * @return the hour in 24-hour format
     */
    public int getScheduledHourOfDay()
    {
        int hour = -1;

        if (suppTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(suppTime);

            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }

        return hour;
    }

    /**
     * Gets the hour to take this {@code Supplement} in the common 12-hour
     * format. Called by {@code EditSupplementDialog} when setting the
     * {@code hourComboBox} state.
     *
     * @return the hour in 12-hour format
     */
    public int getScheduledHour()
    {
        int hour = -1;

        if (suppTime != null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(suppTime);

            hour = calendar.get(Calendar.HOUR);
        }

        // 12 am and pm are represented by 0 with Calendar.HOUR,
        // so we'll return 12 if that is the case.
        hour = hour == 0 ? 12 : hour;

        return hour;
    }

    public int getScheduledMinute()
    {
        int minute = -1;

        if (suppTime != null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(suppTime);

            minute = calendar.get(Calendar.MINUTE);
        }

        return minute;
    }

    public String getScheduledAmPm()
    {
        String AmPm = "";

        int hour = getScheduledHourOfDay(); // 0 - 23

        if (hour >= 0 && hour <= 11)
        {
            AmPm = "AM";
        }
        else
        {
            AmPm = "PM";
        }

        return AmPm;
    }

    /**
     * Gets the job name to be used when creating the Quartz job. Called by
     * {@code ReminderManager}. The job name is used to uniquely identify the
     * Quartz job, so it can be retrieved and deleted later if necessary.
     *
     * @return the job name associated this this {@code Supplement}.
     */
    public String getJobName()
    {
        String jobName = "suppID_" + suppID + "_job"; // e.g., "suppID_1_job"

        return jobName;
    }

} // end class Supplement
