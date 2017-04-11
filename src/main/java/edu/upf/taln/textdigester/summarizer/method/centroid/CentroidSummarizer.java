/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.method.centroid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
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
 * Implementation of single-document centroid summarizer
 * 
 * @author Francesco Ronzano
 *
 */
public class CentroidSummarizer implements ExtractiveSummarizer {

	private static final Logger logger = LoggerFactory.getLogger(TFIDFVectorWiki.class);

	private SentenceSimilarityENUM sentSimilarity = SentenceSimilarityENUM.cosineTFIDF;

	private LangENUM lang;

	private TFIDFVectorWiki TFIDFcomput;
	
	public CentroidSummarizer(LangENUM langIN, SentenceSimilarityENUM simMethod) throws TextDigesterException {
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
	}


	@Override
	public Map<Annotation, Double> sortSentences(TDDocument doc) {

		GtUtils.initGate();

		// Read the document to summarize
		Document gateDoc = doc.getGATEdoc();

		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

		Map<Integer, Annotation> sentenceIDannotationMap = new HashMap<Integer, Annotation>();

		// Getting all sentences of the document that could be potentially included in the summary 
		AnnotationSet sentencesAnnotationSet = gateDoc.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
		if(sentencesAnnotationSet == null || sentencesAnnotationSet.size() < 1) {
			logger.error(">>> NO SENTENCES SELECTED TO GENERATE THE SUMMARY");
			return null;
		}

		Iterator<Annotation> sentenceAnnotations = sentencesAnnotationSet.iterator();
		while(sentenceAnnotations.hasNext()) {
			Annotation sentenceAnnotation = sentenceAnnotations.next();
			if(sentenceAnnotation != null) {
				sentenceIDannotationMap.put(sentenceAnnotation.getId(), sentenceAnnotation);
			}
		}

		switch(sentSimilarity) {
		case cosineTFIDF:
			TFIDFVectorWiki tfidfGen = null;
			try {
				tfidfGen = new TFIDFVectorWiki(lang);
			} catch (TextDigesterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Map<Integer, Map<String, Double>> sentenceIDvectorMap_TFIDF = new HashMap<Integer, Map<String, Double>>();
			
			Map<String, Double> cetroid_TFIDF = new HashMap<String, Double>();
			Double count_TFIDF = 0d;
			for(Entry<Integer, Annotation> sentenceIDannotationMapEntry : sentenceIDannotationMap.entrySet()) {
				if(sentenceIDannotationMapEntry.getValue() != null) {
					Map<String, Double> vect = tfidfGen.computeTFIDFvect(sentenceIDannotationMapEntry.getValue(), doc);
					if(vect != null) {
						sentenceIDvectorMap_TFIDF.put(sentenceIDannotationMapEntry.getKey(), vect);
						count_TFIDF++;
						
						for(Entry<String, Double> elementVect : vect.entrySet()) {
							if(cetroid_TFIDF.containsKey(elementVect.getKey())) {
								cetroid_TFIDF.put(elementVect.getKey(), cetroid_TFIDF.get(elementVect.getKey()) + elementVect.getValue());
							}
							else {
								cetroid_TFIDF.put(elementVect.getKey(), elementVect.getValue());
							}
						}
					}
				}
			}
			
			// Compute centroid
			if(count_TFIDF > 0d) {
				for(Entry<String, Double> cetroid_TFIDFelem : cetroid_TFIDF.entrySet()) {
					cetroid_TFIDF.put(cetroid_TFIDFelem.getKey(), cetroid_TFIDFelem.getValue() / count_TFIDF);
				}
			}
			
			for(Entry<Integer, Map<String, Double>> sentenceIDvectorMap_TFIDFEntry : sentenceIDvectorMap_TFIDF.entrySet()) {
				try {
					retMap.put(sentenceIDannotationMap.get(sentenceIDvectorMap_TFIDFEntry.getKey()), 
							TFIDFcomput.cosSimTFIDF(cetroid_TFIDF, sentenceIDvectorMap_TFIDFEntry.getValue()));
				}
				catch(Exception e) {

				}
			}
			
			break;
		case cosineEMBED:
			Map<Integer, INDArray> sentenceIDvectorMap_EMBEDDING = new HashMap<Integer, INDArray>();

			INDArray centroid = null;
			Double count = 0d;
			for(Entry<Integer, Annotation> sentenceIDannotationMapEntry : sentenceIDannotationMap.entrySet()) {
				if(sentenceIDannotationMapEntry.getValue() != null) {
					INDArray vect = EmbeddingVectorWiki.computeEmbeddingVect(sentenceIDannotationMapEntry.getValue(), doc, lang);
					if(vect != null) {
						sentenceIDvectorMap_EMBEDDING.put(sentenceIDannotationMapEntry.getKey(), vect);
						count++;
						if(centroid == null) {
							centroid = vect.dup();
						}
						else {
							centroid = centroid.add(vect);
						}
					}
				}
			}

			// Compute centroid
			if(count > 0d) {
				centroid = centroid.div(count);
			}

			for(Entry<Integer, INDArray> sentenceIDvectorMap_EMBEDDINGEntry : sentenceIDvectorMap_EMBEDDING.entrySet()) {
				try {
					retMap.put(sentenceIDannotationMap.get(sentenceIDvectorMap_EMBEDDINGEntry.getKey()), 
							Transforms.cosineSim(centroid, sentenceIDvectorMap_EMBEDDINGEntry.getValue()));
				}
				catch(Exception e) {

				}
			}

			break;
		}

		return MapUtil.sortByValue(retMap);
	}

}
