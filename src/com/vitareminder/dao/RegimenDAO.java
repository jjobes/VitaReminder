package com.vitareminder.dao;

import java.util.List;

import com.vitareminder.business.Regimen;


/**
 * An interface in which the implementing class must implement all four basic
 * CRUD operations relating to regimens.  Implemented by {@code RegimenDAOImpl}.
 */
public interface RegimenDAO
{
    public List<Regimen> getRegimens();
    public Regimen addRegimen(Regimen regimen);
    public boolean deleteRegimen(int regimenID);
    public boolean deleteAllRegimens();
    public boolean updateRegimen(int regimenID, String field, Object value);
}
