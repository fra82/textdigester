/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public enum SummarizationMethodENUM {
	LexRank_TFIDF, LexRank_EMBED, Centroid_TFIDF, Centroid_EMBED, CentroidMultiDoc_TFIDF, CentoridMultiDoc_EMDBED, 
	FirstSim, SemScore, TFscore, Position, Centroid_TFIDF_SUMMA;
}