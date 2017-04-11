/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.txtvect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.lex.StopWordList;
import edu.upf.taln.textdigester.resource.lex.WikipediaLemmaPOSfFrequency;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import gate.Annotation;

/**
 * Utility methods to compute TF-IDF sentence vectors
 * 
 * @author Francesco Ronzano
 *
 */
public class TFIDFVectorWiki {

	private static final Logger logger = LoggerFactory.getLogger(TFIDFVectorWiki.class);

	private LangENUM lang;

	// Parameters to extract terms
	public boolean onlyWordKind = true;
	public boolean getLemma = true;
	public boolean toLowerCase = true;
	public boolean removeStopWords = true;
	public boolean appendPOS = true;
	public Set<String> stopWordsList = new HashSet<String>();
	
	// Constructor
	public TFIDFVectorWiki(LangENUM langIN) throws TextDigesterException {
		if(langIN == null) {
			throw new TextDigesterException("Specify a language to load a tfidf word list and stopword list.");
		}
		
		stopWordsList = StopWordList.getStopwordList(langIN);
		lang = langIN;
	}
	
	/**
	 * Compute the TF-IDF vector of the sentence
	 * 
	 * @param ann sentence annotation
	 * @param doc document including the sentence
	 * @return
	 */
	public Map<String, Double> computeTFIDFvect(Annotation ann, TDDocument doc) {
		Map<String, Double> retMap = new HashMap<String, Double>();

		List<String> annotation_terms = TDDocument.extractTokenList(ann, doc, null, onlyWordKind, getLemma, toLowerCase, appendPOS, removeStopWords, stopWordsList);
		
		// Get all the distinct annotation terms
		Set<String> annotation_terms_set = new HashSet<String>(annotation_terms);
		
		for(String term : annotation_terms_set) {
			try {
				Integer tf = 0;
				for(int k = 0; k < annotation_terms.size(); k++) {
					if(annotation_terms.get(k).equals(term)) {
						tf++;
					}
				}
				double normalized_tf = (double) tf / (double) annotation_terms.size();
				
				double idf = 0d;
				Integer documentFreq = WikipediaLemmaPOSfFrequency.getDocumentFrequency(lang, term);
				if(documentFreq != null && documentFreq > 0) {
					idf = Math.log( ((double) WikipediaLemmaPOSfFrequency.getTotNumDoc(lang)) / ((double) documentFreq) ); 
				}
				
				retMap.put(term, normalized_tf * idf);
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception calculating sentSim_TFIDF_DocCentric");
			}

		}
		
		return retMap;
	}
	
	/**
	 * Compute the TF IDF similarity among the two token lists
	 * 
	 * Term frequency of a sentence: number of times the token appears in the sentence
	 * Inverse document frequency: logarithm of the total number of documents divided by the number of docs in which the token appears
	 * 
	 * @param tokenSent1
	 * @param tokenSetn2
	 * @return
	 */
	public double cosSimTFIDF(Map<String, Double> tokenDoc1, Map<String, Double> tokenDoc2) {
		Multiset<String> tfMS = HashMultiset.create();
		for(String str : tokenDoc1.keySet()) { 
			tfMS.add(str);
		}
		for(String str : tokenDoc2.keySet()) { 
			tfMS.add(str);
		}

		// Compute the word vectors
		String[] wordArray = tfMS.elementSet().toArray(new String[tfMS.elementSet().size()]);

		Double[] vectSent1 = new Double[wordArray.length];
		for(int i = 0; i < wordArray.length; i++) {
			try {
				if(tokenDoc1.containsKey(wordArray[i])) {
					vectSent1[i] = tokenDoc1.get(wordArray[i]);
				}
				else {
					vectSent1[i] = 0d;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception calculating sentSim_TFIDF_DocCentric");
			}

		}


		Double[] vectSent2 = new Double[wordArray.length];
		for(int i = 0; i < wordArray.length; i++) {
			try {
				if(tokenDoc2.containsKey(wordArray[i])) {
					vectSent2[i] = tokenDoc2.get(wordArray[i]);
				}
				else {
					vectSent2[i] = 0d;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception calculating sentSim_TFIDF_DocCentric");
			}
		}

		// Compute cosine similarity of vectSent1 and vectSent2
		double sumNumer = 0d;
		for(int i = 0; i < vectSent1.length; i++) {
			sumNumer += (vectSent1[i] * vectSent2[i]);
		}

		double sumDenom1 = 0d;
		for(int i = 0; i < vectSent1.length; i++) {
			sumDenom1 += vectSent1[i] * vectSent1[i];
		}
		double sumDenom2 = 0d;
		for(int i = 0; i < vectSent2.length; i++) {
			sumDenom2 += (vectSent2[i] * vectSent2[i]);
		}
		double sumDenom = Math.sqrt(sumDenom1) * Math.sqrt(sumDenom2);
		
		logger.debug("Cosine similarity - " + ((sumDenom != 0d) ? (sumNumer / sumDenom) : 0d));
		
		return (sumDenom != 0d) ? (sumNumer / sumDenom) : 0d;
	}
	
	/**
	 * Compute the TF IDF similarity among the two document annotations
	 * 
	 * Term frequency of a sentence: number of times the token appears in the sentence
	 * Inverse document frequency: logarithm of the total number of documents divided by the number of docs in which the token appears
	 * 
	 * @param ann1
	 * @param doc1
	 * @param ann2
	 * @param doc2
	 * @return
	 */
	public double cosSimTFIDF(Annotation ann1, TDDocument doc1, Annotation ann2, TDDocument doc2) {
		return cosSimTFIDF(computeTFIDFvect(ann1, doc1), computeTFIDFvect(ann2, doc2));
	}
}
