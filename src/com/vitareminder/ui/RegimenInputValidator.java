package com.vitareminder.ui;

/**
 * This interface is implemented by {@code RegimenDialog}, which is
 * extended by {@code AddRegimenDialog} and {@code EditRegimenDialog}.
 * It indicates that the implementing class is capable of validating
 * these fields, and helps to clarify which fields of the {@code Regimen}
 * class require validation.
 */
public interface RegimenInputValidator
{
    /**
     * This method should ensure that a name is present and also that
     * the name falls within certain length restrictions.
     *
     * @return  <tt>true</tt> if all the validation requirements are met,
     *          <tt>false</tt> otherwise
     */
    boolean validateRegimenName();


    /**
     * This method should ensure that the notes fall within certain length
     * restrictions.  It should allow for an empty {@code String}.
     *
     * @return  <tt>true</tt> if all the validation requirements are met,
     *          <tt>false</tt> otherwise
     */
    boolean validateRegimenNotes();
}
