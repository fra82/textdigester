package edu.upf.taln.textdigester.summarizer.method;

import java.util.Map;

import edu.upf.taln.textdigester.model.TDDocument;
import gate.Annotation;

public interface ExtractiveSummarizer {

	public Map<Annotation, Double> sortSentences(TDDocument doc);
	
}
