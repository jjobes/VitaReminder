package com.vitareminder.dao;

import java.util.List;

import com.vitareminder.business.Supplement;


/**
 * An interface in which the implementing class must implement all four basic
 * CRUD operations relating to supplements.  Implemented by {@code SupplementDAOImpl}.
 */
public interface SupplementDAO
{
    public List<Supplement> getSupplements(int regimenID);
    public List<Supplement> getSupplementsWithReminders();
    public Supplement addSupplement(Supplement supplement);
    public boolean deleteSupplement(int suppID);
    public boolean deleteAllSupplements();
    public boolean updateSupplement(int suppID, String field, Object value);
}
