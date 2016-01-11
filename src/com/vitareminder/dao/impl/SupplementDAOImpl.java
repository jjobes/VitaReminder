package com.vitareminder.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.vitareminder.business.Supplement;
import com.vitareminder.dao.SupplementDAO;


/**
 * Implements the methods defined in the {@code SupplementDAO} interface
 * by performing SQL queries on the supplements table in the vitareminder_db
 * database.
 */
public class SupplementDAOImpl implements SupplementDAO
{
    private Connection connection = null;

    private Logger logger = Logger.getLogger(SupplementDAOImpl.class);


    /**
     * The sole constructor.  Uses a reference to the same connection object used
     * by {@code RegimenDAOImpl}.
     *
     * @param connection  a connection to the datasource, with auto-commit set to
     *                    false
     */
    public SupplementDAOImpl(Connection connection)
    {
        this.connection = connection;
    }


    /**
     * Gets a {@code List} of {@code Supplement} objects, in ascending
     * order by {@code supplementID}.  Called by {@code SupplementTableModel}.
     *
     * @return a {@code List} of {@code Supplement} objects
     */
    public List<Supplement> getSupplements(int regimenID)
    {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Supplement> supplements = new ArrayList<Supplement>();

        String query = "SELECT * FROM supplements "
                     + "WHERE regimen_id = ? "
                     + "ORDER BY supp_id ASC";

        try
        {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, regimenID);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                Supplement supplement = new Supplement();

                supplement.setSuppID(resultSet.getInt("supp_id"));
                supplement.setRegimenID(resultSet.getInt("regimen_id"));
                supplement.setSuppName(resultSet.getString("supp_name"));
                supplement.setSuppAmount(resultSet.getDouble("supp_amount"));
                supplement.setSuppUnits(resultSet.getString("supp_units"));
                supplement.setSuppTime(resultSet.getTime("supp_time"));
                supplement.setEmailEnabled(resultSet.getBoolean("supp_email_enabled"));
                supplement.setTextEnabled(resultSet.getBoolean("supp_text_enabled"));
                supplement.setVoiceEnabled(resultSet.getBoolean("supp_voice_enabled"));
                supplement.setSuppNotes(resultSet.getString("supp_notes"));

                supplements.add(supplement);
            }

            return supplements;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, a database error has occurred.",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }

                if (preparedStatement != null)
                {
                    preparedStatement.close();
                }
            }
            catch (SQLException e)
            {
                logger.warn("A database error has occured.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, a database error has occurred.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        return null;
    }


    /**
     * Gets a {@code List} of {@code Supplement} objects, in ascending
     * order by {@code supplementID}.  Called by {@code ReminderManager}.
     *
     * @return a {@code List} of {@code Supplement} objects
     */
    public ArrayList<Supplement> getSupplementsWithReminders()
    {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ArrayList<Supplement> reminderSupplements = new ArrayList<Supplement>();

        String query = "SELECT * FROM supplements "
                     + "WHERE supp_email_enabled = 1 OR supp_text_enabled = 1 OR supp_voice_enabled = 1 "
                     + "ORDER BY supp_id ASC";

        try
        {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                Supplement supplement = new Supplement();

                supplement.setSuppID(resultSet.getInt("supp_id"));
                supplement.setRegimenID(resultSet.getInt("regimen_id"));
                supplement.setSuppName(resultSet.getString("supp_name"));
                supplement.setSuppAmount(resultSet.getDouble("supp_amount"));
                supplement.setSuppUnits(resultSet.getString("supp_units"));
                supplement.setSuppTime(resultSet.getTime("supp_time"));
                supplement.setEmailEnabled(resultSet.getBoolean("supp_email_enabled"));
                supplement.setTextEnabled(resultSet.getBoolean("supp_text_enabled"));
                supplement.setVoiceEnabled(resultSet.getBoolean("supp_voice_enabled"));
                supplement.setSuppNotes(resultSet.getString("supp_notes"));

                reminderSupplements.add(supplement);
            }

            resultSet.close();
            preparedStatement.close();

            return reminderSupplements;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, a database error has occurred.",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);

            return null;
        }
        finally
        {
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }

                if (preparedStatement != null)
                {
                    preparedStatement.close();
                }
            }
            catch (SQLException e)
            {
                logger.warn("A database error has occured.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, a database error has occurred.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Adds the specified {@code Supplement} to the supplements table.  After inserting
     * the new row, it queries the result set to retrieve the primary key that was just
     * generated.  It then inserts that primary key into the {@code suppID} field in the
     * {@code Supplement} object.
     *
     * @return the {@code Supplement} object just inserted with its {@code suppID}
     */
    public Supplement addSupplement(Supplement supplement)
    {
        PreparedStatement preparedStatement1 = null;
        ResultSet resultSet1 = null;
        PreparedStatement preparedStatement2 = null;
        ResultSet resultSet2 = null;

        try
        {
            String insert = "INSERT INTO supplements (regimen_id, supp_name, supp_amount, supp_units,"
                          +                          "supp_time, supp_email_enabled, supp_text_enabled, "
                          +                          "supp_voice_enabled, supp_notes) "
                          +                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            preparedStatement1 = connection.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement1.setInt(1, (int) supplement.getRegimenID());
            preparedStatement1.setString(2, (String) supplement.getSuppName());
            preparedStatement1.setDouble(3, (double) supplement.getSuppAmount());
            preparedStatement1.setString(4, (String) supplement.getSuppUnits());
            preparedStatement1.setTime(5, (java.sql.Time) supplement.getSuppTime());
            preparedStatement1.setBoolean(6, (boolean) supplement.getEmailEnabled());
            preparedStatement1.setBoolean(7, (boolean) supplement.getTextEnabled());
            preparedStatement1.setBoolean(8, (boolean) supplement.getVoiceEnabled());
            preparedStatement1.setString(9, (String) supplement.getSuppNotes());
            preparedStatement1.executeUpdate();

            long primaryKey = -1;
            resultSet1 = preparedStatement1.getGeneratedKeys();
            if (resultSet1 != null && resultSet1.next())
            {
                primaryKey = resultSet1.getLong(1);
            }

            supplement.setSuppID((int) primaryKey);

            return supplement;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, a database error has occurred.",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            try
            {
                if (resultSet2 != null)
                {
                    resultSet2.close();
                }

                if (preparedStatement2 != null)
                {
                    preparedStatement2.close();
                }

                if (resultSet1 != null)
                {
                    resultSet1.close();
                }

                if (preparedStatement1 != null)
                {
                    preparedStatement1.close();
                }
            }
            catch (SQLException e)
            {
                logger.warn("A database error has occured.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, a database error has occurred.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        return null;
    }


    /**
     * Deletes the {@code Supplement} with the specified {@code suppID} from the
     * supplements table.
     *
     * @return <tt>true</tt> if the delete operation was successful, <tt>false</tt> otherwise
     */
    public boolean deleteSupplement(int suppID)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String delete = "DELETE FROM supplements "
                          + "WHERE supp_id = ?";

            preparedStatement = connection.prepareStatement(delete);
            preparedStatement.setInt(1, suppID);
            preparedStatement.executeUpdate();

            return true;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, a database error has occurred.",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);

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
                logger.warn("A database error has occured.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, a database error has occurred.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Deletes all rows from the supplements table.
     *
     * @return <tt>true</tt> if the delete operation was successful, <tt>false</tt> otherwise
     */
    public boolean deleteAllSupplements()
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String delete = "DELETE FROM supplements";

            preparedStatement = connection.prepareStatement(delete);
            preparedStatement.executeUpdate();

            return true;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, a database error has occurred.",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);

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
                logger.warn("A database error has occured.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, a database error has occurred.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Updates the specified {@code field} with the specified {@code value}
     * for the row in the supplements table associated with {@code suppID}.
     *
     * @return <tt>true</tt> if the update was successful, <tt>false</tt> otherwise
     */
    public boolean updateSupplement(int suppID, String field, Object value)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String update = "UPDATE supplements SET "
                          + field + " = ? "
                          + "WHERE supp_id = ?";

            preparedStatement = connection.prepareStatement(update);
            preparedStatement.setObject(1, value);
            preparedStatement.setInt(2, suppID);
            preparedStatement.executeUpdate();

            return true;
        }
        catch (SQLException e)
        {
            logger.warn("A database error has occured.", e);
            JOptionPane.showMessageDialog(null,
                                          "Sorry, a database error has occurred.",
                                          "Database Error",
                                          JOptionPane.ERROR_MESSAGE);

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
                logger.warn("A database error has occured.", e);
                JOptionPane.showMessageDialog(null,
                                              "Sorry, a database error has occurred.",
                                              "Database Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}  // end class SupplementDAOImpl
