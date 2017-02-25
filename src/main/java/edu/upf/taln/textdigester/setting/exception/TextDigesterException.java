/*
 * ******************************************************************************************************
 * Dr. Inventor Text Mining Framework Java Library
 * 
 * This code has been developed by the Natural Language Processing Group of the
 * Universitat Pompey Fabra in the context of the FP7 European Project Dr. Inventor
 * Call: FP7-ICT-2013.8.1 - Agreement No: 611383
 * ******************************************************************************************************
 */
package edu.upf.taln.textdigester.setting.exception;

/**
 * Base class for the Exception hierarchy of Dr. Inventor Text Mining Framework Java Library.
 * 
 * @author Francesco Ronzano
 *
 */
public class TextDigesterException extends Exception {

	private static final long serialVersionUID = 1L;

	public TextDigesterException(String message) {
        super(message);
    }
	
}
