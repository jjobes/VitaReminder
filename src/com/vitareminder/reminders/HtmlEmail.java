package com.vitareminder.reminders;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;


/**
 * A MIME e-mail that contains HTML that is sent over TLS. The
 * {@code body} argument that is passed in to the constructor
 * is a {@code String} that contains an HTML document that was
 * generated using Velocity.
 */
public class HtmlEmail
{
    private String from;
    private String password;
    private String to;
    private String subject;
    private String body;
    private String host = "smtp.gmail.com";

    // This line pretty much sums up Java.
    private Logger logger = Logger.getLogger(HtmlEmail.class);


    /**
     * The sole constructor.
     *
     * @param from  the username, not including "@gmail.com"
     * @param password  the password for the sending account
     * @param to  the recipient's e-mail address
     * @param subject  the e-mail subject
     * @param body  the e-mail body
     */
    public HtmlEmail(String from, String password, String to,
                     String subject, String body)
    {
        this.from = from;
        this.password = password;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }


    /**
     * Sends the e-mail with an HTML body over TLS.
     */
    public void send()
    {
        Properties properties = System.getProperties();

        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.user", from);
        properties.put("mail.smtp.password", password);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties);

        final MimeMessage message = new MimeMessage(session);

        try
        {
            message.setFrom(new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);
            message.addRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(subject);
            message.setContent(body, "text/html");

            final Transport transport = session.getTransport("smtp");

            // Send the e-mail in its own thread, as the transport process
            // can take a couple of seconds, and we don't want to freeze
            // the Swing components that are operating on the Event Dispatch
            // Thread.
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    try
                    {
                        transport.connect(host, from, password);
                        transport.sendMessage(message, message.getAllRecipients());
                        transport.close();
                    }
                    catch (MessagingException e)
                    {
                        logger.warn("An error has occured sending e-mail.", e);
                        JOptionPane.showMessageDialog(null,
                                                  "<html>Sorry, an error has occurred while attempting to send the e-mail.<br><br>"
                                                + "Please ensure that you are connected to the Internet.</html>",
                                                  "E-Mail Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    }
                }
            }).start();
        }
        catch (AddressException e)
        {
            logger.warn("An error has occured sending e-mail.", e);
            JOptionPane.showMessageDialog(null,
                                          "<html>Sorry, an error has occurred while attempting to send the e-mail.<br><br>"
                                        + "Please ensure that you have entered a valid e-mail address.</html>",
                                          "E-Mail Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        catch (MessagingException e)
        {
            logger.warn("An error has occured sending e-mail.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, an error has occurred while attempting to send the e-mail.",
                                          "E-Mail Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

}  // end class HtmlEmail
