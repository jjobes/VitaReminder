package com.vitareminder.reports;

import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vitareminder.business.Regimen;
import com.vitareminder.business.Supplement;

/**
 * This class defines a single static method that generates an Excel
 * spreadsheet workbook using the Apache POI API.  The Excel workbook
 * that is generated can be saved as an Excel 2007 .xlsx file.
 */
public class ExcelGenerator
{
    private static XSSFWorkbook workbook = new XSSFWorkbook();

    private static XSSFSheet sheet = workbook.createSheet("VitaReminder Report");

    private static XSSFFont titleFont = null;
    private static XSSFFont regimenNameFont = null;
    private static XSSFFont supplementHeaderFont = null;
    private static XSSFFont supplementRowFont = null;
    private static XSSFFont notesFont = null;

    private static XSSFCellStyle titleStyle = null;
    private static XSSFCellStyle regimenNameStyle = null;
    private static XSSFCellStyle regimenNotesStyle = null;
    private static XSSFCellStyle supplementHeaderStyle = null;
    private static XSSFCellStyle supplementRowStyle = null;
    private static XSSFCellStyle reminderStyle = null;
    private static XSSFCellStyle supplementNotesStyle = null;

    private static String[] supplementColumns = {"Supplement Name", "Amount",
                                                 "Units", "Take At", "E-Mail",
                                                 "Text", "Voice", "Notes"};


    /**
     * Generates an Excel workbook report that contains the user's
     * regimens and supplements.
     *
     * @param regimens  a {@code List} of all of the user's {@code Regimen}s, where
     *                  each {@code Regimen} object contains a {@code List} of its
     *                  associated {@code Supplement}s
     * @return a workbook in the Excel 2007 format that contains a formatted
     *         list of the user's regimens and their supplements
     */
    public static XSSFWorkbook getExcelFile(List<Regimen> regimens)
    {
        createFonts();

        createStyles();

        int rowNum = 0;
        Row row = sheet.createRow(rowNum);

        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, supplementColumns.length-1));

        rowNum++;
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, supplementColumns.length-1));

        Cell cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("VitaReminder Report");

        rowNum++;

        for (Regimen r : regimens)
        {
            row = sheet.createRow(rowNum);

            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, supplementColumns.length-1));

            // Regimen name
            cell = row.createCell(0);
            cell.setCellStyle(regimenNameStyle);
            cell.setCellValue(r.getRegimenName());

            // Display regimen notes if present
            if (!r.getRegimenNotes().isEmpty())
            {
                rowNum++;
                row = sheet.createRow(rowNum);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, supplementColumns.length-1));

                cell = row.createCell(0);
                cell.setCellStyle(regimenNotesStyle);
                cell.setCellValue(r.getRegimenNotes());

                rowNum++;
                row = sheet.createRow(rowNum);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, supplementColumns.length-1));
            }

            // New row for headers
            rowNum++;
            row = sheet.createRow(rowNum);

            int cellNum = 0;

            for (String columnName : supplementColumns)
            {
                cell = row.createCell(cellNum++);
                cell.setCellStyle(supplementHeaderStyle);
                cell.setCellValue(columnName);
            }

            for (Supplement s : r.getSupplements())
            {
                // Create the new row for this supplement
                rowNum++;
                row = sheet.createRow(rowNum);

                // Supplement Name
                cellNum = 0;
                cell = row.createCell(cellNum);
                cell.setCellStyle(supplementRowStyle);
                cell.setCellValue(s.getSuppName());

                // Amount
                cellNum++;
                cell = row.createCell(cellNum);
                cell.setCellStyle(supplementRowStyle);
                cell.setCellValue(s.getSuppAmount());

                // Units
                cellNum++;
                cell = row.createCell(cellNum);
                cell.setCellStyle(supplementRowStyle);
                cell.setCellValue(s.getSuppUnits());

                // Take at
                cellNum++;
                cell = row.createCell(cellNum);
                cell.setCellStyle(supplementRowStyle);
                cell.setCellValue(s.getFormattedTime());

                // E-Mail
                cellNum++;
                cell = row.createCell(cellNum);
                if (s.getEmailEnabled())
                {
                    cell.setCellStyle(reminderStyle);
                    cell.setCellValue("x");
                }
                else
                {
                    cell.setCellStyle(reminderStyle);
                    cell.setCellValue("");
                }

                // Text
                cellNum++;
                cell = row.createCell(cellNum);
                if (s.getTextEnabled())
                {
                    cell.setCellStyle(reminderStyle);
                    cell.setCellValue("x");
                }
                else
                {
                    cell.setCellStyle(reminderStyle);
                    cell.setCellValue("");
                }

                // Voice
                cellNum++;
                cell = row.createCell(cellNum);
                if (s.getVoiceEnabled())
                {
                    cell.setCellStyle(reminderStyle);
                    cell.setCellValue("x");
                }
                else
                {
                    cell.setCellStyle(reminderStyle);
                    cell.setCellValue("");
                }

                // Notes
                cellNum++;

                cell = row.createCell(cellNum);
                cell.setCellStyle(supplementNotesStyle);
                cell.setCellValue(s.getSuppNotes());
            }

            // Create extra space between regimens
            rowNum++;
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, supplementColumns.length-1));
            rowNum++;
        }

        // Make sure the column widths are automatically sized to fit the cell contents
        for (int i = 0; i < supplementColumns.length; i++)
        {
          sheet.autoSizeColumn(i, false);
        }

        sheet.setHorizontallyCenter(true);

        return workbook;
    }


    private static void createFonts()
    {
        // The font used for the main report title
        titleFont = workbook.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        titleFont.setBold(true);
        titleFont.setItalic(false);

        // The font used for the regimen name cells
        regimenNameFont = workbook.createFont();
        regimenNameFont.setFontName("Arial");
        regimenNameFont.setFontHeightInPoints((short) 12);
        regimenNameFont.setColor(IndexedColors.BLACK.getIndex());
        regimenNameFont.setBold(true);
        regimenNameFont.setItalic(false);

        // The font used for the supplement header cells
        supplementHeaderFont = workbook.createFont();
        supplementHeaderFont.setFontHeightInPoints((short) 10);
        supplementHeaderFont.setFontName("Arial");
        supplementHeaderFont.setColor(IndexedColors.BLACK.getIndex());
        supplementHeaderFont.setBold(true);
        supplementHeaderFont.setItalic(false);

        // The font used for the normal supplement cells
        supplementRowFont = workbook.createFont();
        supplementRowFont.setFontHeightInPoints((short) 10);
        supplementRowFont.setFontName("Arial");
        supplementRowFont.setColor(IndexedColors.BLACK.getIndex());
        supplementRowFont.setBold(false);
        supplementRowFont.setItalic(false);

        // The font used for both the regimen and supplement notes
        notesFont = workbook.createFont();
        notesFont.setFontHeightInPoints((short) 10);
        notesFont.setFontName("Arial");
        notesFont.setColor(IndexedColors.BLACK.getIndex());
        notesFont.setBold(false);
        notesFont.setItalic(true);
    }


    private static void createStyles()
    {
        // The style used by the main report title cell
        titleStyle = workbook.createCellStyle();
        titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);

        // The style used by the regimen name cells
        regimenNameStyle = workbook.createCellStyle();
        regimenNameStyle = workbook.createCellStyle();
        regimenNameStyle.setFont(regimenNameFont);
        regimenNameStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);

        // The style used by the regimen notes cells
        regimenNotesStyle = workbook.createCellStyle();
        regimenNotesStyle = workbook.createCellStyle();
        regimenNotesStyle.setFont(notesFont);
        regimenNotesStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);

        // The style used by the supplement headers
        supplementHeaderStyle = workbook.createCellStyle();
        supplementHeaderStyle.setFont(supplementHeaderFont);
        supplementHeaderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);
        supplementHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        supplementHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        supplementHeaderStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        supplementHeaderStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        supplementHeaderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        supplementHeaderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

        // The style used by the normal supplement cells
        supplementRowStyle = workbook.createCellStyle();
        supplementRowStyle.setFont(supplementRowFont);
        supplementRowStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        supplementRowStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        supplementRowStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        supplementRowStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

        // The style used by the E-Mail, Text and Voice reminder cells
        reminderStyle = workbook.createCellStyle();
        reminderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);
        reminderStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        reminderStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        reminderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        reminderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);

        // The style used by the supplement notes cells
        supplementNotesStyle = workbook.createCellStyle();
        supplementNotesStyle.setFont(notesFont);
        supplementNotesStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        supplementNotesStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        supplementNotesStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        supplementNotesStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
    }

}  // end class ExcelGenerator
