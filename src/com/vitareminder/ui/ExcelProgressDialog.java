package com.vitareminder.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vitareminder.business.Regimen;
import com.vitareminder.reports.ExcelWorker;
import com.vitareminder.ui.GBCFactory;


/**
 * Displays a {@code JProgressDialog} while the background thread
 * that creates the Excel document is running.  This class creates
 * that thread and sets a listener on it that waits for the thread
 * to signal that it is done.  At that point, the Excel workbook is
 * retrieved from the thread class, and this workbook is placed in
 * a class variable in the current class so that it can be retrieved
 * later.  The progress dialog is then disposed.
 */
public class ExcelProgressDialog
{
    private JDialog progressDialog;
    private JPanel panel;

    private JLabel label;
    private JProgressBar progressBar;

    private XSSFWorkbook workbook;

    private static Logger logger = Logger.getLogger(ExcelProgressDialog.class);


    /**
     * The sole constructor.  Creates the {@code JDialog} and builds its
     * {@code JPanel} that contains a {@code JLabel} and the {@code JProgressBar}.
     * The {@code JProgressBar} is set to indeterminate, as there is no way
     * of querying the POI subsystem for updates on its progress.
     *
     * @param frame  the owner of this dialog
     * @param regimens  the user's regimens and supplements that are used to generate
     *                  the Excel document
     */
    public ExcelProgressDialog(final JFrame frame, final List<Regimen> regimens)
    {
        progressDialog = new JDialog(frame, true);
        progressDialog.setResizable(false);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(new Dimension(200, 90));

        label = new JLabel("Generating Excel document");

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel = new JPanel(new GridBagLayout());
        panel.add(label, GBCFactory.getConstraints(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        panel.add(progressBar, GBCFactory.getConstraints(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));

        progressDialog.add(panel);
        progressDialog.setLocationRelativeTo(frame);

        final ExcelWorker worker = new ExcelWorker(regimens);

        // Listen in on the state of the thread and wait for it to finish,
        // then retrieve the Excel document.
        worker.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pce)
            {
                if (worker.getState() == StateValue.DONE)
                {
                    try
                    {
                        workbook = worker.get();  // Retrieve the Excel document

                        progressDialog.dispose();
                    }
                    catch (InterruptedException e)
                    {
                        logger.error("An error has occurred while generating the Excel document.", e);
                        JOptionPane.showMessageDialog(frame,
                                "Sorry, an error has occurred while generating the Excel document.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    catch (ExecutionException e)
                    {
                        logger.error("An error has occurred while generating the Excel document.", e);
                        JOptionPane.showMessageDialog(frame,
                                "Sorry, an error has occurred while generating the Excel document.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        worker.execute();

        progressDialog.setVisible(true);
    }


    /**
     * Called from the {@code excelMenuItem}'s {@code ActionListener} in
     * {@code VitaReminderFrame} to retrieve the generated Excel workbook
     * after the thread has completed.
     *
     * @return  the Excel workbook that contains a report of the user's regimens
     *          and supplements
     */
    public XSSFWorkbook getWorkbook()
    {
        return workbook;
    }

}  // end class ExcelProgressDialog
