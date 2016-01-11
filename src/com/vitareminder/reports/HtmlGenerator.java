package com.vitareminder.reports;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.vitareminder.business.Regimen;


/**
 * This class uses the Velocity templating engine to generate HTML documents
 * for physical printed reports, e-mail reminders, displaying the content
 * of the {@code AboutDialog}, displaying system information and displaying
 * all active reminders.  It uses five pre-defined templates located in the
 * resources/templates directory:
 * <ul>
 * <li>{@code getHtmlForReport()} uses resources/templates/PrintableReportTemplate.vm</li>
 * <li>{@code getHtmlForEmail()} uses resources/templates/EmailTemplate.vm</li>
 * <li>{@code getHtmlForAboutDialog()} uses resources/templates/AboutTemplate.vm</li>
 * <li>{@code getHtmlForSystemInformation()} uses resources/templates/SystemInformationTemplate.vm</li>
 * <li>{@code getHtmlForActiveReminders()} uses resources/templates/ShowActiveRemindersTemplate.vm</li>
 * </ul>
 */
public class HtmlGenerator
{
    private static VelocityEngine velocityEngine = new VelocityEngine();

    static
    {
        velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        velocityEngine.init();
    }


    /**
     * Gets a {@code String} containing an HTML document containing a report to send
     * to the printer.  The report displays each regimen as a table that contains its
     * associated supplements.  Called by the {@code printMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame}.
     *
     * @param regimens  A {@code List} of all of the user's {@code Regimen}s that is merged
     *                  with the Velocity template to populate the HTML tables
     * @return the HTML report
     */
    public static String getHtmlForReport(List<Regimen> regimens)
    {
        Template template = velocityEngine.getTemplate("res/templates/PrintableReportTemplate.vm");

        VelocityContext context = new VelocityContext();
        context.put("regimensList", regimens);

        StringWriter writer = new StringWriter();

        // Merge the template with the context (which contains our regimens data)
        // and send the output to writer.
        template.merge(context, writer);

        // Return the resulting HTML string
        return writer.toString();
    }

    /**
     * Gets a {@code String} containing an HTML document to send as an e-mail reminder.
     * Called by {@code ReminderManager#loadEmailReminder()}.
     *
     * @param message  the message to be merged with the Velocity template and displayed
     *                 in the body of the HTML document
     * @return the HTML document for the e-mail reminder
     */
    public static String getHtmlForEmail(String message)
    {
        Template template = velocityEngine.getTemplate("res/templates/EmailTemplate.vm");

        VelocityContext context = new VelocityContext();
        context.put("message", message);

        StringWriter writer = new StringWriter();

        template.merge(context, writer);

        return writer.toString();
    }


    /**
     * Gets a {@code String} containing an HTML document used with the {@code AboutDialog}.
     * The {@code AboutDialog} places this {@code String} into a {@code JLabel}.
     *
     * @param contextParams  the parameters to be merged with the Velocity template
     * @return the HTML document for the {@code AboutDialog}
     */
    public static String getHtmlForAboutDialog(Map<String, Object> contextParams)
    {
        Template template = velocityEngine.getTemplate("res/templates/AboutTemplate.vm");

        VelocityContext context = new VelocityContext(contextParams);

        StringWriter writer = new StringWriter();

        template.merge(context, writer);

        return writer.toString();
    }


    /**
     * Gets a {@code String} containing an HTML document used with the {@code SystemInformationDialog}.
     * The {@code SystemInformationDialog} places this {@code String} into a {@code JLabel}.
     *
     * @param contextParams  the parameters to be merged with the Velocity template
     * @return the HTML document for the {@code SystemInformationDialog}
     */
    public static String getHtmlForSystemInformation(Map<String, Object> contextParams)
    {
        Template template = velocityEngine.getTemplate("res/templates/SystemInformationTemplate.vm");

        VelocityContext context = new VelocityContext(contextParams);

        StringWriter writer = new StringWriter();

        template.merge(context, writer);

        return writer.toString();
    }


    /**
     * Gets a {@code String} containing an HTML document used with the {@code ShowActiveRemindersDialog}.
     * The {@code ShowActiveRemindersDialog} places this {@code String} into a {@code JLabel}.
     *
     * @param regimens  the list of {@code Regimen}s that contains {@code Supplement}s with active reminders
     * @return the HTML document for the {@code ShowActiveRemindersDialog}
     */
    public static String getHtmlForActiveReminders(List<Regimen> regimens)
    {
        Template template = velocityEngine.getTemplate("res/templates/ShowActiveRemindersTemplate.vm");

        VelocityContext context = new VelocityContext();
        context.put("regimensList", regimens);

        StringWriter writer = new StringWriter();

        template.merge(context, writer);

        return writer.toString();
    }

}  // end class HtmlGenerator
