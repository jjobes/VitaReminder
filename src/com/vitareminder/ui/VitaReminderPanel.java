package com.vitareminder.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.vitareminder.business.Regimen;
import com.vitareminder.business.Supplement;
import com.vitareminder.dao.DAOManager;
import com.vitareminder.reminders.ReminderManager;
import com.vitareminder.ui.model.RegimenTableModel;
import com.vitareminder.ui.model.SupplementTableModel;


/**
 * This {@code JPanel} is the main application panel that sits inside
 * the {@code VitaReminderFrame}.  It creates the {@code JTable}s and
 * {@code JButton}s that make up the application's main interface, in
 * addition to implementing their event handlers.  The various components
 * are arranged in a {@code GridBagLayout}.
 */
@SuppressWarnings("serial")
public class VitaReminderPanel extends JPanel
{

    private JLabel regimenNotesLabel,
        supplementNotesLabel;

    private JTextArea regimenNotesTextArea,
        supplementNotesTextArea;

    private JScrollPane regimenTableScrollPane,
        regimenNotesScrollPane,
        supplementTableScrollPane,
        supplementNotesScrollPane;

    private String[] regimenTableColumns = { "RegimenID", "Regimen", "Notes" };

    private String[] supplementTableColumnNames = { "SuppID", "RegimenID", "Supplement", "Amount",
                                                    "Units", "Take at", "E-Mail", "Text", "Voice",
                                                    "Notes" };

    private RegimenTableModel regimenTableModel;

    private SupplementTableModel supplementTableModel;

    private JTable regimenTable,
        supplementTable;

    private JPopupMenu regimenTablePopupMenu,
        supplementTablePopupMenu;

    private JButton addRegimenButton,
        deleteRegimenButton,
        editRegimenButton,
        addSupplementButton,
        deleteSupplementButton,
        editSupplementButton,
        saveButton,
        exitButton;

    private JPanel emptyPanel;

    private boolean unsavedChangesExist = false;

    Preferences userPreferences;

    private JFrame frame;
    private JMenuItem saveMenuItem;
    private List<Regimen> regimens;
    private DAOManager daoManager;
    private ReminderManager reminderManager;

    private Logger logger = Logger.getLogger(VitaReminderPanel.class);


    /**
     * The constructor for the {@code VitaReminderPanel} class.  It calls {@code createPanel()},
     * which actually builds the panel.  If rows exist in the {@code regimenTable} on application
     * startup, it programatically selects the first row, and then sets the {@code supplementTableModel}'s
     * {@code regimenID} field to that row's {@code regimenID}, which populates the {@code supplementTable}
     * with the supplements that belong to that regimen. This constructor also accepts as arguments
     * references to objects that are passed in from the {@code VitaReminderFrame}.
     *
     * @param frame  a reference to the application's main {@code JFrame} that is used to
     *               set the parent window of the {@code JOptionPane}s that are created in
     *               this panel
     * @param saveMenuItem  a reference to the {@code saveMenuItem} declared in
     *                      {@code VitaReminderFrame} that is enabled when the
     *                      panel detects changes made to the data model, and
     *                      disabled when the panel detects that the user has
     *                      pressed the panel's save button
     * @param regimens  an {@code ArrayList} of {@code Regimen}s, where each {@code Regimen}
     *                  object contains an {@code ArrayList} of {@code Supplement} objects.
     *                  This is used as the data model for the regimen and supplement table
     *                  models.
     * @param daoManager  a reference to the object that represents the data access layer
     * @param reminderManager  a reference to the object that manages the creating, updating and
     *                         deleting of reminders
     */
    public VitaReminderPanel(JFrame frame, JMenuItem saveMenuItem, List<Regimen> regimens,
                             DAOManager daoManager, ReminderManager reminderManager)
    {
        this.frame = frame;
        this.saveMenuItem = saveMenuItem;
        this.regimens = regimens;
        this.daoManager = daoManager;
        this.reminderManager = reminderManager;

        userPreferences = Preferences.userRoot();

        createPanel();

        if (regimenTable.getRowCount() > 0)
        {
            regimenTable.setRowSelectionInterval(0, 0);

            int regimenID = (int) regimenTableModel.getValueAt(0, 0);

            supplementTableModel.setCurrentRegimenID(regimenID);
        }

        if (supplementTable.getRowCount() > 0)
        {
            supplementTable.setRowSelectionInterval(0, 0);
        }
    }


    /**
     * This method first calls {@code createTables()}, which creates the {@code JTable}s
     * and their underlying table models.  It then creates the remaining components and adds them
     * to the panel's {@code GridBagLayout}.  Finally, it sets the tab order for the components.
     */
    private void createPanel()
    {
        createTables();

        regimenTableScrollPane = new JScrollPane(regimenTable);

        regimenNotesLabel = new JLabel("Regimen Notes");

        regimenNotesTextArea = new JTextArea(7, 20);
        regimenNotesTextArea.setLineWrap(true);
        regimenNotesTextArea.setWrapStyleWord(true);
        regimenNotesTextArea.setEditable(false);
        regimenNotesTextArea.setHighlighter(null);
        regimenNotesTextArea.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int selectedRow = regimenTable.getSelectedRow();

                    if (selectedRow != -1)
                    {
                        editRegimen();

                    }
                }
            }
        });
        regimenNotesScrollPane = new JScrollPane(regimenNotesTextArea);

        ImageIcon addIcon = new ImageIcon(getClass().getResource("resources/icons/add_icon_20x20.png"));
        addRegimenButton = new JButton(addIcon);
        addRegimenButton.setPreferredSize(new Dimension(30, 30));
        addRegimenButton.setToolTipText("Add Regimen");

        addRegimenButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                addRegimen();
            }
        });

        ImageIcon deleteIcon = new ImageIcon(getClass().getResource("resources/icons/delete_icon_20x20.png"));
        deleteRegimenButton = new JButton(deleteIcon);
        deleteRegimenButton.setPreferredSize(new Dimension(30, 30));
        deleteRegimenButton.setToolTipText("Delete Regimen");

        deleteRegimenButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteRegimen();
            }
        });

        ImageIcon editIcon = new ImageIcon(getClass().getResource("resources/icons/edit_icon_20x20.png"));
        editRegimenButton = new JButton(editIcon);
        editRegimenButton.setPreferredSize(new Dimension(30, 30));
        editRegimenButton.setToolTipText("Edit Regimen");

        editRegimenButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editRegimen();
            }
        });

        // Add an empty column to create space between two sides of panel.
        emptyPanel = new JPanel();

        supplementTableScrollPane = new JScrollPane(supplementTable);

        supplementNotesLabel = new JLabel("Supplement Notes");

        supplementNotesTextArea = new JTextArea(7, 20);
        supplementNotesTextArea.setLineWrap(true);
        supplementNotesTextArea.setWrapStyleWord(true);
        supplementNotesTextArea.setEditable(false);
        supplementNotesTextArea.setHighlighter(null);
        supplementNotesTextArea.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int selectedRow = supplementTable.getSelectedRow();

                    if (selectedRow != -1)
                    {
                        editSupplement();

                    }
                }
            }
        });
        supplementNotesScrollPane = new JScrollPane(supplementNotesTextArea);

        addSupplementButton = new JButton(addIcon);
        addSupplementButton.setPreferredSize(new Dimension(30, 30));
        addSupplementButton.setToolTipText("Add Supplement");

        addSupplementButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                addSupplement();
            }
        });

        deleteSupplementButton = new JButton(deleteIcon);
        deleteSupplementButton.setPreferredSize(new Dimension(30, 30));
        deleteSupplementButton.setToolTipText("Delete Supplement");

        deleteSupplementButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteSupplement();
            }
        });

        editSupplementButton = new JButton(editIcon);
        editSupplementButton.setPreferredSize(new Dimension(30, 30));
        editSupplementButton.setToolTipText("Edit Supplement");

        editSupplementButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editSupplement();
            }
        });

        ImageIcon saveIcon = new ImageIcon(getClass().getResource("resources/icons/save_icon_16x16.png"));
        saveButton = new JButton("Save", saveIcon);
        saveButton.setHorizontalAlignment(SwingConstants.LEFT);
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                daoManager.commitTransaction();
                saveButton.setEnabled(false);
                saveMenuItem.setEnabled(false);
                unsavedChangesExist = false;
            }
        });
        saveButton.setPreferredSize(new Dimension(80, 30));
        saveButton.setEnabled(false);

        ImageIcon exitIcon = new ImageIcon(getClass().getResource("resources/icons/exit_icon_16x16.png"));
        exitButton = new JButton("Exit", exitIcon);
        exitButton.setHorizontalAlignment(SwingConstants.LEFT);
        exitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Save the user-specified column settings for the supplementTable.
                saveColumnOrder();
                saveColumnWidths();

                if (hasUnsavedChanges())
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
                    else  // "No"
                    {
                        System.exit(0);
                    }
                }
                else
                {
                    System.exit(0);
                }
            }
        });
        exitButton.setPreferredSize(new Dimension(80, 30));

        JPanel saveExitButtonPanel = new JPanel();
        saveExitButtonPanel.add(saveButton);
        saveExitButtonPanel.add(exitButton);

        setLayout(new GridBagLayout());

        add(regimenTableScrollPane, GBCFactory.getConstraints(0, 0, 3, 3, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL));
        add(regimenNotesLabel, GBCFactory.getConstraints(0, 3, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        add(regimenNotesScrollPane, GBCFactory.getConstraints(0, 4, 3, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH));
        add(addRegimenButton, GBCFactory.getConstraints(3, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE));
        add(deleteRegimenButton, GBCFactory.getConstraints(3, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE));
        add(editRegimenButton, GBCFactory.getConstraints(3, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE));
        add(emptyPanel, GBCFactory.getConstraints(4, 0, 1, 6, GridBagConstraints.CENTER, GridBagConstraints.BOTH));
        add(supplementTableScrollPane, GBCFactory.getConstraints(5, 0, 5, 3, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        add(supplementNotesLabel, GBCFactory.getConstraints(5, 3, 5, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE));
        add(supplementNotesScrollPane, GBCFactory.getConstraints(5, 4, 5, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH));
        add(addSupplementButton, GBCFactory.getConstraints(10, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE));
        add(deleteSupplementButton, GBCFactory.getConstraints(10, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE));
        add(editSupplementButton, GBCFactory.getConstraints(10, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE));
        add(saveExitButtonPanel, GBCFactory.getConstraints(0, 6, 11, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));

        // Create visual space around the panel.
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Set the tab order.
        Vector<Component> order = new Vector<Component>(7);
        order.add(addRegimenButton);
        order.add(deleteRegimenButton);
        order.add(editRegimenButton);
        order.add(addSupplementButton);
        order.add(deleteSupplementButton);
        order.add(editSupplementButton);
        order.add(saveButton);
        order.add(exitButton);
        PanelFocusTraversalPolicy newPolicy = new PanelFocusTraversalPolicy(order);
        frame.setFocusTraversalPolicy(newPolicy);
    }


    /**
     * Called by {@code VitaReminderFrame} when the user attempts to exit
     * the application, to decide whether or not to display a "save changes"
     * dialog.
     *
     * @return a value representing whether or not any unsaved changes exist
     */
    public boolean hasUnsavedChanges()
    {
        return unsavedChangesExist;
    }


    /**
     * Creates the two {@code JTable}s that contain the regimens and supplements,
     * respectively.  It applies custom formatting to the columns, and also creates
     * a popup menu for each table.  Finally, it loads any saved column properties
     * (the order of the columns and their widths) from the last time the application
     * was run.
     */
    private void createTables()
    {
        regimenTableModel = new RegimenTableModel(frame, regimens, daoManager, regimenTableColumns);

        regimenTable = new JTable(regimenTableModel);

        JTableHeader regimenTableHeader = regimenTable.getTableHeader();
        regimenTableHeader.setDefaultRenderer(new CenterHeaderRenderer(regimenTable));

        regimenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        regimenTable.setPreferredScrollableViewportSize(new Dimension(140, 180));
        regimenTable.setGridColor(Color.BLACK);
        regimenTable.getTableHeader().setReorderingAllowed(false);
        regimenTable.getTableHeader().setResizingAllowed(false);
        regimenTable.setFillsViewportHeight(true);
        regimenTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            // Fill regimen notes and fill supplements.

            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                // Don't do anything yet if changes are still being made to the table.
                if (e.getValueIsAdjusting())
                {
                    return;
                }

                if (regimenTable.getSelectedRow() != -1)
                {
                    int selectedRow = regimenTable.getSelectedRow();

                    String notes = (String) regimenTableModel.getValueAt(selectedRow, 2);
                    regimenNotesTextArea.setText(notes);

                    int regimenID = (int) regimenTableModel.getValueAt(selectedRow, 0);

                    // This is critical to communicating with the supplementTableModel which
                    // regimen is currently selected, and therefore which regimen's supplements
                    // comprise the current supplement table:
                    supplementTableModel.setCurrentRegimenID(regimenID);

                    if (supplementTable.getRowCount() > 0)
                    {
                        supplementTable.setRowSelectionInterval(0, 0);
                    }
                }
            }
        });

        // This ensures that our custom table formatting isn't removed every time the
        // underlying table model changes.
        regimenTable.setAutoCreateColumnsFromModel(false);

        // Make regimenID column invisible.
        regimenTable.removeColumn(regimenTable.getColumnModel().getColumn(0));

        // Make notes column invisible.
        regimenTable.removeColumn(regimenTable.getColumnModel().getColumn(1));

        // Create the popup menu for regimenTable.
        regimenTablePopupMenu = new JPopupMenu();
        JMenuItem addRegimenMenuItem = new JMenuItem("Add New Regimen");
        JMenuItem deleteRegimenMenuItem = new JMenuItem("Delete Regimen");
        JMenuItem editRegimenMenuItem = new JMenuItem("Edit Regimen");

        addRegimenMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                addRegimen();
            }
        });

        deleteRegimenMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteRegimen();
            }
        });

        editRegimenMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editRegimen();
            }
        });

        regimenTablePopupMenu.add(addRegimenMenuItem);
        regimenTablePopupMenu.add(deleteRegimenMenuItem);
        regimenTablePopupMenu.add(editRegimenMenuItem);

        regimenTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    // Make sure an actual row is double-clicked before displaying the edit dialog:
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());

                    if (row != -1)
                    {
                        editRegimen();
                    }
                }
            }

            // Check for the popupTrigger on both mousePressed and mouseReleased
            // for cross-platform compatibility.
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row))
                    {
                        source.changeSelection(row, column, false, false);
                    }

                    regimenTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row))
                    {
                        source.changeSelection(row, column, false, false);
                    }

                    regimenTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        supplementTableModel = new SupplementTableModel(frame, regimens, daoManager,
                supplementTableColumnNames);

        supplementTable = new JTable(supplementTableModel);

        JTableHeader supplementTableHeader = supplementTable.getTableHeader();
        supplementTableHeader.setDefaultRenderer(new CenterHeaderRenderer(supplementTable));

        supplementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplementTable.setPreferredScrollableViewportSize(new Dimension(450, 180));
        supplementTable.setGridColor(Color.BLACK);
        supplementTable.getTableHeader().setReorderingAllowed(false);
        supplementTable.setFillsViewportHeight(true);
        supplementTable.getColumnModel().getColumn(2).setPreferredWidth(170);  // Supplement column
        supplementTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Amount column
        supplementTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Units column
        supplementTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Take at column
        supplementTable.setDefaultRenderer(Double.class, new NoCommaDoubleRenderer());

        // Make sure the time column is displayed properly.  The time is stored in the database and resultset
        // as a java.sql.Time object, but Java sees this as a java.util.Date object and by default renders it
        // as "January 1, 1970".  So we have to tell the table how to format this object correctly by setting
        // a TableCellRenderer on that column.
        supplementTable.getColumnModel().getColumn(5).setCellRenderer(new TimeFormatRenderer());

        // This ensures that our custom table formatting isn't removed every time the
        // underlying table model changes.
        supplementTable.setAutoCreateColumnsFromModel(false);
        supplementTable.getTableHeader().setReorderingAllowed(true);

        supplementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    return;
                }

                if (supplementTable.getSelectedRow() != -1)
                {
                    int selectedRow = supplementTable.getSelectedRow();

                    // Get the notes from the hidden column.
                    String notes = (String) supplementTableModel.getValueAt(selectedRow, 9);

                    supplementNotesTextArea.setText(notes);
                }
                else  // No row is selected.
                {
                    supplementNotesTextArea.setText("");
                }
            }
        });

        // Make SuppID column invisible in table view.
        supplementTable.removeColumn(supplementTable.getColumnModel().getColumn(0));

        // Make RegimenID column invisible.
        supplementTable.removeColumn(supplementTable.getColumnModel().getColumn(0));

        // Make Notes column invisible.
        supplementTable.removeColumn(supplementTable.getColumnModel().getColumn(7));

        supplementTablePopupMenu = new JPopupMenu();
        JMenuItem addSupplementMenuItem = new JMenuItem("Add Supplement");
        JMenuItem deleteSupplementMenuItem = new JMenuItem("Delete Supplement");
        JMenuItem editSupplementMenuItem = new JMenuItem("Edit Supplement");

        addSupplementMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                addSupplement();
            }
        });

        deleteSupplementMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteSupplement();
            }
        });

        editSupplementMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editSupplement();
            }
        });

        supplementTablePopupMenu.add(addSupplementMenuItem);
        supplementTablePopupMenu.add(deleteSupplementMenuItem);
        supplementTablePopupMenu.add(editSupplementMenuItem);

        supplementTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    // Make sure an actual row is selected before displaying the edit dialog:
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());

                    if (row != -1)
                    {
                        editSupplement();
                    }
                }
            }

            // Check for the popupTrigger on both mousePressed and mouseReleased
            // for cross-platform compatibility.
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row))
                    {
                        source.changeSelection(row, column, false, false);
                    }

                    supplementTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row))
                    {
                        source.changeSelection(row, column, false, false);
                    }

                    supplementTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        loadColumnOrder();
        loadColumnWidths();
    }


    /**
     * Saves the order of the columns of the supplement table, as the
     * user is able to reorder the columns if they choose.  It uses
     * the {@code getColumns()} method of {@code TableColumnModel} to
     * retrieve an enumeration of the columns in the order in which they appear
     * in the table.  It uses this enumeration to store the index of each column
     * in the order in which it currently appears in the table. This data is then
     * stored in a {@code Properties} object, and these properties are then
     * saved in the columnorder.properties file so they can be read back on the
     * next application startup.
     */
    public void saveColumnOrder()
    {
        TableColumnModel columnModel = supplementTable.getColumnModel();

        List<Integer> order = new ArrayList<Integer>();

        Enumeration<TableColumn> columns = columnModel.getColumns();
        while (columns.hasMoreElements())
        {
            TableColumn column = columns.nextElement();
            Integer modelIndex = new Integer(column.getModelIndex());

            // Subtract two because the first two columns are hidden.
            order.add(modelIndex - 2);
        }

        Properties properties = new Properties();
        OutputStream outputStream = null;
        int columnCount = columnModel.getColumnCount();

        try
        {
            for (int i = 0; i < columnCount; i++)
            {
                // format: columnX=modelIndex, where X is 0-6
                properties.setProperty("column"+(i), order.get(i).toString());
            }

            outputStream = new FileOutputStream("user/data/columnorder.properties");
            properties.store(outputStream, null);
        }
        catch (IOException e)
        {
            logger.warn("A file I/O error has occured.", e);
            JOptionPane.showMessageDialog(null,
                    "Sorry, there was an error saving the user column preferences.",
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                }
                catch (IOException e)
                {
                    logger.warn("A file I/O error has occured.", e);
                    JOptionPane.showMessageDialog(null,
                            "Sorry, there was an error saving the user column preferences.",
                            "I/O Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    /**
     * Loads the column order for the supplement table that is stored in the
     * columnorder.properties file.  It stores the actual columns in their
     * default order in {@code defaultColumns}.  It then removes all columns
     * from the table before adding them back in one by one in the order
     * saved in columnorder.properties.
     */
    private void loadColumnOrder()
    {
        Properties properties = new Properties();
        InputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream("user/data/columnorder.properties");
            properties.load(inputStream);

            TableColumnModel columnModel = supplementTable.getColumnModel();

            TableColumn[] defaultColumns = new TableColumn[7];

            for (int i = 0; i < defaultColumns.length; i++)
            {
                defaultColumns[i] = columnModel.getColumn(i);
            }

            while (columnModel.getColumnCount() > 0)
            {
                columnModel.removeColumn(columnModel.getColumn(0));
            }

            int index = 0;

            for (int i = 0; i < defaultColumns.length; i++)
            {
                index = Integer.parseInt(properties.getProperty("column"+i));
                columnModel.addColumn(defaultColumns[index]);
            }
        }
        catch (IOException e)
        {
            logger.warn("A file I/O error has occured.", e);
            JOptionPane.showMessageDialog(null,
                    "Sorry, there was an error reading the saved column order.",
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    logger.warn("A file I/O error has occured.", e);
                    JOptionPane.showMessageDialog(null,
                            "Sorry, there was an error reading the saved column order.",
                            "I/O Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    /**
     * Saves the widths of the columns of the supplement table, as the user
     * is able to resize the columns if they choose.  It uses the
     * {@code getColumns()} method of TableColumnModel to retrieve an
     * enumeration of the columns in the order in which they appear in the
     * table.  It then iterates through this enumeration and stores the width
     * of each column.  It then stores these widths in a {@code Properties}
     * object and then saves these properties in the columnwidths.properties file
     * so they can be read back in on next application startup.
     */
    public void saveColumnWidths()
    {
        TableColumnModel columnModel = supplementTable.getColumnModel();

        List<Integer> widths = new ArrayList<Integer>();

        Enumeration<TableColumn> columns = columnModel.getColumns();
        while (columns.hasMoreElements())
        {
            TableColumn column = columns.nextElement();
            Integer width = new Integer(column.getPreferredWidth());
            widths.add(width);
        }

        Properties properties = new Properties();
        OutputStream outputStream = null;
        int columnCount = columnModel.getColumnCount();

        try
        {
            for (int i = 0; i < columnCount; i++)
            {
                // format: columnX=width, where X = 0-6
                properties.setProperty("column"+(i), widths.get(i).toString());
            }

            outputStream = new FileOutputStream("user/data/columnwidths.properties");
            properties.store(outputStream, null);
        }
        catch (IOException e)
        {
            logger.warn("A file I/O error has occured.", e);
            JOptionPane.showMessageDialog(null,
                    "Sorry, there was an error saving the column widths.",
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                }
                catch (IOException e)
                {
                    logger.warn("A file I/O error has occured.", e);
                    JOptionPane.showMessageDialog(null,
                            "Sorry, there was an error saving the column widths.",
                            "I/O Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    /**
     * Sets the column widths of the supplement table to the widths
     * stored in the columnwidths.properties file.
     */
    private void loadColumnWidths()
    {
        Properties properties = new Properties();
        InputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream("user/data/columnwidths.properties");
            properties.load(inputStream);

            TableColumnModel columnModel = supplementTable.getColumnModel();
            int columnCount = columnModel.getColumnCount();
            int width = 0;

            for (int i = 0; i < columnCount; i++)
            {
                width = Integer.parseInt(properties.getProperty("column"+i));
                columnModel.getColumn(i).setPreferredWidth(width);
            }
        }
        catch (IOException e)
        {
            logger.warn("A file I/O error has occured.", e);
            JOptionPane.showMessageDialog(null,
                    "Sorry, there was an error restoring the column widths.",
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    logger.warn("A file I/O error has occured.", e);
                    JOptionPane.showMessageDialog(null,
                            "Sorry, there was an error restoring the column widths.",
                            "I/O Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    /**
     * Adds a new {@code Regimen} object to the {@code regimenTableModel}.
     * It is called when the user either presses the {@code addRegimenButton},
     * or when they click the {@code addRegimenMenuItem} on the popup menu.
     * <p>
     * It displays the {@code AddRegimenDialog}, which contains fields for the user to
     * input the name and notes for the new regimen.  When the dialog is closed, this
     * method checks to see if the input was validated.  If it was, it creates a new
     * {@code Regimen} object and fills that object with the values just entered
     * into the dialog.  It then adds that object to the table model.  The {@code saveButton}
     * and {@code saveMenuItem} are also enabled to reflect the fact that saveable changes
     * have just been made.  Finally, this recently added row is programatically selected
     * in the table, and the table scrolls down to make it visible if necessary.
     * <p>
     * <strong>NOTE:</strong> This method only adds the {@code Regimen} object to the table
     * <i>model</i>.  This operation does not commit the current database transaction.  The
     * transaction is only committed when the user presses the {@code saveButton} or the
     * {@code saveMenuItem}.
     */
    private void addRegimen()
    {
        AddRegimenDialog addRegimenDialog = new AddRegimenDialog(frame);

        if (addRegimenDialog.isInputValidated())
        {
            regimenTableModel.addRow(addRegimenDialog.getNewRegimen());

            saveButton.setEnabled(true);
            saveMenuItem.setEnabled(true);

            unsavedChangesExist = true;

            int lastRow = regimenTable.getRowCount() - 1;
            regimenTable.setRowSelectionInterval(lastRow, lastRow);

            regimenTable.scrollRectToVisible(new Rectangle(regimenTable.getCellRect(lastRow, 0, true)));
        }
    }


    /**
     * Deletes the {@code Regimen} object represented by the currently
     * selected row in the {@code regimenTable}.  It is called by either the
     * {@code deleteRegimenButton} or the {@code deleteRegimenMenuItem}.
     * <p>
     * It enables the {@code saveButton} and {@code saveMenuItem} to indicate
     * to the user that unsaved changes now exist.
     * <p>
     * <strong>NOTE:</strong> This method only deletes the {@code Regimen} object from
     * the table <i>model</i>. This operation does not commit the current database
     * transaction.  The transaction is only committed when the user presses the
     * {@code saveButton} or the {@code saveMenuItem}.
     */
    private void deleteRegimen()
    {
        int selectedRow = regimenTable.getSelectedRow();

        // If a row is actually selected in the regimenTable
        if (selectedRow != -1)
        {
            String regimenName = (String) regimenTableModel.getValueAt(selectedRow, 1);

            // Ask if user really wants to delete regimen
            String message = "Delete regimen \"" + regimenName + "\" ?" + "\n\n"
                    + "The regimen and all of its supplements will be deleted.\n";

            Object[] options = {"Yes", "No"};

            int choice = JOptionPane.showOptionDialog(frame,
                    message,
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (choice == 0)  // Yes
            {
                saveButton.setEnabled(true);
                saveMenuItem.setEnabled(true);
                unsavedChangesExist = true;

                regimenNotesTextArea.setText("");

                int numRows = regimenTable.getRowCount();

                // If there is more than 1 row left
                if (numRows > 1)
                {
                    regimenTableModel.removeRow(selectedRow);

                    // If they deleted bottom row
                    if (selectedRow == numRows-1)
                    {
                        // Select row above
                        // (get the index of the row above the one just deleted)
                        int rowAbove = selectedRow - 1;
                        regimenTable.setRowSelectionInterval(rowAbove, rowAbove);
                    }
                    // If they deleted any other row
                    else
                    {
                        // Select that row
                        regimenTable.setRowSelectionInterval(selectedRow, selectedRow);
                    }
                }
                else if (numRows == 1)  // Delete last row
                {
                    // First manually remove all rows from supplementTableModel if rows are still present
                    if (supplementTableModel.getRowCount() > 0)
                    {
                        for (int i = supplementTableModel.getRowCount() - 1; i > -1; i--)
                        {
                            supplementTableModel.removeRow(i);
                        }
                    }

                    // Then remove the regimen row
                    regimenTableModel.removeRow(selectedRow);
                }
            }
        }
        else  // A row is not selected
        {
            JOptionPane.showMessageDialog(frame,
                    "No regimen selected.",
                    "Select a Regimen",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Edits the {@code Regimen} object represented by the currently selected row
     * in the {@code regimenTable}. It is called by either the {@code editRegimenButton}
     * or the {@code editRegimenMenuItem}.
     * <p>
     * When the {@code EditRegimenDialog} returns, the method checks to make
     * sure we have valid input and that changes have actually been made.  If this
     * is the case, the {@code regimenTableModel} is updated.  Only those fields
     * that have been changed are updated, to ensure that the database is only
     * accessed when necessary.  It enables the {@code saveButton} and the
     * {@code saveMenuItem} to indicate to the user that unsaved changes now exist.
     * <p>
     * <strong>NOTE:</strong> This method only updates the fields in the regimen
     * table <i>model</i>.  This operation does not commit the current database
     * transaction.  The transaction is only committed when the user presses the
     * {@code saveButton} or the {@code saveMenuItem}.
     */
    private void editRegimen()
    {
        EditRegimenDialog editRegimenDialog;

        String oldRegimenName = "";
        String oldRegimenNotes = "";
        String newRegimenName = "";
        String newRegimenNotes = "";

        int selectedRow = regimenTable.getSelectedRow();

        // If a regimen is actually selected
        if (selectedRow != -1)
        {
            oldRegimenName = (String) regimenTableModel.getValueAt(selectedRow, 1);
            oldRegimenNotes = (String) regimenTableModel.getValueAt(selectedRow, 2);

            Regimen oldRegimen = new Regimen();
            oldRegimen.setRegimenName(oldRegimenName);
            oldRegimen.setRegimenNotes(oldRegimenNotes);

            // Display EditRegimenDialog
            editRegimenDialog = new EditRegimenDialog(frame, oldRegimen);

            // If we have valid input
            if (editRegimenDialog.isInputValidated())
            {
                // And changes have been made
                if (editRegimenDialog.getChangesMade())
                {
                    Regimen newRegimen = editRegimenDialog.getNewRegimen();
                    newRegimenName = newRegimen.getRegimenName();
                    newRegimenNotes = newRegimen.getRegimenNotes();

                    // Only update the field if the value has changed. This ensures that we only
                    // access the database when necessary.
                    if (!newRegimenName.equals(oldRegimenName))
                        regimenTableModel.setValueAt(newRegimenName, selectedRow, 1);

                    if (!newRegimenNotes.equals(oldRegimenNotes))
                        regimenTableModel.setValueAt(newRegimenNotes, selectedRow, 2);

                    saveButton.setEnabled(true);
                    saveMenuItem.setEnabled(true);
                    unsavedChangesExist = true;

                    String regimenNotes = (String) regimenTableModel.getValueAt(selectedRow, 2);
                    regimenNotesTextArea.setText(regimenNotes);
                }
            }
        }
        else  // No row selected in regimenTable
        {
            JOptionPane.showMessageDialog(frame,
                    "No regimen selected.",
                    "Select Regimen",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /**
     * Adds a new {@code Supplement} object to the {@code supplementTableModel}.
     * It is called when the user either presses the {@code addSupplementButton},
     * or when they click the {@code addSupplementMenuItem} on the popup menu.
     * <p>
     * It displays the {@code AddSupplementDialog}, which contains fields for the user to input
     * the properties and reminder preferences for the new {@code Supplement}.  When the
     * dialog is closed, this method checks to see if the input was validated.  If it was,
     * it grabs the new {@code Supplement} that was just created from the dialog.  It then checks
     * to see if any new reminders need to be created, and creates them if necessary.  The
     * {@code saveButton} and {@code saveMenuItem} are also enabled to reflect the fact that
     * saveable changes have just been made.  Finally, this recently added row is programatically
     * selected in the table, and the table scrolls down to make it visible if necessary.
     * <p>
     * <strong>NOTE:</strong> This method only adds the {@code Supplement} object to the table
     * <i>model</i>.  This operation does not commit the current database transaction.  The
     * transaction is only committed when the user presses the {@code saveButton} or the
     * {@code saveMenuItem}.
     */
    private void addSupplement()
    {
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);
        boolean emailVerified = userPreferences.getBoolean("EMAIL_VERIFIED", false);
        boolean phoneVerified = userPreferences.getBoolean("PHONE_VERIFIED", false);

        AddSupplementDialog addSupplementDialog;

        if (regimenTable.getSelectedRow() != -1)
        {
            int selectedRow = regimenTable.getSelectedRow();
            String regimenName = (String) regimenTableModel.getValueAt(selectedRow, 1);

            // Get regimenID of currently selected regimen. We need to set the new supplement
            // object's regimenID to this so it can be properly inserted into the supplements table
            // foreign key column and be associated with that regimen.
            int regimenID = (int) regimenTableModel.getValueAt(selectedRow, 0);

            addSupplementDialog = new AddSupplementDialog(frame, regimenName);

            if (addSupplementDialog.isInputValidated())
            {
                Supplement newSupplement = addSupplementDialog.getNewSupplement();
                newSupplement.setRegimenID(regimenID);

                supplementTableModel.addRow(newSupplement);

                saveButton.setEnabled(true);
                saveMenuItem.setEnabled(true);
                unsavedChangesExist = true;

                // Select last row of supplement table - the row that was just added
                int lastRow = supplementTable.getRowCount() - 1;
                supplementTable.setRowSelectionInterval(lastRow, lastRow);

                // Scroll table to this row to make it visible
                supplementTable.scrollRectToVisible(new Rectangle(supplementTable.getCellRect(lastRow, 0, true)));

                // Do we have to load a new reminder into memory?

                if (newSupplement.getEmailEnabled() ||
                    newSupplement.getTextEnabled() ||
                    newSupplement.getVoiceEnabled())
                {
                    int suppID = (int) supplementTableModel.getValueAt(lastRow, 0);
                    newSupplement.setSuppID(suppID);

                    if (newSupplement.getEmailEnabled())
                    {
                        if (emailRemindersEnabled &&
                            emailVerified)
                        {
                            reminderManager.loadEmailReminder(newSupplement);
                        }
                    }

                    if (newSupplement.getTextEnabled())
                    {
                        if (textRemindersEnabled &&
                            phoneVerified)
                        {
                            reminderManager.loadTextReminder(newSupplement);
                        }
                    }

                    if (newSupplement.getVoiceEnabled())
                    {
                        if (voiceRemindersEnabled &&
                            phoneVerified)
                        {
                            reminderManager.loadVoiceReminder(newSupplement);
                        }
                    }
                }
            }
        }
        else  // No regimen selected
        {
            JOptionPane.showMessageDialog(frame,
                    "No regimen selected.",
                    "Select Regimen",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Deletes the {@code Supplement} object represented by the currently
     * selected row in the {@code supplementTable}.  It is called by either the
     * {@code deleteSupplementButton} or the {@code deleteSupplementMenuItem}.  It
     * checks to see if the deleted {@code Supplement} had any reminders, and deletes
     * those reminders if necessary.  It enables the {@code saveButton} and
     * {@code saveMenuItem} to indicate to the user that unsaved changes now exist.
     * <p>
     * <strong>NOTE:</strong> This method only deletes the {@code Supplement} object from
     * the table <i>model</i>. This operation does not commit the current database
     * transaction.  The transaction is only committed when the user presses the
     * {@code saveButton} or the {@code saveMenuItem}.
     */
    private void deleteSupplement()
    {
        int selectedRow = supplementTable.getSelectedRow();

        // If a row is actually selected in the supplementTable
        if (selectedRow != -1)
        {
            String suppName = (String) supplementTableModel.getValueAt(selectedRow, 2);

            String message = "Delete supplement \"" + suppName + "\" ?";
            Object[] options = {"Yes", "No"};
            int choice = JOptionPane.showOptionDialog(frame,
                    message,
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (choice == 0)  // Yes
            {
                // First check if it has any active reminders and cancel them
                boolean emailEnabled = (boolean) supplementTableModel.getValueAt(selectedRow, 6);
                boolean textEnabled = (boolean) supplementTableModel.getValueAt(selectedRow, 7);

                // If either e-mail or text messages are enabled, cancel reminders
                if (emailEnabled)
                {
                    int suppID = (int) supplementTableModel.getValueAt(selectedRow, 0);

                    reminderManager.unloadReminder(suppID, "email");
                }

                if (textEnabled)
                {
                    int suppID = (int) supplementTableModel.getValueAt(selectedRow, 0);

                    reminderManager.unloadReminder(suppID, "text");
                }

                saveButton.setEnabled(true);
                saveMenuItem.setEnabled(true);
                unsavedChangesExist = true;

                int numRows = supplementTable.getRowCount();

                // There is more than 1 row left
                if (numRows > 1)
                {
                    supplementTableModel.removeRow(selectedRow);

                    // If they deleted bottom row
                    if (selectedRow == numRows-1)
                    {
                        // Select row above
                        // (get the index of the row above the one just deleted)
                        int rowAbove = selectedRow - 1;

                        // Select that row
                        supplementTable.setRowSelectionInterval(rowAbove, rowAbove);
                    }
                    // If they deleted any other row
                    else
                    {
                        // Select that row
                        supplementTable.setRowSelectionInterval(selectedRow, selectedRow);
                    }
                }
                else if (numRows == 1)
                {
                    // Delete last row
                    supplementTableModel.removeRow(selectedRow);
                }
            }
        }
        else  // A row is not selected
        {
            JOptionPane.showMessageDialog(frame,
                    "No supplement selected.",
                    "Select a Supplement",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Edits the {@code Supplement} object represented by the currently selected row
     * in the {@code supplementTable}. It is called by either the {@code editSupplementButton}
     * or the {@code editSupplementMenuItem}.
     * <p>
     * It first builds a {@code Supplement} object using the values from this {@code Supplement}
     * in the table model and stores these values in the {@code oldSupplement} object.
     * It then passes this object into the {@code EditSupplementDialog} constructor.  The
     * {@code EditSupplementDialog} internally builds a {@code newSupplement} object that
     * contains any changes that the user has just made to the {@code Supplement}.  When
     * the {@code EditSupplementDialog} returns, the method checks to make sure we have
     * valid input and that changes have actually been made.  If this is the case, the
     * method retrieves the {@code newSupplement} object from the dialog and then updates the
     * {@code supplementTableModel}.  Only those fields that have been changed
     * are updated, to ensure that the database is only accessed when necessary.  In
     * addition, any reminders that have been updated in the {@code newSupplement}
     * are now applied.  It then enables the {@code saveButton} and the {@code saveMenuItem}
     * to indicate to the user that unsaved changes now exist.
     * <p>
     * <strong>NOTE:</strong> This method only updates the fields in the supplement table
     * <i>model</i>.  This operation does not commit the current database transaction.  The
     * transaction is only committed when the user presses the {@code saveButton} or the
     * {@code saveMenuItem}.
     */
    private void editSupplement()
    {
        boolean emailRemindersEnabled = userPreferences.getBoolean("EMAIL_REMINDERS_ENABLED", false);
        boolean textRemindersEnabled = userPreferences.getBoolean("TEXT_REMINDERS_ENABLED", false);
        boolean voiceRemindersEnabled = userPreferences.getBoolean("VOICE_REMINDERS_ENABLED", false);
        boolean emailVerified = userPreferences.getBoolean("EMAIL_VERIFIED", false);
        boolean phoneVerified = userPreferences.getBoolean("PHONE_VERIFIED", false);

        EditSupplementDialog editSupplementDialog;

        // We will grab the values from the currently selected row and fill them
        // in these variables. then we'll use these values to fill the dialog with.

        String oldSuppName = "";
        double oldSuppAmount = 0.0;
        String oldSuppUnits = "";

        String oldTimeString = "";  // We will convert the current java.sql.Time to a String
        // and store it here. We'll use this String later to determine
        // if the user has changed the time after the edit dialog closes.

        boolean oldEmailEnabled = false;
        boolean oldTextEnabled = false;
        boolean oldVoiceEnabled = false;
        String oldSuppNotes = "";

        // First check that a supplement is selected
        int selectedSuppRow = supplementTable.getSelectedRow();

        if (selectedSuppRow != -1)
        {
            // Retrieve the old values that we will create the oldSupplement object
            // with and then pass to the EditSupplementDialog.
            int suppID = (int) supplementTableModel.getValueAt(selectedSuppRow, 0);
            int regimenID = (int) supplementTableModel.getValueAt(selectedSuppRow, 1);
            oldSuppName = (String) supplementTableModel.getValueAt(selectedSuppRow, 2);
            oldSuppAmount = (double) supplementTableModel.getValueAt(selectedSuppRow, 3);
            oldSuppUnits = (String) supplementTableModel.getValueAt(selectedSuppRow, 4);

            java.sql.Time oldTime = (java.sql.Time) supplementTableModel.getValueAt(selectedSuppRow, 5);  // e.g., 15:30:00
            oldTimeString = oldTime.toString();  // used to compare to newTimeString below

            // We have to call the getValueAtUnfiltered() function because the normal getValueAt()
            // filters out the reminder values if the user has disabled them. This filtering
            // is necessary for not displaying reminder check marks in the main table if that
            // reminder has been globally disabled. However, getValueAtUnfiltered returns the actual
            // value in that cell, which is what we want in order to set the state of the radio
            // buttons in the editSupplementDialog, even if they are globally disabled. This allows the
            // user to see what their previous settings were for these reminders, even if they are
            // grayed out.
            oldEmailEnabled = (boolean) supplementTableModel.getValueAtUnfiltered(selectedSuppRow, 6);
            oldTextEnabled = (boolean) supplementTableModel.getValueAtUnfiltered(selectedSuppRow, 7);
            oldVoiceEnabled = (boolean) supplementTableModel.getValueAtUnfiltered(selectedSuppRow, 8);

            oldSuppNotes = (String) supplementTableModel.getValueAt(selectedSuppRow, 9);

            // Build oldSupplement object to pass in to the EditSupplementDialog constructor
            Supplement oldSupplement = new Supplement();
            oldSupplement.setSuppID(suppID);
            oldSupplement.setRegimenID(regimenID);
            oldSupplement.setSuppName(oldSuppName);
            oldSupplement.setSuppAmount(oldSuppAmount);
            oldSupplement.setSuppUnits(oldSuppUnits);
            oldSupplement.setSuppTime(oldTime);
            oldSupplement.setEmailEnabled(oldEmailEnabled);
            oldSupplement.setTextEnabled(oldTextEnabled);
            oldSupplement.setVoiceEnabled(oldVoiceEnabled);
            oldSupplement.setSuppNotes(oldSuppNotes);

            editSupplementDialog = new EditSupplementDialog(frame, oldSupplement);

            if (editSupplementDialog.isInputValidated())
            {
                if (editSupplementDialog.getChangesMade())
                {
                    Supplement newSupplement = editSupplementDialog.getNewSupplement();

                    // Store new values, which we will add to our newSupplement object
                    // and pass that to the table model.
                    String newSuppName = newSupplement.getSuppName();
                    double newSuppAmount = newSupplement.getSuppAmount();
                    String newSuppUnits = newSupplement.getSuppUnits();
                    java.sql.Time newTime = newSupplement.getSuppTime();
                    String newTimeString = newTime.toString();
                    boolean newEmailEnabled = newSupplement.getEmailEnabled();
                    boolean newTextEnabled = newSupplement.getTextEnabled();
                    boolean newVoiceEnabled = newSupplement.getVoiceEnabled();
                    String newSuppNotes = newSupplement.getSuppNotes();

                    // Only update the following fields if the value has changed.
                    if (!newSuppName.equals(oldSuppName))
                        supplementTableModel.setValueAt(newSuppName, selectedSuppRow, 2);

                    if (newSuppAmount != oldSuppAmount)
                        supplementTableModel.setValueAt(newSuppAmount, selectedSuppRow, 3);

                    if (!newSuppUnits.equals(oldSuppUnits))
                        supplementTableModel.setValueAt(newSuppUnits, selectedSuppRow, 4);

                    if (!newSupplement.getFormattedTime().equals(oldSupplement.getFormattedTime()))
                        supplementTableModel.setValueAt(newTime, selectedSuppRow, 5);

                    if (newEmailEnabled != oldEmailEnabled)
                        supplementTableModel.setValueAt(newEmailEnabled, selectedSuppRow, 6);

                    if (newTextEnabled != oldTextEnabled)
                        supplementTableModel.setValueAt(newTextEnabled, selectedSuppRow, 7);

                    if (newVoiceEnabled != oldVoiceEnabled)
                        supplementTableModel.setValueAt(newVoiceEnabled, selectedSuppRow, 8);

                    if (!newSuppNotes.equals(oldSuppNotes))
                        supplementTableModel.setValueAt(newSuppNotes, selectedSuppRow, 9);

                    // Check if user has canceled any reminders

                    // If they disabled email reminders
                    if (oldEmailEnabled &&
                        !newEmailEnabled)
                    {
                        reminderManager.unloadReminder(suppID, "email");
                    }

                    // If they disabled text message reminders
                    if (oldTextEnabled &&
                        !newTextEnabled)
                    {
                        reminderManager.unloadReminder(suppID, "text");
                    }

                    // If they disabled voice reminders
                    if (oldVoiceEnabled &&
                        !newVoiceEnabled)
                    {
                        reminderManager.unloadReminder(suppID, "voice");
                    }

                    // Has the user just changed a field on an existing reminder?
                    // This updates the content of any existing reminders in memory.

                    // If user changed any of these three...
                    if (!newSuppName.equals(oldSuppName) ||
                        newSuppAmount != oldSuppAmount ||
                        !newSuppUnits.equals(oldSuppUnits))
                    {
                        // ... on an existing email reminder
                        if (oldEmailEnabled &&
                            newEmailEnabled)
                        {
                            if (emailRemindersEnabled &&
                                emailVerified)
                            {
                                reminderManager.unloadReminder(suppID, "email");
                                reminderManager.loadEmailReminder(newSupplement);
                            }
                        }

                        // ... on an existing text message reminder
                        if (oldTextEnabled &&
                            newTextEnabled)
                        {
                            if (textRemindersEnabled &&
                                phoneVerified)
                            {
                                reminderManager.unloadReminder(suppID, "text");
                                reminderManager.loadTextReminder(newSupplement);
                            }
                        }

                        // ... on an existing voice reminder
                        if (oldVoiceEnabled &&
                            newVoiceEnabled)
                        {
                            if (voiceRemindersEnabled &&
                                phoneVerified)
                            {
                                reminderManager.unloadReminder(suppID, "voice");
                                reminderManager.loadVoiceReminder(newSupplement);
                            }
                        }
                    }

                    // Check if user has created any new reminders

                    // User just created a new email reminder
                    if(!oldEmailEnabled &&
                        newEmailEnabled)
                    {
                        if (emailRemindersEnabled &&
                            emailVerified)
                        {
                            reminderManager.loadEmailReminder(newSupplement);
                        }
                    }

                    // User just created a new text message reminder
                    if(!oldTextEnabled &&
                        newTextEnabled)
                    {
                        if (textRemindersEnabled &&
                            phoneVerified)
                        {
                            reminderManager.loadTextReminder(newSupplement);
                        }
                    }

                    // User just created a new voice reminder
                    if(!oldVoiceEnabled &&
                        newVoiceEnabled)
                    {
                        if (voiceRemindersEnabled &&
                            phoneVerified)
                        {
                            reminderManager.loadVoiceReminder(newSupplement);
                        }
                    }

                    // Check if the user has just changed the time on a pre-existing reminder.
                    // These are special cases because we have to remove the old reminder
                    // and then create a new one.

                    if (oldEmailEnabled &&
                        newEmailEnabled &&
                        !newTimeString.equals(oldTimeString))
                    {
                        if (emailRemindersEnabled &&
                            emailVerified)
                        {
                            reminderManager.unloadReminder(suppID, "email");
                            reminderManager.loadEmailReminder(newSupplement);
                        }
                    }

                    if (oldTextEnabled &&
                        newTextEnabled &&
                        !newTimeString.equals(oldTimeString))
                    {
                        if (textRemindersEnabled &&
                            phoneVerified)
                        {
                            reminderManager.unloadReminder(suppID, "text");
                            reminderManager.loadTextReminder(newSupplement);
                        }
                    }

                    if (oldVoiceEnabled &&
                        newVoiceEnabled &&
                        !newTimeString.equals(oldTimeString))
                    {
                        if (voiceRemindersEnabled &&
                            phoneVerified)
                        {
                            reminderManager.unloadReminder(suppID, "voice");
                            reminderManager.loadVoiceReminder(newSupplement);
                        }
                    }

                    saveButton.setEnabled(true);
                    saveMenuItem.setEnabled(true);
                    unsavedChangesExist = true;

                    supplementTable.setRowSelectionInterval(selectedSuppRow, selectedSuppRow);

                    // Display new notes because they're in a hidden column.
                    String notes = (String) supplementTableModel.getValueAt(selectedSuppRow, 9);
                    supplementNotesTextArea.setText(notes);
                }
            }
        }
        else  // A row is not selected
        {
            JOptionPane.showMessageDialog(frame,
                    "No supplement selected.",
                    "Select a Supplement",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Called from {@code VitaReminderFrame} to disable the saveButton when the user
     * clicks the {@code saveMenuItem}.
     *
     * @param enabled  the value that reflects whether or not the {@code saveButton}
     *                 is enabled or disabled
     */
    public void setSaveButtonEnabled(boolean enabled)
    {
        saveButton.setEnabled(enabled);
    }


    /**
     * Called from {@code VitaReminderFrame} when the user clicks the {@code saveMenuItem}.
     *
     * @param unsavedChangesExist  the value that reflects whether or not there
     *                             are unsaved changes that the user can save
     */
    public void setUnsavedChangesExist(boolean unsavedChangesExist)
    {
        this.unsavedChangesExist = unsavedChangesExist;
    }


    /**
     * Updates the reminder check boxes on the supplementTable. Called from
     * {@code ConfigureRemindersDialog} when the user enables or disables any
     * of the reminder types.
     */
    public void fireSupplementTableModelUpdates()
    {
        supplementTableModel.fireTableDataChanged();

        if (supplementTable.getRowCount() > 0)
        {
            supplementTable.setRowSelectionInterval(0, 0);
        }
    }


    /**
     * Called by the {@code importMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame} to update the regimen table's
     * model after the user imports data from a backup.
     *
     * @param regimens  the new regimen objects pulled from the
     *                  new database that the user just imported
     */
    public void setRegimenTableModel(List<Regimen> regimens)
    {
        regimenTableModel.setModel(regimens);
    }


    /**
     * Called by the {@code importMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame} to update the supplement table's
     * model after the user imports data from a backup.
     *
     * @param regimens  the new regimen objects pulled from the
     *                  new database that the user just imported
     */
    public void setSupplementTableModel(List<Regimen> regimens)
    {
        supplementTableModel.setModel(regimens);
    }


    /**
     * Called by the {@code importMenuItem}'s {@code ActionListener}
     * in {@code VitaReminderFrame} to programatically select the
     * first row in the regimens table after the user imports
     * data from a backup.
     */
    public void selectFirstRegimenRow()
    {
        regimenTable.setRowSelectionInterval(0, 0);
    }

}  // end class VitaReminderPanel
