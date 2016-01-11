package com.vitareminder.ui;

/**
 * This interface is implemented by {@code SupplementDialog}, which is
 * extended by {@code AddSupplementDialog} and {@code EditSupplementDialog}.
 * It indicates that the implementing class is capable of validating these
 * fields, and helps to clarify which fields of the {@code Supplement} class
 * require validation.
 */
public interface SupplementInputValidator
{
    /**
     * This method should ensure that a name is present and also that
     * the name does not exceed a certain length.
     *
     * @return  <tt>true</tt> if all the validation requirements are met,
     * 			<tt>false</tt> otherwise
     */
    boolean validateSuppName();


    /**
     * This method should ensure that, if present, the text entered into the amount
     * field can be parsed as a {@code double}, and that it is non-negative.
     *
     * @return  <tt>true</tt> if all the validation requirements are met,
     * 			<tt>false</tt> otherwise
     */
    boolean validateSuppAmount();


    /**
     * This method should ensure that the notes fall within certain length
     * restrictions.  It should allow for an empty {@code String}.
     *
     * @return  <tt>true</tt> if all the validation requirements are met,
     * 			<tt>false</tt> otherwise
     */
    boolean validateSuppNotes();
}
