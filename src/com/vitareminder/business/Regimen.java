package com.vitareminder.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@code Regimen} represents a group of {@code Supplement}s.  A {@code Regimen}
 * object keeps track of its name and notes.  It contains an {@code ArrayList} of
 * {@code Supplement} objects that are associated with this {@code Regimen}.
 */
public class Regimen implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int regimenID;
    private String regimenName;
    private String regimenNotes;
    private List<Supplement> supplements;


    /**
     * The default, no-argument constructor.  The default values are:
     * <ul>
     * <li>regimenID = 0</li>
     * <li>regimenName = ""</li>
     * <li>regimenNotes = ""</li>
     * <li>supplements = new ArrayList<Supplement>()</li>
     * </ul>
     */
    public Regimen()
    {
        regimenID = 0;
        regimenName = "";
        regimenNotes = "";
        supplements = new ArrayList<Supplement>();
    }


    /**
     * The multiple-argument constructor.
     *
     * @param regimenID  the unique ID for this regimen, which corresponds to its primary key in the
     *                   regimens table in the database
     * @param regimenName  the user-assigned name for this regimen
     * @param regimenNotes  the user-assigned notes for this regimen
     * @param supplements  the supplement objects that are associated with this regimen
     */
    public Regimen(int regimenID, String regimenName, String regimenNotes, ArrayList<Supplement> supplements)
    {
        this.regimenID = regimenID;
        this.regimenName = regimenName;
        this.regimenNotes = regimenNotes;
        this.supplements = supplements;
    }


    public void setRegimenID(int regimenID)
    {
        this.regimenID = regimenID;
    }


    public int getRegimenID()
    {
        return regimenID;
    }


    public void setRegimenName(String regimenName)
    {
        this.regimenName = regimenName;
    }


    public String getRegimenName()
    {
        return regimenName;
    }


    public void setRegimenNotes(String regimenNotes)
    {
        this.regimenNotes = regimenNotes;
    }


    public String getRegimenNotes()
    {
        return regimenNotes;
    }


    public void setSupplements(ArrayList<Supplement> supplements)
    {
        this.supplements = supplements;
    }


    public ArrayList<Supplement> getSupplements()
    {
        return (ArrayList<Supplement>) supplements;
    }


    public String toString()
    {
        return regimenName;
    }

}  // end class Regimen
