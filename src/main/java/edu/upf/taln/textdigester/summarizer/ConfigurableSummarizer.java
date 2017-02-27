package edu.upf.taln.textdigester.summarizer;

import java.util.Map;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.method.SentenceSimilarityENUM;
import edu.upf.taln.textdigester.summarizer.method.centroid.CentroidSummarizer;
import edu.upf.taln.textdigester.summarizer.method.lexrank.LexRankSummarizer;
import edu.upf.taln.textdigester.summarizer.summa.CallSUMMA;
import gate.Annotation;

public class ConfigurableSummarizer {

	public static Map<Annotation, Double> summarize(TDDocument doc, LangENUM lang, SummarizationMethodENUM approach) throws TextDigesterException {

		if(doc == null || lang == null || doc.getGATEdoc() == null) {
			throw new TextDigesterException("Parameter error");
		}
		
		CallSUMMA.analyze(doc.getGATEdoc(), lang);
		
		switch(approach) {
		case TextRank_TFIDF:
			LexRankSummarizer lexRank_TFIDF = new LexRankSummarizer(lang, SentenceSimilarityENUM.cosineTFIDF, false, 0.01);
			return lexRank_TFIDF.sortSentences(doc);

		case TextRank_EMBED:
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
			
		case SemScore:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "sem_score");
			
		case NEScore:
			return GtUtils.orderSentencesBySentFeatValue(doc.getGATEdoc(), "NE_score");
		}

		return null;
	}


}
