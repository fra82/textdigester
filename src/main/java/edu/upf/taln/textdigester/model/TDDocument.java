package edu.upf.taln.textdigester.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Strings;

import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.util.TokenFilterInterface;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;

public class TDDocument {
	
	private static Random rnd = new Random();
	
	public static final String nameDocFeatName = "Name";
	
	public static final String mainAnnSet = "Analysis";
	
	public static final String documentAnnType = "Document";
	
	public static final String sentenceAnnType = "Sentence";
	
	public static final String titleAnnType = "Title";
	
	public static final String sectHeaderAnnType = "SectHeader";
	
	public static final String tokenAnnType = "Token";
	public static final String token_startSpanFeatName = "f_spanStart";
	public static final String token_endSpanFeatName = "f_spanFinish";
	public static final String token_lemmaFeatName = "f_lemma";
	public static final String token_positionFeatName = "f_position";
	public static final String token_POSFeatName = "f_tag";
	public static final String token_formStringFeatName = "f_form";
	public static final String token_phFormStringFeatName = "f_phForm";
	
	private String originalText;
	private Document GATEdoc;
	
	// Constructors
	public TDDocument(String originalText, String docName) throws TextDigesterException {
		
		if(originalText == null) {
			throw new TextDigesterException("This constructor can be used only with a not null original text");
		}
		
		this.originalText = originalText;
		
		// Generate a GATE document containing the original text
		GtUtils.initGate();

		// Writing textual contents to GATE document
		try {
			Document gateDoc = Factory.newDocument(originalText);
			
			// Set name
			gateDoc.setFeatures(Factory.newFeatureMap());
			gateDoc.getFeatures().put(TDDocument.nameDocFeatName, ((docName != null && docName.length() > 0) ? docName : "DOC_" + rnd.nextInt() ));
			
			this.GATEdoc = gateDoc;
			
		} catch (ResourceInstantiationException e1) {
			e1.printStackTrace();
		}
	}
	
	public TDDocument(String originalText, Document GATEdocument, String docName) throws TextDigesterException {
		
		if(GATEdocument == null) {
			throw new TextDigesterException("This constructor can be used only with a not null GATE Document instance");
		}
		
		this.originalText = originalText;
		this.GATEdoc = GATEdocument;
		if(this.GATEdoc.getFeatures() == null) {
			this.GATEdoc.setFeatures(Factory.newFeatureMap());
		}
		this.GATEdoc.getFeatures().put(TDDocument.nameDocFeatName, ((docName != null && docName.length() > 0) ? docName : "DOC_" + rnd.nextInt() ));
	}
	
	// Setters and getters
	public String getOriginalText() {
		return originalText;
	}
	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}
	public Document getGATEdoc() {
		return GATEdoc;
	}
	public void setGATEdoc(Document gATEdoc) {
		GATEdoc = gATEdoc;
	}
	
	// Utility methods
	/** 
	 * Get the name of the document
	 * 
	 * @return
	 */
	public String getDocumentName() {
		if(GATEdoc != null && GATEdoc.getFeatures() != null && GATEdoc.getFeatures().containsKey(nameDocFeatName)) {
			return (String) GATEdoc.getFeatures().get(nameDocFeatName);
		}
		return null;
	}
	
	/**
	 * Given an annotation of a TDDocument, extract the list of tokens (eventually repeated in case of multiple occurrences)
	 * 
	 * @param ann
	 * @param doc
	 * @param onlyWordKind
	 * @param getLemma
	 * @param toLowerCase
	 * @param removeStopWords
	 * @return
	 */
	public static List<String> extractTokenList(Annotation ann, TDDocument doc, TokenFilterInterface tokenFilter, 
			boolean onlyWordKind, boolean getLemma, boolean toLowerCase, boolean appendPOS, boolean removeStopWords, Set<String> stopWordsList) {
		List<String> annotationTokens = new ArrayList<String>();

		List<Integer> tokenIDnotToConsider = new ArrayList<Integer>();
		if(tokenFilter != null) {
			tokenIDnotToConsider = tokenFilter.getTokenListNotToConsider(ann, doc.GATEdoc);
		}
		

		List<Annotation> intersectingTokensList = gate.Utils.inDocumentOrder(
				doc.getGATEdoc().getAnnotations(mainAnnSet).get(tokenAnnType).getContained(
						ann.getStartNode().getOffset(), 
						ann.getEndNode().getOffset() ));

		if(intersectingTokensList != null && intersectingTokensList.size() > 0) {

			for(Annotation tokenAnn : intersectingTokensList) {
				if(tokenFilter != null && tokenIDnotToConsider.contains(tokenAnn.getId())) {
					continue; // It's a token not to consider
				}

				if(onlyWordKind && 
					(!tokenAnn.getFeatures().containsKey(token_POSFeatName) || ((String) tokenAnn.getFeatures().get(token_POSFeatName)).toLowerCase().startsWith("f")) ) {
					continue; // It's a token with POS feature staring with 'F', thus a punctuation, not to consider
				}

				String string = "";
				if(getLemma) {
					string = (String) ((tokenAnn.getFeatures().containsKey(token_lemmaFeatName)) ? tokenAnn.getFeatures().get(token_lemmaFeatName) : "");
				}
				else {
					string = (String) ((tokenAnn.getFeatures().containsKey(token_formStringFeatName)) ? tokenAnn.getFeatures().get(token_formStringFeatName) : "");
				}
				string = (toLowerCase) ? string.trim().toLowerCase(): string.trim();

				if(!Strings.isNullOrEmpty(string)) {
					if(!removeStopWords || (removeStopWords && !stopWordsList.contains(string.toLowerCase().trim())) ) {
						if(appendPOS) {
							if(tokenAnn.getFeatures().containsKey(token_POSFeatName) && !Strings.isNullOrEmpty((String) tokenAnn.getFeatures().get(token_POSFeatName))) {
								annotationTokens.add(string + "_" + ((String) tokenAnn.getFeatures().get(token_POSFeatName)).substring(0, 1));
							}
						}
						else {
							annotationTokens.add(string);
						}
					}
				}
			}
		}

		return annotationTokens;
	}
	
}
