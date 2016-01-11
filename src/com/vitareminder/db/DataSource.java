package com.vitareminder.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;


/**
 * This clsas directly manages the JDBC connection to the database.
 * The present implementation uses an embedded H2 database.  The connection's
 * auto-commit mode is set to false.  The current transaction is committed
 * only when the user presses the save button or clicks on the save
 * menu item.
 */
public class DataSource
{
    private String url = DBConstants.DATABASE_URL;
    private String dbName = DBConstants.DATABASE_NAME;
    private String username = DBConstants.USERNAME;
    private String password = DBConstants.PASSWORD;

    private Connection connection = null;

    private Logger logger = Logger.getLogger(DataSource.class);


    /**
     * The sole constructor.  This establishes a connection to the
     * vitareminder_db database and sets this connection's auto-commit
     * mode to false.
     */
    public DataSource()
    {
        try
        {
            connection = DriverManager.getConnection(url + dbName, username, password);
            connection.setAutoCommit(false);

            logger.info("Connected to database.");
        }
        catch (SQLException e)
        {
            logger.fatal("There was an error connecting to the database.", e);
            JOptionPane.showMessageDialog(null,
                                          "<html>Sorry, there was an error connecting to the database.<br><br>"
                                        + "The application must now exit.</html>",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }


    /**
     * Gets a connection to the database.  Supplies the {@code DAOManager} with its
     * connection, which in turn supplies the {@code RegimenDAOImpl} and
     * {@code SupplementDAOImpl} objects with their connections.
     *
     * @return a connection to the database with auto-commit set to false
     */
    public Connection getConnection()
    {
        if (connection != null)
        {
            return connection;
        }

        return null;
    }


    /**
     * Closes the database connection.  The shutdown hook in {@code VitaReminder}
     * calls this indirectly by calling {@code DAOManager#closeDatabaseConnection()}.
     */
    public void closeDatabaseConnection()
    {
        if (connection != null)
        {
            try
            {
                connection.close();

                logger.info("Disconnected from database.");
            }
            catch (SQLException e)
            {
                logger.warn("There was an error disconnecting from the database.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, there was an error disconnecting from the database.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Commits the current database transaction.  The {@code VitaReminderFrame}
     * and {@code VitaReminderPanel} classes call this indirectly by calling
     * {@code DAOManager#commitTransaction()}.
     */
    public void commitTransaction()
    {
        if (connection != null)
        {
            try
            {
                connection.commit();
            }
            catch (SQLException e)
            {
                logger.warn("Error saving data to database.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, there was an error saving to the database.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}  // end class DataSource
