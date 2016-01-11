package com.vitareminder.reports;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vitareminder.business.Regimen;


/**
 * The {@code SwingWorker} background thread that calls
 * {@code ExcelGenerator.getExcelFile()} and waits for it
 * to return.
 */
public class ExcelWorker extends SwingWorker<XSSFWorkbook, String>
{
    List<Regimen> regimens;


    /**
     * The sole constructor.
     *
     * @param regimens  the regimens and supplements that are passed in
     *                  to {@code ExcelGenerator#getExcelFile()} to generate
     *                  the report
     */
    public ExcelWorker(List<Regimen> regimens)
    {
        this.regimens = regimens;
    }


    /**
     * This method is executed in a background thread and
     * generates the Excel workbook.
     */
    @Override
    protected XSSFWorkbook doInBackground() throws Exception
    {
        Thread.sleep(1000);

        XSSFWorkbook workbook = ExcelGenerator.getExcelFile(regimens);

        return workbook;
    }

}  // end class ExcelWorker
