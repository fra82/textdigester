package edu.upf.taln.textdigester;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.importer.HTMLimporter;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.method.SentenceSimilarityENUM;
import edu.upf.taln.textdigester.summarizer.method.lexrank.LexRankSummarizer;
import gate.Annotation;

public class SummarizeDocumentExample {
	
	private static final Logger logger = LoggerFactory.getLogger(SummarizeDocumentExample.class);
	
	public static void main(String[] args) {
		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");
		
		/* Extract main-text from HTML page and parse it */
		TDDocument HTMLdoc = null;
		try {
			HTMLdoc = HTMLimporter.extractText(new URL("http://politica.elpais.com/politica/2017/02/22/actualidad/1487762346_547683.html"));
			logger.debug("TEXT: " + HTMLdoc.getOriginalText());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Process text document */
		LangENUM languageOfHTMLdoc = FlProcessor.getLanguage(HTMLdoc.getOriginalText());
		FlProcessor flProc = null;
		try {
			flProc = new FlProcessor(languageOfHTMLdoc);
		}
		catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		HTMLdoc = flProc.parseDocumentSentences(HTMLdoc);
		
		try {
			LexRankSummarizer lexRank = new LexRankSummarizer(languageOfHTMLdoc, SentenceSimilarityENUM.cosineTFIDF, false, 0.01);
			Map<Annotation, Double> sortedSentences = lexRank.sortSentences(HTMLdoc);
			
			List<Annotation> sentListOrderedByRelevance = new ArrayList<Annotation>();
			for(Entry<Annotation, Double> sentence : sortedSentences.entrySet()) {
				logger.info("Score: " + sentence.getValue() + " - '" + GtUtils.getTextOfAnnotation(sentence.getKey(), HTMLdoc.getGATEdoc()) + "'");
				sentListOrderedByRelevance.add(sentence.getKey());
			}
			
			logger.info("Summary max 100 tokens: ");
			List<Annotation> summarySentences = GtUtils.orderAnnotations(sentListOrderedByRelevance, HTMLdoc.getGATEdoc(), 100);
			for(Annotation ann : summarySentences) {
				logger.info(GtUtils.getTextOfAnnotation(ann, HTMLdoc.getGATEdoc()));
			}
		} catch (TextDigesterException e) {
			e.printStackTrace();
		}
		
	}

}
