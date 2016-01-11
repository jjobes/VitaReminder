package com.vitareminder.dao;

import java.sql.Connection;

import com.vitareminder.dao.impl.DbDAOImpl;
import com.vitareminder.dao.impl.RegimenDAOImpl;
import com.vitareminder.dao.impl.SupplementDAOImpl;
import com.vitareminder.db.DataSource;


/**
 * This class represents the DAO layer for the application.  It enables
 * these classes to perform data operations, close the database connection,
 * or commit the current transaction without directly interacting with the
 * datasource.
 * <p>
 * A single instance of this object is created in {@code VitaReminder} and
 * is passed to multiple classes, most notably {@code VitaReminderFrame},
 * {@code VitaReminderPanel}, {@code RegimenTableModel} and {@code SupplementTableModel}.
 * <p>
 * When {@code DAOManager} creates either a {@code RegimenDAO} or {@code SupplementDAO},
 * the same connection object is passed into each of their constructors.  This ensures
 * that the application only ever works with a single connection, and therefore a single
 * transaction.  This has the effect that the single {@code commitTransaction()} function
 * will commit the changes that were initiated by both the {@code RegimenDAO} and
 * {@code SupplementDAO} objects.
 * <p>
 * This class can also return a reference to a {@code DbDAO} object, which provides
 * facilities for backing up and restoring the database.
 */
public class DAOManager
{
    private DataSource dataSource = null;
    private Connection connection = null;

    private RegimenDAO regimenDAO = null;
    private SupplementDAO supplementDAO = null;
    private DbDAO dbDAO = null;


    /**
     * The sole constructor.
     *
     * @param dataSource  a {@code DataSource} object that maintains a connection to the database
     */
    public DAOManager(DataSource dataSource)
    {
        this.dataSource = dataSource;
        this.connection = this.dataSource.getConnection();
    }


    /**
     * Gets a RegimenDAO object, which enables the client class to perform
     * CRUD operations on the regimens table.
     *
     * @return an implementation of the RegimenDAO interface
     */
    public RegimenDAO getRegimenDAO()
    {
        if (regimenDAO == null)
        {
            regimenDAO = new RegimenDAOImpl(connection);
        }

        return regimenDAO;
    }


    /**
     * Gets a SupplementDAO object, which enables the client class to perform
     * CRUD operations on the supplements table.
     *
     * @return an implementation of the SupplementDAO interface
     */
    public SupplementDAO getSupplementDAO()
    {
        if (supplementDAO == null)
        {
            supplementDAO = new SupplementDAOImpl(connection);
        }

        return supplementDAO;
    }


    /**
     * Gets a DbDAO object, which enables the client class to backup and
     * restore the database.
     *
     * @return an implementation of the DbDAO interface
     */
    public DbDAO getDbDAO()
    {
        if (dbDAO == null)
        {
            dbDAO = new DbDAOImpl(connection);
        }

        return dbDAO;
    }


    /**
     * Closes the current database connection.  Called by the shutdown hook
     * in the {@code VitaReminder} class.
     */
    public void closeDatabaseConnection()
    {
        dataSource.closeDatabaseConnection();
    }


    /**
     * Commits the current database transaction.  The connection's auto-commit
     * mode is set to false, so we must manually commit the transaction to
     * commit any changes to the actual database.  This is how the "save"
     * functionality is implemented in this application.
     */
    public void commitTransaction()
    {
        dataSource.commitTransaction();
    }

}  // end class DAOManager
