/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.method;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.upf.taln.textdigester.model.TDDocument;
import gate.Annotation;

/**
 * Interface of multi-document extractive summarizer
 * 
 * @author Francesco Ronzano
 *
 */
public interface ExtractiveSummarizerMulti {

	Map<Entry<Annotation, TDDocument>, Double> sortSentences(List<TDDocument> docList);
	
}
