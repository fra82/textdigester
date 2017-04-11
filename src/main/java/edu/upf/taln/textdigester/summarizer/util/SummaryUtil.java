/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import gate.Annotation;

/**
 * Utility methods to generate extractive summaries
 * 
 * @author Francesco Ronzano
 *
 */
public class SummaryUtil {

	private static final Logger logger = LoggerFactory.getLogger(SummaryUtil.class);
	
	/**
	 * Given a map of ranked document sentences, select the top %.<br/>
	 * See the class {@link edu.upf.taln.textdigester.summarizer.ConfigurableSummarizer ConfigurableSummarizer} to know how to generate
	 * the map of ranked document sentences.
	 * 
	 * @param orderedSentences_SemScore map of ranked document sentences
	 * @param doc document to summarize
	 * @param percentageOfOriginalDocument top %
	 * @return
	 */
	public static Map<Annotation, Double> getSummary(Map<Annotation, Double> orderedSentences_SemScore, TDDocument doc, double percentageOfOriginalDocument) {
		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

		if(orderedSentences_SemScore == null || orderedSentences_SemScore.size() == 0 || doc == null) {
			return null;
		}

		if(percentageOfOriginalDocument < 0 && percentageOfOriginalDocument > 100) {
			logger.warn("Length percentage of summary wrongly defined - set to 30%.");
		}

		percentageOfOriginalDocument = (percentageOfOriginalDocument > 0 && percentageOfOriginalDocument < 100) ? percentageOfOriginalDocument : 30d;

		String docText = "";		
		for(Entry<Annotation, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {
			System.out.println("Sent: " + orderedSentences_SemScoreEntry.getKey().getId() + " > SCORE: " + orderedSentences_SemScoreEntry.getValue());
			docText += ((docText.length() > 0) ? " " : "") + GtUtils.getTextOfAnnotation(orderedSentences_SemScoreEntry.getKey(), doc.getGATEdoc()); 
		}

		double totalLengthDoc = docText.length();
		double totalLengthSummary = 0d;
		for(Entry<Annotation, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {

			retMap.put(orderedSentences_SemScoreEntry.getKey(), orderedSentences_SemScoreEntry.getValue());

			String annoText = GtUtils.getTextOfAnnotation(orderedSentences_SemScoreEntry.getKey(), doc.getGATEdoc());
			totalLengthSummary += annoText.length();
			if( ((totalLengthSummary / totalLengthDoc) * 100d) > percentageOfOriginalDocument) {
				break;
			}
		}

		return retMap;
	}

	public static Map<Map.Entry<Annotation, TDDocument>, Double> getSummary(Map<Map.Entry<Annotation, TDDocument>, Double> orderedSentences_SemScore, List<TDDocument> docList, double percentageOfOriginalDocument) {
		Map<Map.Entry<Annotation, TDDocument>, Double> retMap = new HashMap<Map.Entry<Annotation, TDDocument>, Double>();

		if(orderedSentences_SemScore == null || orderedSentences_SemScore.size() == 0 || docList == null || docList.size() == 0) {
			return null;
		}

		if(percentageOfOriginalDocument < 0 && percentageOfOriginalDocument > 100) {
			logger.warn("Length percentage of summary wrongly defined - set to 30%.");
		}

		percentageOfOriginalDocument = (percentageOfOriginalDocument > 0 && percentageOfOriginalDocument < 100) ? percentageOfOriginalDocument : 30d;

		String docText = "";		
		for(Entry<Entry<Annotation, TDDocument>, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {
			System.out.println("Sent: " + orderedSentences_SemScoreEntry.getKey().getKey().getId() + " > SCORE: " + orderedSentences_SemScoreEntry.getValue());
			docText += ((docText.length() > 0) ? " " : "") + GtUtils.getTextOfAnnotation(orderedSentences_SemScoreEntry.getKey().getKey(), orderedSentences_SemScoreEntry.getKey().getValue().getGATEdoc()); 
		}

		double totalLengthDoc = docText.length();
		double totalLengthSummary = 0d;
		for(Entry<Entry<Annotation, TDDocument>, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {

			retMap.put(orderedSentences_SemScoreEntry.getKey(), orderedSentences_SemScoreEntry.getValue());

			String annoText = GtUtils.getTextOfAnnotation(orderedSentences_SemScoreEntry.getKey().getKey(), orderedSentences_SemScoreEntry.getKey().getValue().getGATEdoc());
			totalLengthSummary += annoText.length();
			if( (totalLengthSummary / totalLengthDoc) > percentageOfOriginalDocument) {
				break;
			}
		}

		return retMap;
	}
	
	/**
	 * Given a map of ranked document sentences, select the top sentences.<br/>
	 * See the class {@link edu.upf.taln.textdigester.summarizer.ConfigurableSummarizer ConfigurableSummarizer} to know how to generate
	 * the map of ranked document sentences.
	 * 
	 * @param orderedSentences_SemScore map of ranked document sentences
	 * @param doc document to summarize
	 * @param numSentence top sentences
	 * @return
	 */
	public static Map<Annotation, Double> getSummary(Map<Annotation, Double> orderedSentences_SemScore, TDDocument doc, int numSentence) {
		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

		if(orderedSentences_SemScore == null || orderedSentences_SemScore.size() == 0 || doc == null) {
			return null;
		}

		if(numSentence < 0) {
			logger.warn("Num sentences of summary wrongly defined - set to 10.");
		}

		numSentence = (numSentence > 0) ? numSentence : 10;

		for(Entry<Annotation, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {
			System.out.println("Sent: " + orderedSentences_SemScoreEntry.getKey().getId() + " > SCORE: " + orderedSentences_SemScoreEntry.getValue());
		}

		int totalSentencesSummary = 0;
		for(Entry<Annotation, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {

			retMap.put(orderedSentences_SemScoreEntry.getKey(), orderedSentences_SemScoreEntry.getValue());

			totalSentencesSummary++;
			if( totalSentencesSummary >= numSentence) {
				break;
			}
		}

		return retMap;
	}

	public static Map<Map.Entry<Annotation, TDDocument>, Double> getSummary(Map<Map.Entry<Annotation, TDDocument>, Double> orderedSentences_SemScore, List<TDDocument> docList, int numSentence) {
		Map<Map.Entry<Annotation, TDDocument>, Double> retMap = new HashMap<Map.Entry<Annotation, TDDocument>, Double>();

		if(orderedSentences_SemScore == null || orderedSentences_SemScore.size() == 0 || docList == null || docList.size() == 0) {
			return null;
		}

		if(numSentence < 0) {
			logger.warn("Num sentences of summary wrongly defined - set to 10.");
		}

		numSentence = (numSentence > 0) ? numSentence : 10;


		for(Entry<Entry<Annotation, TDDocument>, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {
			System.out.println("Sent: " + orderedSentences_SemScoreEntry.getKey().getKey().getId() + " > SCORE: " + orderedSentences_SemScoreEntry.getValue());
		}

		int totalSentencesSummary = 0;
		for(Entry<Entry<Annotation, TDDocument>, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {

			retMap.put(orderedSentences_SemScoreEntry.getKey(), orderedSentences_SemScoreEntry.getValue());

			totalSentencesSummary++;
			if( totalSentencesSummary >= numSentence) {
				break;
			}
		}

		return retMap;
	}


	/**
	 * Get the text of the summary sentences in orderedSentences_SemScore
	 * 
	 * @param orderedSentences_SemScore
	 * @param doc
	 * @return
	 */
	public static String getStringSummaryText(Map<Annotation, Double> orderedSentences_SemScore, TDDocument doc) {

		if(orderedSentences_SemScore == null || orderedSentences_SemScore.size() == 0 || doc == null) {
			return null;
		}

		String docText = "";

		Set<Annotation> setAnn = orderedSentences_SemScore.keySet();
		List<Annotation> listAnn = new ArrayList<Annotation>();
		listAnn.addAll(setAnn);
		GtUtils.orderAnnotationList(listAnn, doc.getGATEdoc(), new Integer(10000000));

		for(Annotation summaryAnn : listAnn) {
			docText += ((docText.length() > 0) ? " " : "") + GtUtils.getTextOfAnnotation(summaryAnn, doc.getGATEdoc()); 
		}

		return docText;
	}

	public static String getStringSummaryText(Map<Map.Entry<Annotation, TDDocument>, Double> orderedSentences_SemScore) {

		if(orderedSentences_SemScore == null || orderedSentences_SemScore.size() == 0) {
			return null;
		}

		String docText = "";

		Map<TDDocument, List<Annotation>> docAnnMap = new HashMap<TDDocument, List<Annotation>>();
		for(Entry<Entry<Annotation, TDDocument>, Double> orderedSentences_SemScoreEntry : orderedSentences_SemScore.entrySet()) {
			if(docAnnMap.containsKey(orderedSentences_SemScoreEntry.getKey().getValue())) {
				docAnnMap.get(orderedSentences_SemScoreEntry.getKey().getValue()).add(orderedSentences_SemScoreEntry.getKey().getKey());
			}
			else {
				docAnnMap.put(orderedSentences_SemScoreEntry.getKey().getValue(), new ArrayList<Annotation>());
				docAnnMap.get(orderedSentences_SemScoreEntry.getKey().getValue()).add(orderedSentences_SemScoreEntry.getKey().getKey());
			}
		}
		
		for(Entry<TDDocument, List<Annotation>> docAnnotations : docAnnMap.entrySet()) {
			List<Annotation> listAnn = docAnnotations.getValue();
			GtUtils.orderAnnotationList(listAnn, docAnnotations.getKey().getGATEdoc(), new Integer(10000000));

			for(Annotation summaryAnn : listAnn) {
				docText += ((docText.length() > 0) ? " " : "") + GtUtils.getTextOfAnnotation(summaryAnn, docAnnotations.getKey().getGATEdoc()); 
			}
		}
		
		return docText;
	}

}
