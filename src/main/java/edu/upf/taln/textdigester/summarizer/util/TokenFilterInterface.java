/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.util;

import gate.Annotation;
import gate.Document;

import java.util.List;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public interface TokenFilterInterface {
	List<Integer> getTokenListNotToConsider(Annotation ann, Document doc);
}
