/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import edu.upf.taln.textdigester.summarizer.util.TokenFilterInterface;
import gate.Annotation;
import gate.Document;
import gate.util.InvalidOffsetException;

/**
 * ROUGE summary evaluation utility
 * 
 * @author Francesco Ronzano
 *
 */
public class ROUGEscorer {

	// ID of the summary we are genrating
	public String summaryID;

	// Reference document info
	public String originalDocName;
	public Document originalDoc;

	// Map of other doc to use for summarization
	public Map<Integer, Document> auxiliaryDocs = new HashMap<Integer, Document>();

	// Gold standard and automatically computed data
	public Map<String, Document> gsAnnotatorSummaryDocumentMap = new HashMap<String, Document>();

	// Result of automated sentence extraction
	public Map<Integer, Annotation> summarySentenceMap= new HashMap<Integer, Annotation>();
	public Map<Integer, Document> summaryDocumentMap= new HashMap<Integer, Document>();
	public Map<Integer, Double> summaryScoreMap= new HashMap<Integer, Double>();


	// Config of ROUGE computation parameters:
	// a bigram is a sequence of two tokens that are:
	// - of kind word (excluding number or punctuation) - onlyWordKind = true;
	// - consecutive, that is not separated by any other number or punctuation token
	// - lowercased - toLowerCase = true;
	// - not lemmatized - getLemma = false;
	// - including stop words - removeStopWords = false;
	// - not filtered - tokenFilter = null;
	public static boolean onlyWordKind = true;
	public static boolean toLowerCase = true;
	public static boolean getLemma = false;
	public static boolean removeStopWords = false;
	public static TokenFilterInterface tokenFilter = null;

	private static boolean debug = true;

	/**
	 * Compute the ROUGE-2 metric with respect to the citation based summary
	 * 
	 * REFERENCE: http://research.microsoft.com/en-us/um/people/cyl/download/papers/was2004.pdf
	 * 
	 * @return
	 */
	public Double computeROUGEscore(Integer wordNumTrunk) {
		
		System.out.println("\n\nROUGE SCORE COMPUTAITON with " + wordNumTrunk + " max words - START:");

		if(debug) {
			System.out.println("\nGENERATED SUMMARY TEXT:");
			for(Entry<Integer, Annotation> entry : summarySentenceMap.entrySet()) {
				try {
					System.out.println(summaryDocumentMap.get(entry.getKey()).getContent().getContent(
							entry.getValue().getStartNode().getOffset(),
							entry.getValue().getEndNode().getOffset()
							)
							);
				} catch (InvalidOffsetException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		// 1.A) Compute uni/bigram list of generated summary
		Multiset<String> unigramMultiset_generatedSummary = TreeMultiset.create();
		Multiset<String> bigramMultiset_generatedSummary = TreeMultiset.create();
		
		Integer addedWords_trk = 0;
		for(Entry<Integer, Annotation> entry : summarySentenceMap.entrySet()) {
			Integer summarySentId = entry.getKey();
			List<String> sentUnigramList = extractNGramsList(1, entry.getValue(), summaryDocumentMap.get(summarySentId), 
					tokenFilter, onlyWordKind, getLemma, toLowerCase);
			for(String unigram : sentUnigramList) {

				// Check if to stop adding words
				addedWords_trk++;
				if(wordNumTrunk != null && wordNumTrunk > 0 && addedWords_trk > wordNumTrunk) {
					break;
				}

				unigramMultiset_generatedSummary.add(unigram);
			}
		}

		Integer addedWordsBi_trk = 0;
		for(Entry<Integer, Annotation> entry : summarySentenceMap.entrySet()) {
			Integer summarySentId = entry.getKey();
			
			List<String> sentBigramsList = extractNGramsList(2, entry.getValue(), summaryDocumentMap.get(summarySentId), 
					tokenFilter, onlyWordKind, getLemma, toLowerCase);
			List<String> sentUnigramList = extractNGramsList(1, entry.getValue(), summaryDocumentMap.get(summarySentId), 
					tokenFilter, onlyWordKind, getLemma, toLowerCase);
			
			if(debug) {
				System.out.println("\nGEN SUMM > Sentence (" + entry.getKey() + "):");
				try {
					System.out.println(summaryDocumentMap.get(entry.getKey()).getContent().getContent(
							entry.getValue().getStartNode().getOffset(),
							entry.getValue().getEndNode().getOffset()
							)
							);
				} catch (InvalidOffsetException e) {
					e.printStackTrace();
				}
				
				System.out.println("GEN SUMM > Sentence (" + entry.getKey() + ") > Bigrams: " + sentBigramsList.toString());
			}
			
			
			for(String bigram : sentBigramsList) {
				bigramMultiset_generatedSummary.add(bigram);
			}
			
			// Check if to stop adding words
			addedWordsBi_trk += sentUnigramList.size();
			
			if(wordNumTrunk != null && wordNumTrunk > 0 && addedWordsBi_trk > wordNumTrunk) {
				if(debug) {
					System.out.println("GEN SUMM > Words' number: " + addedWordsBi_trk + " is greater than " + wordNumTrunk + " > finish bigram computation.");
				}
				break;
			}
			else {
				if(debug) {
					System.out.println("GEN SUMM > Words' number: " + addedWordsBi_trk + " is lower than " + wordNumTrunk);
				}
			}
		}


		// 1.B) Computed uni/bigram list of gs summaries
		Map<String, Multiset<String>> annotatorUnigramMultisetMap_GS = new HashMap<String, Multiset<String>>();
		Map<String, Multiset<String>> annotatorBigramMultisetMap_GS = new HashMap<String, Multiset<String>>();

		Integer addedWordsGS_trk = 0;
		for(Entry<String, Document> entry : gsAnnotatorSummaryDocumentMap.entrySet()) {

			Multiset<String> unigramMultiset_gs = TreeMultiset.create();

			// Add unigrams for all sentences
			List<Annotation> sentAnnList = gate.Utils.inDocumentOrder(entry.getValue().getAnnotations("Analysis").get("Sentence"));
			for(Annotation sentAnn : sentAnnList) {
				List<String> sentUnigramList = extractNGramsList(1, sentAnn, entry.getValue(),
						tokenFilter, onlyWordKind, getLemma, toLowerCase);
				for(String unigram : sentUnigramList) {

					// Check if to stop adding words
					addedWordsGS_trk++;
					if(wordNumTrunk != null && wordNumTrunk > 0 && addedWordsGS_trk > wordNumTrunk) {
						break;
					}

					unigramMultiset_gs.add(unigram);
				}
			}

			// Add result
			annotatorUnigramMultisetMap_GS.put(entry.getKey(), unigramMultiset_gs);
		}

		Integer addedWordsBiGS_trk = 0;
		for(Entry<String, Document> entry : gsAnnotatorSummaryDocumentMap.entrySet()) {

			Multiset<String> bigramMultiset_gs = TreeMultiset.create();

			// Add unigrams for all sentences
			List<Annotation> sentAnnList = gate.Utils.inDocumentOrder(entry.getValue().getAnnotations("Analysis").get("Sentence"));
			for(Annotation sentAnn : sentAnnList) {

				List<String> sentBigramList = extractNGramsList(2, sentAnn, entry.getValue(),
						tokenFilter, onlyWordKind, getLemma, toLowerCase);
				List<String> sentUnigramList = extractNGramsList(1, sentAnn, entry.getValue(),
						tokenFilter, onlyWordKind, getLemma, toLowerCase);
				
				if(debug) {
					System.out.println("\nGS ANN" + entry.getKey() + " > Sentence:");
					try {
						System.out.println(entry.getValue().getContent().getContent(
								sentAnn.getStartNode().getOffset(),
								sentAnn.getEndNode().getOffset()
								)
								);
					} catch (InvalidOffsetException e) {
						e.printStackTrace();
					}

					System.out.println("GS ANN" + entry.getKey() + " > Bigrams: " + sentBigramList.toString());
				}

				for(String bigram : sentBigramList) {
					bigramMultiset_gs.add(bigram);
				}
				
				// Check if to stop adding words
				addedWordsBiGS_trk += sentUnigramList.size();
				
				if(wordNumTrunk != null && wordNumTrunk > 0 && addedWordsBiGS_trk > wordNumTrunk) {
					if(debug) {
						System.out.println("GS ANN" + entry.getKey() + " > Words' number: " + addedWordsBi_trk + " is greater than " + wordNumTrunk + " > finish bigram computation.");
					}
					break;
				}
				else {
					if(debug) {
						System.out.println("GS ANN" + entry.getKey() + " > Words' number: " + addedWordsBi_trk + " is lower than " + wordNumTrunk);
					}
				}
			}

			// Add result
			annotatorBigramMultisetMap_GS.put(entry.getKey(), bigramMultiset_gs);
		}


		// 2) Compute ROUGE-2 starting from:
		// unigramMultiset_generatedSummary, bigramMultiset_generatedSummary, annotatorUnigramMultisetMap_GS, annotatorBigramMultisetMap_GS

		Integer countMatchNgramOverAllSummaries = 0;
		Integer countTotalNgramsOverAllSummaries = 0;

		// Numerator - all the bigrams occurring in both the generated summary and reference ones
		for(Entry<String, Multiset<String>> GSsummaryBigram : annotatorBigramMultisetMap_GS.entrySet()) {
			// String GSsummaryName = GSsummaryBigram.getKey();
			Set<String> GSsummaryBigrams = GSsummaryBigram.getValue().elementSet();

			for(String bigramStr : GSsummaryBigrams) {
				if( bigramMultiset_generatedSummary.contains(bigramStr) ) {
					countMatchNgramOverAllSummaries = countMatchNgramOverAllSummaries + bigramMultiset_generatedSummary.count(bigramStr);
					if(debug) {
						if(bigramMultiset_generatedSummary.count(bigramStr) > 1) {
							System.out.println(" ROUGE computing: bigram repeated more than one time in summary: " + bigramStr + ", times: " + bigramMultiset_generatedSummary.count(bigramStr) );
						}
					}
					
				}
			}
		}

		// Denominator - all the bigrams occurring in the reference summaries
		for(Entry<String, Multiset<String>> GSsummaryBigram : annotatorBigramMultisetMap_GS.entrySet()) {
			// String GSsummaryName = GSsummaryBigram.getKey();
			
			countTotalNgramsOverAllSummaries += GSsummaryBigram.getValue().size();
		}

		double retROUGE2 = (countTotalNgramsOverAllSummaries > 0) ? ( ((double) countMatchNgramOverAllSummaries) / ((double) countTotalNgramsOverAllSummaries) ) : 0d;

		System.out.println("\nSum ov number of distinct n-grams matches in all ref. summaries: " + countMatchNgramOverAllSummaries);
		System.out.println("Sum of number of disting n-grams in every ref. summaries: " + countTotalNgramsOverAllSummaries);
		System.out.println("ROUGE SCORE " + wordNumTrunk + " max words: " + retROUGE2);
		System.out.println("\nROUGE SCORE COMPUTAITON with " + wordNumTrunk + " max words - END.\n\n");
		
		return retROUGE2;
	}
	
	
	/**
	 * Given an annotation, extract the list of n-grams (each n-gram can be repeated more times in the list)
	 * 
	 * @param n between 1 and 4
	 * @param ann
	 * @param doc
	 * @param onlyWordKind
	 * @param getLemma
	 * @param toLowerCase
	 * @param removeStopWords
	 * @return
	 */
	public static List<String> extractNGramsList(Integer n, Annotation ann, Document doc, TokenFilterInterface tokenFilter,
			boolean onlyWordKind, boolean getLemma, boolean toLowerCase) {

		n = (n != null && n > 0 && n < 5) ? n : 2;

		List<String> ngramsTokens = new ArrayList<String>();

		List<Annotation> intersectingSentenceList = gate.Utils.inDocumentOrder(
				doc.getAnnotations("Analysis").get("Sentence").getContained(
						ann.getStartNode().getOffset(), 
						ann.getEndNode().getOffset() ));

		if(intersectingSentenceList != null && intersectingSentenceList.size() > 0) {

			for(Annotation sentAnn : intersectingSentenceList) {

				Map<Integer, String> sentenceTokenList = extractTokenListWithSequenceNumber(sentAnn, doc, tokenFilter, onlyWordKind, getLemma, toLowerCase);

				if(sentenceTokenList != null && sentenceTokenList.size() > 0) {

					for(Entry<Integer, String> entry : sentenceTokenList.entrySet()) {
						
						Integer tokenSequenceNumber = entry.getKey();
						String tokenString = entry.getValue();
						
						Integer addedTokens = 1;
						String nGram = tokenString;

						for(int z = tokenSequenceNumber + 1; z < (tokenSequenceNumber + n); z++) {
							if(sentenceTokenList.containsKey(z)) {
								nGram += "__" + sentenceTokenList.get(z);
								addedTokens++;
							}
							else {
								break;
							}
						}

						if(addedTokens.equals(n)) {
							ngramsTokens.add(nGram);
						}
					}
				}
			}
		}

		return ngramsTokens;
	}
	
	
	/**
	 * Given an annotation of a document, extract the list of tokens (eventually repeated in case of multiple occurrences)
	 * 
	 * @param ann
	 * @param doc
	 * @param onlyWordKind
	 * @param getLemma
	 * @param toLowerCase
	 * @param removeStopWords
	 * @return
	 */
	public static Map<Integer, String> extractTokenListWithSequenceNumber(Annotation ann, Document doc, TokenFilterInterface tokenFilter,
			boolean onlyWordKind, boolean getLemma, boolean toLowerCase) {
		Map<Integer, String> sentenceTokensOrdered = new HashMap<Integer, String>();

		List<Integer> tokenIDnotToConsider = new ArrayList<Integer>();
		if(tokenFilter != null) {
			tokenIDnotToConsider = tokenFilter.getTokenListNotToConsider(ann, doc);
		}
		

		List<Annotation> intersectingTokensList = gate.Utils.inDocumentOrder(
				doc.getAnnotations("Analysis").get("Token").getContained(
						ann.getStartNode().getOffset(), 
						ann.getEndNode().getOffset() ));

		Integer countTokenPosition = -1;
		if(intersectingTokensList != null && intersectingTokensList.size() > 0) {

			for(Annotation tokenAnn : intersectingTokensList) {
				countTokenPosition++;
				if(tokenFilter != null && tokenIDnotToConsider.contains(tokenAnn.getId())) {
					continue; // It's a token belonging to a citation or to a text inside parenthesis
				}

				if(onlyWordKind && (!tokenAnn.getFeatures().containsKey("kind") || !tokenAnn.getFeatures().get("kind").equals("word")) ) {
					continue; // It's not a token with 'kind' feature equal to 'word'
				}

				String string = "";
				if(getLemma) {
					string = (String) ((tokenAnn.getFeatures().containsKey("lemma")) ? tokenAnn.getFeatures().get("lemma") : "");
				}
				else {
					string = (String) ((tokenAnn.getFeatures().containsKey("string")) ? tokenAnn.getFeatures().get("string") : "");
				}
				string = (toLowerCase) ? string.trim().toLowerCase(): string.trim();

				if(!Strings.isNullOrEmpty(string)) {
					sentenceTokensOrdered.put(countTokenPosition, string);
				}
			}
		}

		return sentenceTokensOrdered;
	}
}
