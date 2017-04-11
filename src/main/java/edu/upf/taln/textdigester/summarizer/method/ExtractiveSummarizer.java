/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.method;

import java.util.Map;

import edu.upf.taln.textdigester.model.TDDocument;
import gate.Annotation;

/**
 * Interface of single document extractive summarizer
 * 
 * @author Francesco Ronzano
 *
 */
public interface ExtractiveSummarizer {

	public Map<Annotation, Double> sortSentences(TDDocument doc);
	
}
