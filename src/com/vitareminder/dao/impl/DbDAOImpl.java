package com.vitareminder.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.vitareminder.dao.DbDAO;

/**
 * This class implements the methods in the {@code DbDAO} interface. These
 * methods, {@code backupDatabase()} and {@code restoreDatabase()}, represent
 * high-level backup and restore operations.
 */
public class DbDAOImpl implements DbDAO
{
    private Connection connection = null;

    private Logger logger = Logger.getLogger(DbDAOImpl.class);

    /**
     * The sole constructor. Uses a reference to the same connection object used
     * by {@code RegimenDAOImpl} and {@code SupplementDAOImpl}.
     *
     * @param connection
     *            a connection to the datasource, with auto-commit set to false
     */
    public DbDAOImpl(Connection connection)
    {
        this.connection = connection;
    }

    /**
     * Exports a SQL script that contains the commands to create and populate
     * the current database tables. The DROP option is specified so that the SQL
     * script contains the commands to drop any pre-existing tables prior to
     * recreating them.
     *
     * @param filePath
     *            the absolute file path to the backup file that the user has
     *            specified in the {@code JFileChooser}
     */
    public boolean backupDatabase(String filePath)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String backup = "SCRIPT DROP TO " + "'" + filePath + "'";

            preparedStatement = connection.prepareStatement(backup);
            preparedStatement.execute();

            return true;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                    "Sorry, a database error has occurred.\n"
                  + "Your data has not been saved.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);

            return false;
        }
        finally
        {
            try
            {
                if (preparedStatement != null)
                {
                    preparedStatement.close();
                }
            }
            catch (SQLException e)
            {
                logger.warn("A database error has occurred.", e);
                JOptionPane.showMessageDialog(null,
                        "Sorry, a database error has occurred.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Imports the SQL script specified by {@code filePath} and executes the
     * script, which creates and populates the regimens and supplements tables.
     *
     * @param filePath
     *            the absolute path to the backup file that the user has
     *            specified in the {@code JFileChooser}
     */
    public boolean restoreDatabase(String filePath)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String backup = "RUNSCRIPT FROM " + "'" + filePath + "'";

            preparedStatement = connection.prepareStatement(backup);
            preparedStatement.execute();

            return true;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane
                    .showMessageDialog(
                            null,
                            "Sorry, a problem was encountered while processing your file.\n\n"
                          + "Please ensure that the file that you are attempting\n"
                          + "to import is a valid .vrdata file.",
                            "Import Error", JOptionPane.ERROR_MESSAGE);

            return false;
        }
        finally
        {
            try
            {
                if (preparedStatement != null)
                {
                    preparedStatement.close();
                }
            }
            catch (SQLException e)
            {
                logger.warn("A database error has occurred.", e);
                JOptionPane.showMessageDialog(null,
                        "Sorry, a database error has occurred.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

} // end class DbDAOImpl
