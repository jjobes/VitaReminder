package com.vitareminder.dao;

/**
 * An interface in which the implementing class must provide
 * functions to perform the high-level database operations of
 * backing up and restoring the database.  Implemented by
 * {@code DbDAOImpl}.
 */
public interface DbDAO
{
    public boolean backupDatabase(String filePath);
    public boolean restoreDatabase(String filePath);
}
