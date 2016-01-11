package com.vitareminder.ui;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vitareminder.business.Regimen;
import com.vitareminder.dao.DAOManager;
import com.vitareminder.reminders.ReminderManager;
import com.vitareminder.reports.HtmlGenerator;
import com.vitareminder.reports.Printer;


/**
 * The application's main window.  It creates and displays the menu bar and the System
 * Tray menu.  It listens for the closing of this frame and displays a "save" option pane
 * if unsaved changes exist.  It also creates the {@code VitaReminderPanel} object that
 * sits inside of this frame and that contains the visible components.
 */
public class VitaReminderFrame
{
    private JFrame frame;
    private VitaReminderPanel panel;
    private List<Regimen> regimens;
    private DAOManager daoManager;
    private ReminderManager reminderManager;
    private String operatingSystem;
    private boolean trayIconCreated = false;
    private JMenuItem saveMenuItem;

    private boolean isLinux = false;
    private boolean isMac = false;
    private boolean isWindows = false;

    private Logger logger = Logger.getLogger(VitaReminderFrame.class);


    /**
     * The constructor calls {@code createFrame()} to create the actual frame.
     * It detects the current OS to see if the application is running on Linux,
     * and if so it sets the application name that should be displayed in the
     * menu bar.  If the OS is Mac, it places the menu bar that is normally tied
     * to the top of the {@code JFrame} and places it outside of the application
     * window and onto the shared menu at the top of the screen, to adhere to Mac
     * OS standards.  It also sets the Mac display name that appears in the upper
     * left-hand corner of the screen.
     *
     * @param daoManager  represents the DAO layer and is passed into the panel constructor
     * @param reminderManager  manages all Quartz Scheduler jobs and is passed into the panel
     *                         constructor
     */
    public VitaReminderFrame(DAOManager daoManager, ReminderManager reminderManager)
    {
        this.daoManager = daoManager;
        this.reminderManager = reminderManager;

        operatingSystem = System.getProperty("os.name");

        logger.info("Operating System detected: " + operatingSystem);

        if (operatingSystem.startsWith("Linux"))
        {
            isLinux = true;

            // Fix the application name that is associated with the application's icon in X11.
            // By default, X11 will display the package and class name for the application's
            // main class.  X11 sets this value based on the WM_CLASS property of the window.
            // The following code overrides the value that X11 uses for WM_CLASS.
            try
            {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                java.lang.reflect.Field awtAppClassNameField = toolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(toolkit, "VitaReminder");
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                logger.error("An error occurred while attempting to set WM_CLASS name in Linux", e);
            }
        }

        if (operatingSystem.startsWith("Mac"))
        {
            isMac = true;

            // Place the JMenuBar on the Mac menu bar at the top of the screen
            // instead of inside the application's window
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Set the application's main menu name
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VitaReminder");
        }

        if (operatingSystem.startsWith("Windows"))
        {
            isWindows = true;
        }

        createFrame();

        if (isWindows)
        {
            if (SystemTray.isSupported())
            {
                createSystemTrayMenu();
            }
        }
    }


    /**
     * Creates the {@code JFrame} that represents the application's main window.
     * It also listens for a window closing event.  On a window closing event,
     * if the tray icon was created, the frame will be set invisible, but if the
     * tray icon was not created, the application will exit on this event.  In this
     * case, if there are unsaved changes, the user will be prompted to save their
     * changes before the application exits.
     */
    private void createFrame()
    {
        setLookAndFeel();

        frame = new JFrame();

        createMenuBar();

        setFrameIcon();

        // Read regimens from the database and store in regimens
        regimens = daoManager.getRegimenDAO().getRegimens();

        // regimens is passed into panel then passed into the table models
        panel = new VitaReminderPanel(frame, saveMenuItem, regimens,
                                      daoManager, reminderManager);

        frame.setTitle("VitaReminder");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(panel);

        // X11 tends to be very slow to calculate the sizes of the components
        // that is involved with a call to frame.pack(), so we'll manually
        // set the frame size on Linux.
        if (isLinux)
        {
            frame.setSize(new Dimension(850, 550));
        }
        else
        {
            frame.pack();
        }

        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e)
            {
                if (trayIconCreated)
                {
                    frame.setVisible(false);
                }
                else
                {
                    panel.saveColumnWidths();
                    panel.saveColumnOrder();

                    if (panel.hasUnsavedChanges())
                    {
                        displaySaveChangesOptionPane();
                    }
                    else
                    {
                        System.exit(0);
                    }
                }
            }
        });

        frame.setVisible(true);
    }


    /**
     * This method creates the {@code TrayIcon} and its {@code PopupMenu}.
     * The {@code MenuItem}s allow the user to hide or show the main
     * {@code VitaReminderFrame}, display the {@code AboutDialog}, or to
     * exit the application.
     */
    private void createSystemTrayMenu()
    {
        TrayIcon trayIcon = null;

        trayIcon = new TrayIcon(createImage("resources/icons/tray_icon_16x16.png",
                                            "VitaReminder tray icon"));

        trayIcon.setImageAutoSize(true);

        MenuItem hideShowItem = new MenuItem("Hide/Show VitaReminder");
        hideShowItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (frame.getExtendedState() == Frame.ICONIFIED)
                {
                    frame.setExtendedState(Frame.NORMAL);
                    frame.setVisible(true);
                }
                else if (!frame.isVisible())
                {
                    frame.setVisible(true);
                }
                else
                {
                    frame.setVisible(false);
                }
            }
        });

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new AboutDialog(frame);
            }
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                panel.saveColumnOrder();
                panel.saveColumnWidths();

                if (panel.hasUnsavedChanges())
                {
                    displaySaveChangesOptionPane();
                }
                else
                {
                    System.exit(0);
                }
            }
        });

        final PopupMenu popup = new PopupMenu();
        popup.add(hideShowItem);
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        SystemTray tray = SystemTray.getSystemTray();
        try
        {
            tray.add(trayIcon);
            trayIconCreated = true;
        }
        catch (AWTException e)
        {
            logger.error("An error occurred while attempting to add the tray icon.", e);
        }
    }


    /**
     * This method creates the {@code Image} that is needed to create the
     * {@code TrayIcon}. It is called by {@code VitaReminderFrame#createSystemTrayMenu()}.
     *
     * @param path  the relative path to the .png icon resource
     * @param description  used by the javax.accessibility API for visually impared users
     * @return An {@code Image} used to create the {@code TrayIcon}
     */
    private Image createImage(String path, String description)
    {
        URL imageURL = getClass().getResource(path);

        if (imageURL == null)
        {
            System.err.println("Resource not found: " + path);
            return null;
        }
        else
        {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }


    /**
     * Sets the Swing Look & Feel (L&F) depending on the current OS. On
     * Mac OS X, set the L&F to the native one.  On any other OS, set it
     * to Nimbus, which should succeed assuming Java 1.6 update 10 is available
     * on the host machine.  If it is available, we have to manually set the
     * {@code Table.showGrid} parameter to true, as Nimbus does not by default
     * show grid lines in {@code JTable}s.  If either of these L&F's fails to
     * load, it sets the L&F to "Metal".
     */
    private void setLookAndFeel()
    {
        if (isMac)
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (ClassNotFoundException | InstantiationException |
                   IllegalAccessException | UnsupportedLookAndFeelException e)
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                }
                catch (ClassNotFoundException | InstantiationException |
                       IllegalAccessException | UnsupportedLookAndFeelException e1)
                {
                    logger.warn("An error occurred while setting the Look & Feel.", e1);
                }
            }
        }
        else
        {
            // For Windows and Linux, try to set the L&F to Nimbus
            try
            {
                // The Nimbus L&F JProgressBar looks odd, so before setting the L&F to Nimbus,
                // copy in the JProgressBar defaults.
                HashMap<Object, Object> progressDefaults = new HashMap<>();

                for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet())
                {
                    if (entry.getKey().getClass() == String.class && ((String) entry.getKey()).startsWith("ProgressBar"))
                    {
                        progressDefaults.put(entry.getKey(), entry.getValue());
                    }
                }

                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                {
                    if (info.getName().equals("Nimbus"))
                    {
                        UIManager.setLookAndFeel(info.getClassName());
                        UIManager.put("Table.showGrid", true);
                    }
                }

                // After setting the L&F to Nimbus, set the JProgressBar to its default look:
                for (Map.Entry<Object, Object> entry : progressDefaults.entrySet())
                {
                    UIManager.getDefaults().put(entry.getKey(), entry.getValue());
                }
            }
            catch (Exception e)
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                }
                catch (ClassNotFoundException | InstantiationException |
                       IllegalAccessException | UnsupportedLookAndFeelException e1)
                {
                    logger.warn("An error occurred while setting the Look & Feel.", e1);
                }
            }
        }
    }


    /**
     * This method creates the {@code JMenuBar} for the application as well
     * as each {@code JMenuItem}'s event handler.  The {@code saveMenuItem}
     * commits the current database transaction.  The {@code printMenuItem} uses
     * the {@code HtmlGenerator} and Velocity to generate a table-based report
     * to send to the printer.  The {@code exitMenuItem} exits the application and
     * prompts the user to save any unsaved changes.  The {@code configureRemindersMenuItem}
     * displays a {@code ConfigureRemindersDialog} that enables the user to set global
     * reminder and contact settings.  The {@code aboutMenuItem} displays an
     * {@code AboutDialog} with general program information.
     */
    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        ImageIcon saveIcon = new ImageIcon(getClass().getResource("resources/icons/save_icon_16x16.png"));

        saveMenuItem = new JMenuItem("Save", saveIcon);
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                daoManager.commitTransaction();
                saveMenuItem.setEnabled(false);

                panel.setSaveButtonEnabled(false);
                panel.setUnsavedChangesExist(false);
            }
        });
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        ImageIcon printIcon = new ImageIcon(getClass().getResource("resources/icons/print_icon_16x16.png"));
        JMenuItem printMenuItem = new JMenuItem("Print", printIcon);
        printMenuItem.setMnemonic(KeyEvent.VK_P);
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        printMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String htmlString = HtmlGenerator.getHtmlForReport(regimens);

                Printer.printHtml(htmlString);
            }
        });
        fileMenu.add(printMenuItem);

        fileMenu.addSeparator();

        ImageIcon importIcon = new ImageIcon(getClass().getResource("resources/icons/import_icon_16x16.png"));
        JMenuItem importMenuItem = new JMenuItem("Import", importIcon);
        importMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String[] options = {"YES", "NO"};
                int result = JOptionPane.showOptionDialog(frame,
                        "Importing will overwrite your current data.\n" +
                        "Would you like to save your data before proceeding?",
                        "Overwrite Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (result == JOptionPane.YES_OPTION)
                {
                    // First allow the user to save their current data

                    if (panel.hasUnsavedChanges())
                    {
                        daoManager.commitTransaction();
                        panel.setUnsavedChangesExist(false);
                    }

                    ExportFileChooser exportChooser = new ExportFileChooser(frame, daoManager);

                    if (exportChooser.getExportSuccess())
                    {
                        JOptionPane.showMessageDialog(frame,
                                "Your backup was successfully saved.",
                                "Backup Saved",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                    else
                    {
                        result = JOptionPane.showOptionDialog(frame,
                                "Your backup was not saved.\n" +
                                "Would you still like to proceed with the import?",
                                "Backup Failure",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                options,
                                options[1]);

                        if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION)
                        {
                            return;
                        }
                    }

                    performImport();
                }
                else if (result == JOptionPane.NO_OPTION)
                {
                    performImport();
                }
                else if (result == JOptionPane.CLOSED_OPTION)
                {
                    return;
                }
            }


            private void performImport()
            {
                ImportFileChooser fileChooser = new ImportFileChooser(frame, daoManager);

                if (fileChooser.getImportSuccess())
                {
                    // The file was successfully imported and the database tables
                    // have been cleared and replaced with the imported data.

                    // Unload all reminders (these reminders were associated with
                    // the old supplements).
                    if (fileChooser.getImportSuccess())
                    {
                        reminderManager.unloadActiveReminders("email");
                        reminderManager.unloadActiveReminders("text");
                        reminderManager.unloadActiveReminders("voice");
                    }

                    // Retrieve the new regimens data structure that is built
                    // from the new database table contents.
                    regimens = daoManager.getRegimenDAO().getRegimens();

                    // Update the table models.  These method calls will inform
                    // the tables that their models have been updated.
                    panel.setRegimenTableModel(regimens);
                    panel.setSupplementTableModel(regimens);

                    panel.selectFirstRegimenRow();

                    saveMenuItem.setEnabled(false);
                    panel.setSaveButtonEnabled(false);

                    panel.setUnsavedChangesExist(false);

                    // Load any reminders that are associated with the newly
                    // imported supplements.
                    reminderManager.loadStartupReminders();

                    JOptionPane.showMessageDialog(frame,
                            "Your data was successfully imported.",
                            "Import Successful",
                            JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        fileMenu.add(importMenuItem);

        ImageIcon exportIcon = new ImageIcon(getClass().getResource("resources/icons/export_icon_16x16.png"));
        JMenuItem exportMenuItem = new JMenuItem("Export", exportIcon);
        exportMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new ExportFileChooser(frame, daoManager);
            }
        });
        fileMenu.add(exportMenuItem);

        ImageIcon excelIcon = new ImageIcon(getClass().getResource("resources/icons/excel_icon_16x16.png"));
        JMenuItem excelMenuItem = new JMenuItem("Export to Excel", excelIcon);
        excelMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Display a progress bar while the spreadsheet is generated in the background thread
                ExcelProgressDialog excelProgressDialog = new ExcelProgressDialog(frame, regimens);

                // Retrieve the spreadsheet workbook that was just generated
                XSSFWorkbook workbook = excelProgressDialog.getWorkbook();

                // Display the export file chooser
                new ExportExcelFileChooser(frame, workbook);
            }
        });
        fileMenu.add(excelMenuItem);

        fileMenu.addSeparator();

        ImageIcon exitIcon = new ImageIcon(getClass().getResource("resources/icons/exit_icon_16x16.png"));
        JMenuItem exitMenuItem = new JMenuItem("Exit", exitIcon);
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        exitMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                panel.saveColumnOrder();
                panel.saveColumnWidths();

                if (panel.hasUnsavedChanges())
                {
                    displaySaveChangesOptionPane();
                }
                else
                {
                    System.exit(0);
                }
            }
        });
        fileMenu.add(exitMenuItem);

        JMenu remindersMenu = new JMenu("Reminders");
        remindersMenu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(remindersMenu);

        ImageIcon configureIcon = new ImageIcon(getClass().getResource("resources/icons/configure_icon_16x16.png"));
        JMenuItem configureRemindersMenuItem = new JMenuItem("Configure Reminders", configureIcon);
        configureRemindersMenuItem.setMnemonic(KeyEvent.VK_C);
        configureRemindersMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        configureRemindersMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new ConfigureRemindersDialog(frame, panel, reminderManager);
            }
        });
        remindersMenu.add(configureRemindersMenuItem);

        remindersMenu.addSeparator();

        JMenuItem showActiveRemindersMenuItem = new JMenuItem("Show Active Reminders");
        showActiveRemindersMenuItem.setMnemonic(KeyEvent.VK_A);
        showActiveRemindersMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        showActiveRemindersMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new ShowActiveRemindersDialog(frame, daoManager);
            }
        });
        remindersMenu.add(showActiveRemindersMenuItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        ImageIcon helpIcon = new ImageIcon(getClass().getResource("resources/icons/help_icon_16x16.png"));
        JMenuItem helpMenuItem = new JMenuItem("Launch Help", helpIcon);
        helpMenuItem.setMnemonic(KeyEvent.VK_L);
        helpMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (Desktop.isDesktopSupported())
                {
                    String htmlFilePath = "res/help/help.html";
                    File htmlFile = new File(htmlFilePath);

                    // Open help.html in the user's default web browser
                    try
                    {
                        Desktop.getDesktop().browse(htmlFile.toURI());
                    }
                    catch (IOException e1)
                    {
                        logger.error("An error occurred while displaying the help page.", e1);

                        JOptionPane.showMessageDialog(frame,
                                "Sorry, an error occurred while displaying the help page.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
                    new HelpDialog(frame);
                }
            }
        });
        helpMenu.add(helpMenuItem);

        helpMenu.addSeparator();

        JMenuItem systemInformationMenuItem = new JMenuItem("System Information");
        systemInformationMenuItem.setMnemonic(KeyEvent.VK_F);
        systemInformationMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new SystemInformationDialog(frame);
            }
        });
        helpMenu.add(systemInformationMenuItem);

        helpMenu.addSeparator();

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        aboutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new AboutDialog(frame);
            }
        });
        helpMenu.add(aboutMenuItem);
    }


    /**
     * This method is called by this frame's window closing event handler, and is
     * also called by the {@code exitMenuItem}'s event handler.  It displays
     * a {@code JOptionPane} that prompts the user to save any changes.
     */
    private void displaySaveChangesOptionPane()
    {
        String message = "Save changes before exiting?";
        Object[] options = {"Yes", "No"};
        int choice = JOptionPane.showOptionDialog(frame,
                message,
                "Save Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0)  // "Yes"
        {
            daoManager.commitTransaction();
            System.exit(0);
        }
        else if (choice == 1)  // "No"
        {
            System.exit(0);
        }
    }


    /**
     * This method sets this frame's icon to an {@code ArrayList} of {@code ImageIcon}s
     * of differing dimensions, which enables the JVM choose the correct size for the current
     * screen resolution at runtime.
     */
    private void setFrameIcon()
    {
        ImageIcon redCrossIcon16 = new ImageIcon(getClass().getResource("resources/icons/cross_icon_16x16.png"));
        ImageIcon redCrossIcon32 = new ImageIcon(getClass().getResource("resources/icons/cross_icon_32x32.png"));
        ImageIcon redCrossIcon64 = new ImageIcon(getClass().getResource("resources/icons/cross_icon_64x64.png"));
        ImageIcon redCrossIcon128 = new ImageIcon(getClass().getResource("resources/icons/cross_icon_128x128.png"));

        List<Image> icons = new ArrayList<Image>();

        icons.add(redCrossIcon16.getImage());
        icons.add(redCrossIcon32.getImage());
        icons.add(redCrossIcon64.getImage());
        icons.add(redCrossIcon128.getImage());

        frame.setIconImages(icons);
    }

}  // end class VitaReminderFrame
