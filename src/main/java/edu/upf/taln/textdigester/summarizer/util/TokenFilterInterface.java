package edu.upf.taln.textdigester.summarizer.util;

import gate.Annotation;
import gate.Document;

import java.util.List;

public interface TokenFilterInterface {
	List<Integer> getTokenListNotToConsider(Annotation ann, Document doc);
}
