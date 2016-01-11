package com.vitareminder.reports;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;


/**
 * A class that is used to print an HTML document.
 */
public class Printer
{
    private static Logger logger = Logger.getLogger(Printer.class);

    /**
     * Prints an HTML document.  Presents the user with a cross-platform
     * {@code PrintDialog}.  Called by the {@code printMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame}.
     *
     * @param htmlString  the HTML {@code String} to be printed
     */
    public static void printHtml(String htmlString)
    {
        MessageFormat header = new MessageFormat("");
        MessageFormat footer = new MessageFormat("");

        PrinterJob printerJob = PrinterJob.getPrinterJob();

        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();

        if (printerJob.printDialog(attributeSet))
        {
            JEditorPane htmlEditorPane = new JEditorPane("text/html", "");

            try
            {
                htmlEditorPane.read(new BufferedReader(new StringReader(htmlString)), "");
            }
            catch (FileNotFoundException e)
            {
                logger.warn("An error has occurred while printing.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, an error has occurred while printing.",
                                              "Print Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException e)
            {
                logger.warn("An error has occurred while printing.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, an error has occurred while printing.",
                                              "Print Error",
                                              JOptionPane.ERROR_MESSAGE);
            }

            htmlEditorPane.repaint();

            printerJob.setPrintable(htmlEditorPane.getPrintable(header, footer));

            try
            {
                printerJob.print();
            }
            catch (PrinterException e)
            {
                logger.warn("An error has occurred while printing.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, an error has occurred while printing.",
                                              "Print Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}  // end class Printer
