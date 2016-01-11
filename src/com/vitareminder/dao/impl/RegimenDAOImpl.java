package com.vitareminder.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.vitareminder.business.Regimen;
import com.vitareminder.business.Supplement;
import com.vitareminder.dao.RegimenDAO;


/**
 * This class implements the methods defined in the {@code RegimenDAO} interface
 * by performing SQL queries on the regimens and supplements tables in the
 * vitareminder_db database.
 */
public class RegimenDAOImpl implements RegimenDAO
{
    private Connection connection = null;

    private Logger logger = Logger.getLogger(RegimenDAOImpl.class);


    /**
     * The sole constructor.  Uses a reference to the same connection object used
     * by {@code SupplementDAOImpl}.
     *
     * @param connection  a connection to the datasource, with auto-commit
     *                    set to false
     */
    public RegimenDAOImpl(Connection connection)
    {
        this.connection = connection;
    }


    /**
     * Gets a {@code List} of {@code Regimen} objects, where each {@code Regimen}
     * contains a {@code List} of {@code Supplement} objects.
     * <p>
     * The operation performed on the database is a left outer join to ensure that only
     * a single query is made to the database with each call to this method.  However,
     * the resultset table that is returned requires some special processing.  This is
     * because there is no way of knowing beforehand, as the cursor moves forward one row
     * at a time, whether or not the next row will contain the same regimen or a different
     * one.  We therefore have to store each unique {@code Regimen} in one {@code Map}
     * mapped to its {@code regimenID}, and a separate {@code Map} will map each {@code regimenID}
     * to its {@code Supplement}s.
     * <p>
     * Once we have these two {@code Map}s, we can reconstruct a {@code List} of
     * {@code Regimen}s, and insert the appropriate list of {@code Supplement}s into the
     * appropriate {@code Regimen}.
     *
     * @return a {@code List} of {@code Regimen} objects, each containing a {@code List}
     *         of its {@code Supplement}s
     */
    public List<Regimen> getRegimens()
    {
        Map<Integer, ArrayList<Supplement>> supplements = new HashMap<>();
        Map<Integer, Regimen> regimens = new TreeMap<>();  // regimens sorted by regimenID

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String query = "SELECT r.regimen_id, r.regimen_name, r.regimen_notes, "
                     + "s.supp_id, s.supp_name, s.supp_amount, s.supp_units, "
                     + "s.supp_time, s.supp_email_enabled, s.supp_text_enabled, "
                     + "s.supp_voice_enabled, s.supp_notes "
                     + "FROM regimens AS r LEFT JOIN supplements AS s "
                     + "ON r.regimen_id = s.regimen_id";

        try
        {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                Integer regimenID = resultSet.getInt("regimen_id");
                Regimen regimen = regimens.get(regimenID);
                if (regimen == null)
                {
                    regimen = new Regimen();
                    regimen.setRegimenID(regimenID);
                    regimen.setRegimenName(resultSet.getString("regimen_name"));
                    regimen.setRegimenNotes(resultSet.getString("regimen_notes"));
                    regimens.put(regimenID, regimen);
                    supplements.put(regimenID, new ArrayList<Supplement>());
                }

                Integer supplementID = resultSet.getInt("supp_id");

                if (!resultSet.wasNull())  // Is there a supplement on this row?
                {
                    List<Supplement> supplementsList = supplements.get(regimenID);
                    Supplement supplement = new Supplement();
                    supplement.setSuppID(supplementID);
                    supplement.setRegimenID(resultSet.getInt("regimen_id"));
                    supplement.setSuppName(resultSet.getString("supp_name"));
                    supplement.setSuppAmount(resultSet.getDouble("supp_amount"));
                    supplement.setSuppUnits(resultSet.getString("supp_units"));
                    supplement.setSuppTime(resultSet.getTime("supp_time"));
                    supplement.setEmailEnabled(resultSet.getBoolean("supp_email_enabled"));
                    supplement.setTextEnabled(resultSet.getBoolean("supp_text_enabled"));
                    supplement.setVoiceEnabled(resultSet.getBoolean("supp_voice_enabled"));
                    supplement.setSuppNotes(resultSet.getString("supp_notes"));
                    supplementsList.add(supplement);
                }
            }

            // Using the two Maps, build the list of Regimens, each containing
            // its list of Supplements.
            List<Regimen> result = new ArrayList<>();
            for (Regimen r : regimens.values())
            {
                r.setSupplements(supplements.get(r.getRegimenID()));
                result.add(r);
            }

            return result;
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
     * Adds the specified {@code Regimen} to the regimens table.  After inserting
     * the new row, it queries the result set to retrieve the primary key of the
     * {@code Regimen} just inserted.  It then builds a complete {@code Regimen}
     * object that includes its {@code regimenID}.
     *
     * @return the {@code Regimen} object just inserted with its {@code regimenID}
     */
    public Regimen addRegimen(Regimen regimen)
    {
        PreparedStatement preparedStatement1 = null;
        ResultSet resultSet1 = null;
        PreparedStatement preparedStatement2 = null;
        ResultSet resultSet2 = null;

        try
        {
            String insert = "INSERT INTO regimens (regimen_name, regimen_notes) "
                          + "VALUES (?, ?)";

            preparedStatement1 = connection.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement1.setString(1, (String) regimen.getRegimenName());
            preparedStatement1.setString(2, (String) regimen.getRegimenNotes());
            preparedStatement1.executeUpdate();

            long primaryKey = -1;
            resultSet1 = preparedStatement1.getGeneratedKeys();
            if (resultSet1 != null && resultSet1.next())
            {
                primaryKey = resultSet1.getLong(1);
            }

            regimen.setRegimenID((int) primaryKey);

            return regimen;
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
     * Deletes the {@code Regimen} with the specified {@code regimenID} from the
     * regimens table.
     *
     * @return <tt>true</tt> if the delete operation was successful, <tt>false</tt> otherwise
     */
    public boolean deleteRegimen(int regimenID)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String delete = "DELETE FROM regimens "
                          + "WHERE regimen_id = ?";

            preparedStatement = connection.prepareStatement(delete);
            preparedStatement.setInt(1, regimenID);
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
     * Deletes all rows from the regimens table.
     *
     * @return <tt>true</tt> if the delete operation was successful, <tt>false</tt> otherwise
     */
    public boolean deleteAllRegimens()
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String delete = "DELETE FROM regimens";

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
     * for the row in the regimens table associated with {@code regimenID}.
     *
     * @return <tt>true</tt> if the update was successful, <tt>false</tt> otherwise
     */
    public boolean updateRegimen(int regimenID, String field, Object value)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            String update = "UPDATE regimens SET "
                          + field + " = ? "
                          + "WHERE regimen_id = ?";

            preparedStatement = connection.prepareStatement(update);
            preparedStatement.setObject(1, value);
            preparedStatement.setInt(2, regimenID);
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

}  // end class RegimenDAOImpl

