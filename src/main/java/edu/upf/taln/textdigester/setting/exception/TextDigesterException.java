/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.setting.exception;

/**
 * Base class for the Exception hierarchy of TextDigester Framework
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
