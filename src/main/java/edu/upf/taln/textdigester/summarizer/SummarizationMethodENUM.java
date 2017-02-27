/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer;

import edu.upf.taln.textdigester.resource.gate.GtUtils;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public enum SummarizationMethodENUM {
	TextRank_TFIDF, TextRank_EMBED, Centroid_TFIDF, Centroid_EMBED, 
	FirstSim, SemScore, TFscore, Position, Centroid_TFIDF_SUMMA;
}