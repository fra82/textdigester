/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.method.SentenceSimilarityENUM;
import edu.upf.taln.textdigester.summarizer.method.centroid.CentroidSummarizer;
import edu.upf.taln.textdigester.summarizer.method.lexrank.LexRankSummarizer;
import edu.upf.taln.textdigester.summarizer.method.multi.centroid.CentroidSummarizerMulti;
import edu.upf.taln.textdigester.summarizer.summa.CallSUMMA;
import gate.Annotation;

/**
 * This class provides static method to rank the sentences of a document by means of one of the summarization approaches implemented by
 * TextDigester. Sentences with an higher score are the more suitable to summarize the content of the document.
 * 
 * @author Francesco Ronzano
 *
 */
public class ConfigurableSummarizer {

	/**
	 * Invokes a summarization approach to resume a document in a specific language.<br/>
	 * Inspect the code of the {@link edu.upf.taln.textdigester.CoreExample CoreExample} class in order to navigate a complete
	 * example of how this method can be used.
	 * 
	 * @param doc the document to summarize
	 * @param lang language of the document
	 * @param approach one of the values of {@link edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM SummarizationMethodENUM},
	 * except CentroidMultiDoc_TFIDF / CentoridMultiDoc_EMDBED
	 * @return
	 * @throws TextDigesterException
	 */
	public static Map<Annotation, Double> summarize(TDDocument doc, LangENUM lang, SummarizationMethodENUM approach) throws TextDigesterException {

		if(doc == null || lang == null || doc.getGATEdoc() == null) {
			throw new TextDigesterException("Parameter error");
		}
		
		CallSUMMA.analyze(doc.getGATEdoc(), lang);
		
		switch(approach) {
		case LexRank_TFIDF:
			LexRankSummarizer lexRank_TFIDF = new LexRankSummarizer(lang, SentenceSimilarityENUM.cosineTFIDF, false, 0.01);
			return lexRank_TFIDF.sortSentences(doc);

		case LexRank_EMBED:
			LexRankSummarizer lexRank_EMBED = new LexRankSummarizer(lang, SentenceSimilarityENUM.cosineEMBED, false, 0.01);
			return lexRank_EMBED.sortSentences(doc);

		case Centroid_TFIDF:
			CentroidSummarizer centroid_TFIDF = new CentroidSummarizer(lang, SentenceSimilarityENUM.cosineEMBED);
			return centroid_TFIDF.sortSentences(doc);

		case Centroid_EMBED:
			CentroidSummarizer centroid_EMBED = new CentroidSummarizer(lang, SentenceSimilarityENUM.cosineEMBED);
			return centroid_EMBED.sortSentences(doc);
			
		case FirstSim:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "first_sim");
		
		case TFscore:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "tf_score");
			
		case Position:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "position");
			
		case Centroid_TFIDF_SUMMA:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "sent_doc_sim");
			
		case SemScore:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "sem_score");
			
		}

		return null;
	}
	
	/**
	 * Invokes a summarization approach to resume a set of documents in a specific language.<br/>
	 * 
	 * @param docList list of documents to summarize
	 * @param lang language of the documents
	 * @param approach summarization approach (one of CentroidMultiDoc_TFIDF / CentoridMultiDoc_EMDBED)
	 * @return
	 * @throws TextDigesterException
	 */
	public static Map<Entry<Annotation, TDDocument>, Double> summarizeMultiDoc(List<TDDocument> docList, LangENUM lang, SummarizationMethodENUM approach) throws TextDigesterException {

		if(docList == null || lang == null || docList.size() == 0) {
			throw new TextDigesterException("Parameter error");
		}
		
		for(TDDocument doc : docList) {
			CallSUMMA.analyze(doc.getGATEdoc(), lang);
		}
		
		switch(approach) {
		case CentroidMultiDoc_TFIDF:
			CentroidSummarizerMulti lexRank_CentroidMultiDoc_TFIDF = new CentroidSummarizerMulti(lang, SentenceSimilarityENUM.cosineTFIDF);
			return lexRank_CentroidMultiDoc_TFIDF.sortSentences(docList);

		case CentoridMultiDoc_EMDBED:
			CentroidSummarizerMulti lexRank_CentoridMultiDoc_EMDBED = new CentroidSummarizerMulti(lang, SentenceSimilarityENUM.cosineEMBED);
			return lexRank_CentoridMultiDoc_EMDBED.sortSentences(docList);

		default:
			throw new TextDigesterException("Error in summarization method specification");
			
		}
		
	}


}
