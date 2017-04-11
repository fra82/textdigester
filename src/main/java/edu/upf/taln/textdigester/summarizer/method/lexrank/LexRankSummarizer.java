/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.method.lexrank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.MapUtil;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.method.ExtractiveSummarizer;
import edu.upf.taln.textdigester.summarizer.method.SentenceSimilarityENUM;
import edu.upf.taln.textdigester.summarizer.txtvect.EmbeddingVectorWiki;
import edu.upf.taln.textdigester.summarizer.txtvect.TFIDFVectorWiki;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

/**
 * Implementation of single-document LexRank summarizer
 * 
 * @author Francesco Ronzano
 *
 */
public class LexRankSummarizer implements ExtractiveSummarizer {

	private static final Logger logger = LoggerFactory.getLogger(TFIDFVectorWiki.class);

	// *******************************
	// LexRank parameters:
	// > linkThrashold_LR: The LexRank paper suggests a value of 0.1
	private SentenceSimilarityENUM sentSimilarity = SentenceSimilarityENUM.cosineTFIDF;
	// > linkThrashold_LR: The LexRank paper suggests a value of 0.1
	private double linkThrashold_LR = 0.01d;
	// > isContinuous_LR: Whether or not to use a continuous version of the LexRank algorithm, If set to false,
	// all similarity links above the similarity threshold will be considered equal; otherwise, the similarity
	// scores are used. The paper authors note that non-continuous LexRank seems to perform better.
	private boolean isContinuous_LR = false; 
	// *******************************

	private LangENUM lang;

	private TFIDFVectorWiki TFIDFcomput;
	
	
	public LexRankSummarizer(LangENUM langIN, SentenceSimilarityENUM simMethod, boolean isContinuous, double thrashold) throws TextDigesterException {
		if(langIN == null) {
			throw new TextDigesterException("Specify a language to load a tfidf word list and stopword list.");
		}

		lang = langIN;

		if(simMethod != null) {
			sentSimilarity = simMethod;
		}
		else {
			sentSimilarity = SentenceSimilarityENUM.cosineTFIDF;
		}

		switch(sentSimilarity) {
		case cosineTFIDF:
			TFIDFcomput = new TFIDFVectorWiki(lang);
			break;
		case cosineEMBED:
			
			break;
		}

		isContinuous_LR = isContinuous;
		linkThrashold_LR = thrashold;
	}


	@Override
	public Map<Annotation, Double> sortSentences(TDDocument doc) {

		GtUtils.initGate();

		// Read the document to summarize
		Document gateDoc = doc.getGATEdoc();

		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

		// STEP 1: If we consider that the document to summarize contains N sentence annotations, we have to generate a map
		// that map each sentence to an integer starting from 0 up to (N-1) in order to support the generation of the 
		// similarity matrix
		Map<Integer, Annotation> sentenceIndexToAnnotationMap = new HashMap<Integer, Annotation>();

		// Getting all sentences of the document that could be potentially included in the summary 
		AnnotationSet sentencesAnnotationSet = gateDoc.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
		if(sentencesAnnotationSet == null || sentencesAnnotationSet.size() < 1) {
			logger.error(">>> NO SENTENCES SELECTED TO GENERATE THE SUMMARY");
			return null;
		}

		Integer sentenceIndex = 0;
		Iterator<Annotation> sentenceAnnotations = sentencesAnnotationSet.iterator();
		while(sentenceAnnotations.hasNext()) {
			Annotation sentenceAnnotation = sentenceAnnotations.next();
			if(sentenceAnnotation != null) {
				sentenceIndexToAnnotationMap.put(sentenceIndex++, sentenceAnnotation);
			}
		}

		logger.info("*** Start Ranking " + sentenceIndexToAnnotationMap.size() + " sentences...");

		// STEP 2: Lex rank needs a sentence similarity matrix - square symmetric matrix, N * N, where N is the size of the sentenceIndexToAnnotationMap
		// Instantiate and initialize the matrix with 0 sentence similarity values
		double[][] similarityMatrix = new double[sentenceIndexToAnnotationMap.size()][sentenceIndexToAnnotationMap.size()];

		for(int i = 0; i < sentenceIndexToAnnotationMap.size(); i++) {
			for(int j = 0; j < sentenceIndexToAnnotationMap.size(); j++) {
				similarityMatrix[i][j] = 0d;
			}
		}

		logger.info("*** Instantiated similarity matrix of size " + sentenceIndexToAnnotationMap.size());

		// STEP 3: Populate the similarity matrix computing the similarity value for each pair of sentences
		Integer computedSimilarityCounter = 0;
		for(int x_val = 0; x_val < sentenceIndexToAnnotationMap.size(); x_val++) {

			for(int y_val = 0; y_val <= x_val; y_val++) {
				if(y_val < x_val) {

					// Get the pair of sentences to analyze
					Annotation firstSentenceAnn = sentenceIndexToAnnotationMap.get(x_val);
					Annotation secondSentenceAnn = sentenceIndexToAnnotationMap.get(y_val);

					// Compute the similarity value between the two sentences (double, usually from 0 to 1, where 1 is total similarity)
					double similarityValue = computeSentenceSimilarity(doc, firstSentenceAnn, secondSentenceAnn);

					// Store the similarity value in the matrix
					similarityMatrix[y_val][x_val] = similarityMatrix[x_val][y_val] = similarityValue;

					computedSimilarityCounter++;
					if(computedSimilarityCounter % 100 == 0) {
						logger.info(" ...computed similarity of " + computedSimilarityCounter + " sentence pairs over " + ( (int) (Math.pow( (double) sentenceIndexToAnnotationMap.size(), 2d) / 2d ) ));
					}

				}
				else if(y_val == x_val) {
					similarityMatrix[y_val][x_val] = 1d;
				}
			}
		}

		// Check similarity matrix consistency
		for (int i = 0; i < similarityMatrix.length; ++i) {
			for (int j = 0; j <= i; ++j) {
				if(similarityMatrix[i][j] != similarityMatrix[j][i]) {
					logger.error(">>> NOT SYMMATRIC MATRIX, ERROR AT INDEXES [" + i + ", " + j + "] " + 
							"->" + similarityMatrix[i][j] + " == " + similarityMatrix[j][i]);
				}
			}
		}

		logger.info("*** Populated similarity matrix of size " + sentenceIndexToAnnotationMap.size());

		// Printing similarity matrix - UNCOMMENT IF NEEDED
		/*
		for(int i = 0; i < sentenceIndexToAnnotationMap.size(); i++) {
			for(int j = 0; j < sentenceIndexToAnnotationMap.size(); j++) {
				System.out.print(similarityMatrix[i][j] + " ");
			}
			System.out.print("\n");
		}
		 */

		// STEP 4: LexRank computation
		System.out.println("*** Starting LexRank computations...");
		List<DummyItem> items = new ArrayList<DummyItem>();
		for (int i = 0; i < similarityMatrix.length; ++i) {
			items.add(new DummyItem(i, similarityMatrix));
		}
		LexRankResults<DummyItem> results = LexRanker.rank(items, linkThrashold_LR, isContinuous_LR);

		double max = results.scores.get(results.rankedResults.get(0));

		HashMap<Integer, Double> rankedSentenceMap = new HashMap<Integer, Double>();
		for(DummyItem res : results.rankedResults) {
			double itemScore = results.scores.get(res) / max; // Normalize to 1
			rankedSentenceMap.put(res.id, itemScore);
		}

		// In the SortedSet sortedRrankedSentenceMap there is a sorted (by decreasing LexRank / centrality value) collection of Map entries
		// each one with the index of the sentence in the similarity matrix as key and the ranking as value
		Map<Integer, Double> sortedRrankedSentenceMap = MapUtil.sortByValue(rankedSentenceMap);


		// STEP 5: Print the sorted list of sentences (from the most to the least relevant as scored by LexRank)
		System.out.println("\nRANKED SENTENCE LIST:");
		Integer rankingPosition = 1;
		for(Map.Entry<Integer, Double> sortedRrankedSentenceMapEntry : sortedRrankedSentenceMap.entrySet()) {
			try {
				// Get sentence Annotation object
				Annotation sentenceAnnotation = sentenceIndexToAnnotationMap.get(sortedRrankedSentenceMapEntry.getKey());

				retMap.put(sentenceAnnotation, sortedRrankedSentenceMapEntry.getValue());

				String sentenceText = gateDoc.getContent().getContent(sentenceAnnotation.getStartNode().getOffset(), sentenceAnnotation.getEndNode().getOffset()).toString();

				System.out.println(rankingPosition++ + " > LexRank value: " + sortedRrankedSentenceMapEntry.getValue() + " :"
						+ "\n >>> TEXT: " + sentenceText);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return MapUtil.sortByValue(retMap);
	}

	/**
	 * Given two sentences of a document, the similarity value between them is returned as double.
	 * 
	 * SUBSTITUTE WITH CUSTOM SENTENCE SIMILARITY
	 * 
	 * @param gateDoc
	 * @param sentence1
	 * @param sentence2
	 * @return
	 */
	private double computeSentenceSimilarity(TDDocument doc, Annotation sentence1, Annotation sentence2) {

		switch(sentSimilarity) {
		case cosineTFIDF:
			return TFIDFcomput.cosSimTFIDF(sentence1, doc, sentence2, doc);
		case cosineEMBED:
			return EmbeddingVectorWiki.cosSimEMBED(sentence1, doc, sentence2, doc, lang);
		}

		return 0d;
	}


}
