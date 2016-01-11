package com.vitareminder.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportExcelFileChooser
{
    JFileChooser fileChooser;

    private final JFrame frame;
    private XSSFWorkbook workbook;

    private Logger logger = Logger.getLogger(ExportExcelFileChooser.class);

    @SuppressWarnings("serial")
    public ExportExcelFileChooser(final JFrame frame, XSSFWorkbook workbook)
    {
        this.frame = frame;
        this.workbook = workbook;

        fileChooser = new JFileChooser() {

            @Override
            public void approveSelection()
            {
                File f = getSelectedFile();
                if (f.exists())
                {
                    String[] options = {"YES", "NO"};
                    int result = JOptionPane.showOptionDialog(frame,
                            "This file already exists.\nDo you want to replace it?",
                            "File already exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[1]);

                    switch (result)
                    {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CLOSED_OPTION:
                        return;
                    }
                }

                super.approveSelection();
            }
        };

        fileChooser.setDialogTitle("Export to Excel");

        // Build the default file name
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date();
        String fileName = "VitaReminder_Report_" + dateFormat.format(date) + ".xlsx";

        fileChooser.setSelectedFile(new File(fileName));

        // Set the file filter to only display .xlsx files
        FileFilter filter = new FileNameExtensionFilter("Excel spreadsheet (.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);

        display();
    }

    private void display()
    {
        // Display the save dialog and save the file if the user clicks "Save"
        int option = fileChooser.showSaveDialog(frame);

        if (option == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File file = fileChooser.getSelectedFile();

                String filePath = file.getPath();

                FileOutputStream fileOut = null;

                if (filePath.matches(".*xlsx"))
                {
                    fileOut = new FileOutputStream(filePath);
                }
                else
                {
                    fileOut = new FileOutputStream(filePath + ".xlsx");
                }

                // Save the actual file to disk
                workbook.write(fileOut);

                fileOut.close();
            }
            catch (FileNotFoundException fnfe)
            {
                logger.error("An error has occurred while saving the Excel document.", fnfe);
                JOptionPane.showMessageDialog(frame,
                        "Sorry, an error has occurred while saving the Excel document.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException ioe)
            {
                logger.error("An error has occurred while saving the Excel document.", ioe);
                JOptionPane.showMessageDialog(frame,
                        "Sorry, an error has occurred while saving the Excel document.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}  // end class ExportExcelFileChooser
