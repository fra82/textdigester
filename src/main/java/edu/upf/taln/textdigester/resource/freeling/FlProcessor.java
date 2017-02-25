package edu.upf.taln.textdigester.resource.freeling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import edu.upc.freeling.ChartParser;
import edu.upc.freeling.DepTxala;
import edu.upc.freeling.HmmTagger;
import edu.upc.freeling.LangIdent;
import edu.upc.freeling.ListSentence;
import edu.upc.freeling.ListSentenceIterator;
import edu.upc.freeling.ListWord;
import edu.upc.freeling.ListWordIterator;
import edu.upc.freeling.Maco;
import edu.upc.freeling.MacoOptions;
import edu.upc.freeling.Nec;
import edu.upc.freeling.SWIGTYPE_p_splitter_status;
import edu.upc.freeling.Senses;
import edu.upc.freeling.Sentence;
import edu.upc.freeling.Splitter;
import edu.upc.freeling.Tokenizer;
import edu.upc.freeling.Ukb;
import edu.upc.freeling.Util;
import edu.upc.freeling.Word;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.corpora.CorpusImpl;
import gate.creole.ExecutionException;
import gate.util.InvalidOffsetException;

public class FlProcessor {

	private static final Logger logger = LoggerFactory.getLogger(FlProcessor.class);

	private static Random rnd = new Random();

	// Check the following paths
	private static final String FREELINGDIR = "/usr/local";
	private static final String DATA = FREELINGDIR + "/share/freeling/";

	private String LANG = "en";

	private static LangIdent lgid;

	// Define analyzers:
	private Tokenizer tk;
	private Splitter sp;
	private Maco mf;
	private HmmTagger tg;
	private ChartParser parser;
	private DepTxala dep; // Not used
	private Nec neclass; // Not used
	private Senses sen; // Not used
	private Ukb dis; // Not used

	protected static CorpusController corpusController_preprocess_ssplit = null;
	private static final Object LOCK_corpusController_preprocess_ssplit = new Object();

	/**
	 * Instantiate a processor by specifying the language to use
	 * 
	 * @param flang
	 * @throws Exception
	 */
	public FlProcessor(LangENUM flang) throws Exception {
		if(flang == null) {
			throw new Exception("Freeling language not specified!");
		}

		// Set language
		switch(flang) {
		case English:
			LANG = "en";
			break;
		case Spanish:
			LANG = "es";
			break;
		case Catalan:
			LANG = "ca";
			break;
		default:
			LANG = "en";
		}

		this.initiFreeling();
	}

	/**
	 * Initialize Freeling and load resources
	 * 
	 * @throws Exception
	 */
	private void initiFreeling() throws Exception {

		// Instantiate Freeling resources if not already done
		if(tk == null) {
			logger.info("Initializing Freeling (language " + LANG + ")...");

			System.loadLibrary("freeling_javaAPI");

			Util.initLocale("default");

			// Create options set for maco analyzer.
			// Default values are Ok, except for data files.
			MacoOptions op = new MacoOptions( LANG );

			op.setDataFiles( "", 
					DATA + "common/punct.dat",
					DATA + LANG + "/dicc.src",
					DATA + LANG + "/afixos.dat",
					"",
					DATA + LANG + "/locucions.dat", 
					DATA + LANG + "/np.dat",
					DATA + LANG + "/quantities.dat",
					DATA + LANG + "/probabilitats.dat");

			// Create analyzers.

			tk = new Tokenizer( DATA + LANG + "/tokenizer.dat" );
			sp = new Splitter( DATA + LANG + "/splitter.dat" );
			SWIGTYPE_p_splitter_status sid = sp.openSession();

			mf = new Maco( op );
			mf.setActiveOptions(false, true, true, true,  // select which among created 
					true, true, false, true,  // submodules are to be used. 
					true, true, true, true);  // default: all created submodules 
			// are used

			tg = new HmmTagger( DATA + LANG + "/tagger.dat", true, 2 );
			parser = new ChartParser( DATA + LANG + "/chunker/grammar-chunk.dat" );
			dep = new DepTxala( DATA + LANG + "/dep_txala/dependences.dat",
					parser.getStartSymbol() );
			neclass = new Nec( DATA + LANG + "/nerc/nec/nec-ab-poor1.dat" );

			sen = new Senses( DATA + LANG + "/senses.dat" ); // sense dictionary
			dis = new Ukb( DATA + LANG + "/ukb.dat" ); // sense disambiguator

			logger.info("Freeling initialized (language " + LANG + ").");

			// Init GATE ssplit
			String resourcePath = PropertyManager.getProperty("textdigester.resource");
			resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;

			File XGAPP_ssplit = new File(resourcePath + "Preprocessing_app_1.xgapp");
			try {
				corpusController_preprocess_ssplit = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_ssplit);
			} catch (IOException e) {
				e.printStackTrace();
				throw new TextDigesterException("Error while initializing XGAPP ssplit: " + e.getMessage());
			}

		}

	}

	public static LangENUM getLanguage(String text) {
		if(lgid == null) {
			System.loadLibrary("freeling_javaAPI");

			Util.initLocale("default");

			// language detector. Used just to show it. Results are printed  but ignored. 
			// See below.
			lgid = new LangIdent(DATA + "/common/lang_ident/ident.dat");
		}

		if(text != null && text.length() > 0) {
			String language = lgid.identifyLanguage(text);

			if(language == null) {
				return null;
			}
			if(language.equals("en")) {
				return LangENUM.English;
			}
			if(language.equals("es")) {
				return LangENUM.Spanish;
			}
			if(language.equals("ca")) {
				return LangENUM.Catalan;
			}

			return null;
		}
		else {
			return null;
		}
	}

	private Sentence analyzeSentenceText(String sentenceText) {
		// Extract the tokens from the line of text
		ListWord l = tk.tokenize(sentenceText);

		Sentence sent = new Sentence(l);

		// Perform morphological analysis
		mf.analyze(sent);

		// Perform part-of-speech tagging.
		tg.analyze(sent);

		// Perform named entity (NE) classification
		// neclass.analyze(sent); - DISABLED

		// sen.analyze(sent); - DISABLED

		// dis.analyze(sent); - DISABLED

		// Chunk parser
		//parser.analyze(sent); - DISABLED

		// Dependency parser
		// dep.analyze(sent); - DISABLED

		return sent;
	}


	/**
	 * Sentence split, morpho analysis, lemmtization, tagging and word sense disambiguation
	 * 
	 * @param text
	 * @return
	 */
	private ListSentence analyzeText(String text) {

		// Extract the tokens from the line of text
		ListWord l = tk.tokenize(text);

		SWIGTYPE_p_splitter_status sid = sp.openSession();

		ListSentence ls = sp.split(sid, l, false); // Original: true

		// Perform morphological analysis
		mf.analyze(ls);

		// Perform part-of-speech tagging.
		tg.analyze(ls);

		// Perform named entity (NE) classification
		// neclass.analyze(ls); - DISABLED

		// sen.analyze(ls); - DISABLED

		// dis.analyze(ls); - DISABLED

		// Chunk parser
		//parser.analyze(ls); - DISABLED

		// Dependency parser
		// dep.analyze(ls); - DISABLED

		return ls;
	}

	/**
	 * Parse a plain text by Freeling and return a {@link edu.upf.taln.textdigester.model.TDDocument}
	 * 
	 * @param inputText
	 * @return
	 */
	public TDDocument generateDocumentFromFreeText(String inputText, String name) {

		if(inputText != null) {
			logger.info("Parsing plain text (language " + LANG + ", text length " + inputText.length() + " chars)...");

			// Maps to store the start offset, end offset, name and features of the annotations that will be added to the inputText in a GATE document
			Map<Long, Long> GATEann_StartOffset = new HashMap<Long, Long>();
			Map<Long, Long> GATEann_EndOffset = new HashMap<Long, Long>();
			Map<Long, String> GATEann_annName = new HashMap<Long, String>();
			Map<Long, FeatureMap> GATEann_FeatureMap = new HashMap<Long, FeatureMap>();

			// Grouping all the annotation features by means of the same integer
			long annNum = 0l;
			long startDocumentOffset = 0l;
			long endDocumentOffset = inputText.length();

			// *** Adding Document annotation ***
			GATEann_StartOffset.put(annNum, 0l);
			GATEann_EndOffset.put(annNum, endDocumentOffset);
			GATEann_annName.put(annNum, TDDocument.documentAnnType);
			GATEann_FeatureMap.put(annNum, Factory.newFeatureMap());
			annNum++;

			// Visiting all the sentences spotted inside the text of the tweet
			ListSentenceIterator sIt = new ListSentenceIterator(analyzeText(inputText));

			while (sIt.hasNext()) {

				Sentence s = sIt.next();
				// DepTree dt = s.getDepTree(); - NOT USED
				// ParseTree pt = s.getParseTree(); - NOT USED

				// Iterating sentence words
				ListWordIterator wIt = new ListWordIterator(s);

				Long stenStart = null;
				Long stenFinish = null;

				while (wIt.hasNext()) {
					Word w = wIt.next();

					if(w.getLemma() != null) {

						// Getting word features
						if(stenStart == null || stenStart > w.getSpanStart()) {
							stenStart = w.getSpanStart();
						}

						if(stenFinish == null || stenFinish < w.getSpanFinish()) {
							stenFinish = w.getSpanFinish();
						}

						FeatureMap wordFeats = Factory.newFeatureMap();

						wordFeats.put(TDDocument.token_startSpanFeatName, w.getSpanStart());
						wordFeats.put(TDDocument.token_endSpanFeatName, w.getSpanFinish());
						wordFeats.put(TDDocument.token_lemmaFeatName, w.getLemma());
						wordFeats.put(TDDocument.token_positionFeatName, "" + w.getPosition());

						if(w.getForm() != null) {
							wordFeats.put(TDDocument.token_formStringFeatName, w.getForm());
						}

						if(w.getTag() != null) {
							wordFeats.put(TDDocument.token_POSFeatName, w.getTag());
						}

						if(w.getPhForm() != null && !w.getPhForm().equals("")) {
							wordFeats.put(TDDocument.token_phFormStringFeatName, w.getPhForm());
						}

						// *** Add word annotation ***
						GATEann_StartOffset.put(annNum, startDocumentOffset + w.getSpanStart());
						GATEann_EndOffset.put(annNum, startDocumentOffset + w.getSpanFinish());
						GATEann_annName.put(annNum, TDDocument.tokenAnnType);
						GATEann_FeatureMap.put(annNum, wordFeats);
						annNum++;
					}
				}

				// *** Add sentence annotation ***
				GATEann_StartOffset.put(annNum, startDocumentOffset + stenStart);
				GATEann_EndOffset.put(annNum, startDocumentOffset + stenFinish);
				GATEann_annName.put(annNum, TDDocument.sentenceAnnType);
				GATEann_FeatureMap.put(annNum, Factory.newFeatureMap());
				annNum++;

			}

			GtUtils.initGate();

			// Writing textual contents to GATE document
			try {
				Document gateDoc = Factory.newDocument(inputText);

				// Adding annotations to GATE document
				for(Entry<Long, Long> featID : GATEann_StartOffset.entrySet()) {
					Long featIDvalue = featID.getKey();

					if(GATEann_StartOffset.get(featIDvalue) != null && GATEann_EndOffset.get(featIDvalue) != null &&
							GATEann_annName.get(featIDvalue) != null && !GATEann_annName.get(featIDvalue).equals("") && GATEann_FeatureMap.get(featIDvalue) != null) {
						try {
							gateDoc.getAnnotations(TDDocument.mainAnnSet).add(GATEann_StartOffset.get(featIDvalue), GATEann_EndOffset.get(featIDvalue), GATEann_annName.get(featIDvalue), GATEann_FeatureMap.get(featIDvalue));
							logger.debug("Added feature: " + GATEann_annName.get(featIDvalue) + " (" + GATEann_StartOffset.get(featIDvalue) + ", " + GATEann_EndOffset.get(featIDvalue) + ") feature map size: " + GATEann_FeatureMap.get(featIDvalue).size());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						logger.debug("******** IMPOSSIBLE TO ADD ANNOTATION *****************");
					}
				}


				logger.info("Parsed plain text (language " + LANG + ", text length " + inputText.length() + " chars) - added " + GATEann_StartOffset.size() + " text annotations.");

				return new TDDocument(inputText, gateDoc, name);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Parse a {@link edu.upf.taln.textdigester.model.TDDocument}
	 * All the results are stored in the annotation set Analysis
	 * 
	 * @param inputDoc
	 * @return
	 */
	public TDDocument parseDocumentSentences(TDDocument inputDoc) {

		if(inputDoc == null) {
			return null;
		}

		// Remove sentences and tokens
		GtUtils.removeAnnotationOfType(inputDoc.getGATEdoc(), TDDocument.mainAnnSet, TDDocument.sentenceAnnType);
		GtUtils.removeAnnotationOfType(inputDoc.getGATEdoc(), TDDocument.mainAnnSet, TDDocument.tokenAnnType);

		String inputText = null;
		try {
			inputText = inputDoc.getGATEdoc().getContent().getContent(0l, Long.valueOf(gate.Utils.length(inputDoc.getGATEdoc()))).toString();
		} catch (InvalidOffsetException e2) {
			e2.printStackTrace();
		}

		if(inputText != null) {
			logger.info("Parsing GATE document (language " + LANG + ", text length " + inputText.length() + " chars)...");

			// Maps to store the start offset, end offset, name and features of the annotations that will be added to the inputText in a GATE document
			Map<Long, Long> GATEann_StartOffset = new HashMap<Long, Long>();
			Map<Long, Long> GATEann_EndOffset = new HashMap<Long, Long>();
			Map<Long, String> GATEann_annName = new HashMap<Long, String>();
			Map<Long, FeatureMap> GATEann_FeatureMap = new HashMap<Long, FeatureMap>();

			// Grouping all the annotation features by means of the same integer
			long annNum = 0l;
			long startDocumentOffset = 0l;
			long endDocumentOffset = inputText.length();

			// *** Adding Document annotation ***
			GATEann_StartOffset.put(annNum, 0l);
			GATEann_EndOffset.put(annNum, endDocumentOffset);
			GATEann_annName.put(annNum, TDDocument.documentAnnType);
			GATEann_FeatureMap.put(annNum, Factory.newFeatureMap());
			annNum++;

			// Visiting all the sentences spotted inside the text of the tweet
			ListSentenceIterator sIt = new ListSentenceIterator(analyzeText(inputText));

			while (sIt.hasNext()) {

				Sentence s = sIt.next();
				// DepTree dt = s.getDepTree(); - NOT USED
				// ParseTree pt = s.getParseTree(); - NOT USED

				// Iterating sentence words
				ListWordIterator wIt = new ListWordIterator(s);

				Long stenStart = null;
				Long stenFinish = null;

				while (wIt.hasNext()) {
					Word w = wIt.next();

					if(w.getLemma() != null) {

						// Getting word features
						if(stenStart == null || stenStart > w.getSpanStart()) {
							stenStart = w.getSpanStart();
						}

						if(stenFinish == null || stenFinish < w.getSpanFinish()) {
							stenFinish = w.getSpanFinish();
						}

						FeatureMap wordFeats = Factory.newFeatureMap();

						wordFeats.put(TDDocument.token_startSpanFeatName, w.getSpanStart());
						wordFeats.put(TDDocument.token_endSpanFeatName, w.getSpanFinish());
						wordFeats.put(TDDocument.token_lemmaFeatName, w.getLemma());
						wordFeats.put(TDDocument.token_positionFeatName, "" + w.getPosition());

						if(w.getForm() != null) {
							wordFeats.put(TDDocument.token_formStringFeatName, w.getForm());
						}

						if(w.getTag() != null) {
							wordFeats.put(TDDocument.token_POSFeatName, w.getTag());
						}

						if(w.getPhForm() != null && !w.getPhForm().equals("")) {
							wordFeats.put(TDDocument.token_phFormStringFeatName, w.getPhForm());
						}

						// *** Add word annotation ***
						GATEann_StartOffset.put(annNum, startDocumentOffset + w.getSpanStart());
						GATEann_EndOffset.put(annNum, startDocumentOffset + w.getSpanFinish());
						GATEann_annName.put(annNum, TDDocument.tokenAnnType);
						GATEann_FeatureMap.put(annNum, wordFeats);
						annNum++;
					}
				}

				// *** Add sentence annotation ***
				GATEann_StartOffset.put(annNum, startDocumentOffset + stenStart);
				GATEann_EndOffset.put(annNum, startDocumentOffset + stenFinish);
				GATEann_annName.put(annNum, TDDocument.sentenceAnnType);
				GATEann_FeatureMap.put(annNum, Factory.newFeatureMap());
				annNum++;

			}

			GtUtils.initGate();

			// Writing textual contents to GATE document
			try {

				// Adding annotations to GATE document
				for(Entry<Long, Long> featID : GATEann_StartOffset.entrySet()) {
					Long featIDvalue = featID.getKey();

					if(GATEann_StartOffset.get(featIDvalue) != null && GATEann_EndOffset.get(featIDvalue) != null &&
							GATEann_annName.get(featIDvalue) != null && !GATEann_annName.get(featIDvalue).equals("") && GATEann_FeatureMap.get(featIDvalue) != null) {
						try {
							inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(GATEann_StartOffset.get(featIDvalue), GATEann_EndOffset.get(featIDvalue), GATEann_annName.get(featIDvalue), GATEann_FeatureMap.get(featIDvalue));
							logger.debug("Added feature: " + GATEann_annName.get(featIDvalue) + " (" + GATEann_StartOffset.get(featIDvalue) + ", " + GATEann_EndOffset.get(featIDvalue) + ") feature map size: " + GATEann_FeatureMap.get(featIDvalue).size());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						logger.debug("******** IMPOSSIBLE TO ADD ANNOTATION *****************");
					}
				}


				logger.info("Parsed plain text (language " + LANG + ", text length " + inputText.length() + " chars) - added " + GATEann_StartOffset.size() + " text annotations.");

				return inputDoc;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return inputDoc;
	}


	/**
	 * Parse the Sentence annotations of a document {@link edu.upf.taln.textdigester.model.TDDocument}
	 * All the results are stored in the annotation set Analysis
	 * 
	 * @param inputDoc
	 * @return
	 */
	public TDDocument parseDocumentGTSentences(TDDocument inputDoc) {

		if(inputDoc == null) {
			return null;
		}

		// Remove sentences and tokens
		GtUtils.removeAnnotationOfType(inputDoc.getGATEdoc(), TDDocument.mainAnnSet, TDDocument.sentenceAnnType);
		GtUtils.removeAnnotationOfType(inputDoc.getGATEdoc(), TDDocument.mainAnnSet, TDDocument.tokenAnnType);

		// Identify sentences
		Corpus corpusToProcess = new CorpusImpl();
		corpusToProcess.add(inputDoc.getGATEdoc());
		synchronized(LOCK_corpusController_preprocess_ssplit) {

			corpusController_preprocess_ssplit.setCorpus(corpusToProcess);
			try {
				corpusController_preprocess_ssplit.execute();
			} catch (ExecutionException e) {
				logger.warn("Exception / error while extracting sentences (ssplit): " + e.getMessage());
				e.printStackTrace();
			}
			corpusController_preprocess_ssplit.setCorpus(null);
		}

		// Copy to mainAnnSet all title and sectHeader
		AnnotationSet titleAnnSet_MOVE = inputDoc.getGATEdoc().getAnnotations("Original markups").get(TDDocument.titleAnnType);
		AnnotationSet headerSectAnnSet_MOVE = inputDoc.getGATEdoc().getAnnotations("Original markups").get(TDDocument.sectHeaderAnnType);
		List<Annotation> annToMoveList_MOVE = gate.Utils.inDocumentOrder(titleAnnSet_MOVE);
		annToMoveList_MOVE.addAll( gate.Utils.inDocumentOrder(headerSectAnnSet_MOVE));

		for(Annotation annToMove : annToMoveList_MOVE) {
			if(annToMove != null) {
				try {
					inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(annToMove.getStartNode().getOffset(),
							annToMove.getEndNode().getOffset(), annToMove.getType(), annToMove.getFeatures());
				} catch (InvalidOffsetException e) {
					e.printStackTrace();
				}
			}
		}

		// Remove sentences that overlaps annotation in mainAnnSet of type title and sectHeader
		AnnotationSet titleAnnSet = inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.titleAnnType);
		AnnotationSet sectHeaderAnnSet = inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sectHeaderAnnType);
		List<Annotation> listToDelOverlappingSent = gate.Utils.inDocumentOrder(titleAnnSet);
		listToDelOverlappingSent.addAll(gate.Utils.inDocumentOrder(sectHeaderAnnSet));
		
		boolean changesDone = true;
		
		Annotation sentToDel = null;
		
		while(changesDone) {
			changesDone = false;
			sentToDel = null;
			
			if(listToDelOverlappingSent != null && listToDelOverlappingSent.size() > 0) {
				AnnotationSet sentAnnSet = inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
				Iterator<Annotation> sentAnnSetIter = sentAnnSet.iterator();
				while(sentAnnSetIter.hasNext()) {
					Annotation sentAnn = sentAnnSetIter.next();
					for(Annotation overlapElem : listToDelOverlappingSent) {
						System.out.println("Sentence: " + sentAnn.getStartNode().getOffset() + " - " + sentAnn.getEndNode().getOffset());
						System.out.println("Overlap: " + overlapElem.getStartNode().getOffset() + " - " + overlapElem.getEndNode().getOffset());					

						// Resize sentence annotation if this is the case
						if(sentAnn.getEndNode().getOffset() <= overlapElem.getEndNode().getOffset() &&
								sentAnn.getStartNode().getOffset() >= overlapElem.getStartNode().getOffset()) {
							System.out.println("DELETE");
							changesDone = true;
							sentToDel = sentAnn;
							break;
						} 
						else if(sentAnn.getStartNode().getOffset() < overlapElem.getStartNode().getOffset() &&
								(sentAnn.getEndNode().getOffset() > overlapElem.getStartNode().getOffset() && sentAnn.getEndNode().getOffset() <= overlapElem.getEndNode().getOffset())) {
							try {
								inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(sentAnn.getStartNode().getOffset(), overlapElem.getStartNode().getOffset(), TDDocument.sentenceAnnType, sentAnn.getFeatures());
								System.out.println("RESIZE BEFORE");
								changesDone = true;
								sentToDel = sentAnn;
								break;
							} catch (InvalidOffsetException e) {
								e.printStackTrace();
							}
						}
						else if(sentAnn.getEndNode().getOffset() > overlapElem.getEndNode().getOffset() &&
								(sentAnn.getStartNode().getOffset() >= overlapElem.getStartNode().getOffset() && sentAnn.getStartNode().getOffset() < overlapElem.getEndNode().getOffset())) {
							try {
								inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(overlapElem.getEndNode().getOffset(), sentAnn.getEndNode().getOffset(), TDDocument.sentenceAnnType, sentAnn.getFeatures());
								System.out.println("RESIZE AFTER");
								changesDone = true;
								sentToDel = sentAnn;
								break;
							} catch (InvalidOffsetException e) {
								e.printStackTrace();
							}
						}
						else if(sentAnn.getEndNode().getOffset() > overlapElem.getEndNode().getOffset() &&
								sentAnn.getStartNode().getOffset() < overlapElem.getStartNode().getOffset()) {
							try {
								if(sentAnn.getEndNode().getOffset() - overlapElem.getEndNode().getOffset() > 15l) {
									inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(overlapElem.getEndNode().getOffset(), sentAnn.getEndNode().getOffset(), TDDocument.sentenceAnnType, sentAnn.getFeatures());
								}
								
								if(overlapElem.getStartNode().getOffset() - sentAnn.getStartNode().getOffset() > 15l) {
									inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(sentAnn.getStartNode().getOffset(), overlapElem.getStartNode().getOffset(), TDDocument.sentenceAnnType, sentAnn.getFeatures());
								}
								
								System.out.println("RESIZE BOTH");
								changesDone = true;
								sentToDel = sentAnn;
								break;
							} catch (InvalidOffsetException e) {
								e.printStackTrace();
							}
						}
					}
					
					if(changesDone) {
						break;
					}
				}
			}
			
			if(sentToDel != null) {
				inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).remove(sentToDel);
			}
		}
		
		
		
		
		
		// Parsing all the annotations of the mainAnnSet as sentences
		List<Annotation> annotationsToParse = gate.Utils.inDocumentOrder(inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet));

		for(Annotation toParseAnn : annotationsToParse) {

			try {
				String sentText = GtUtils.getTextOfAnnotation(toParseAnn, inputDoc.getGATEdoc());

				if(!Strings.isNullOrEmpty(sentText) && sentText.trim().length() > 0) {
					logger.info("Parsing sentence (language " + LANG + ", text length " + sentText.length() + " chars)...");

					// Maps to store the start offset, end offset, name and features of the annotations that will be added to the inputText in a GATE document
					Map<Long, Long> GATEann_StartOffset = new HashMap<Long, Long>();
					Map<Long, Long> GATEann_EndOffset = new HashMap<Long, Long>();
					Map<Long, String> GATEann_annName = new HashMap<Long, String>();
					Map<Long, FeatureMap> GATEann_FeatureMap = new HashMap<Long, FeatureMap>();

					// Grouping all the annotation features by means of the same integer
					long annNum = 0l;
					long startDocumentOffset = toParseAnn.getStartNode().getOffset();
					long endDocumentOffset = toParseAnn.getEndNode().getOffset();

					Sentence s = analyzeSentenceText(sentText);

					// Iterating sentence words
					ListWordIterator wIt = new ListWordIterator(s);

					Long stenStart = null;
					Long stenFinish = null;

					while (wIt.hasNext()) {
						Word w = wIt.next();

						if(w.getLemma() != null) {

							// Getting word features
							if(stenStart == null || stenStart > w.getSpanStart()) {
								stenStart = w.getSpanStart();
							}

							if(stenFinish == null || stenFinish < w.getSpanFinish()) {
								stenFinish = w.getSpanFinish();
							}

							FeatureMap wordFeats = Factory.newFeatureMap();

							wordFeats.put(TDDocument.token_startSpanFeatName, w.getSpanStart());
							wordFeats.put(TDDocument.token_endSpanFeatName, w.getSpanFinish());
							wordFeats.put(TDDocument.token_lemmaFeatName, w.getLemma());
							wordFeats.put(TDDocument.token_positionFeatName, "" + w.getPosition());

							if(w.getForm() != null) {
								wordFeats.put(TDDocument.token_formStringFeatName, w.getForm());
							}

							if(w.getTag() != null) {
								wordFeats.put(TDDocument.token_POSFeatName, w.getTag());
							}

							if(w.getPhForm() != null && !w.getPhForm().equals("")) {
								wordFeats.put(TDDocument.token_phFormStringFeatName, w.getPhForm());
							}

							// *** Add word annotation ***
							GATEann_StartOffset.put(annNum, startDocumentOffset + w.getSpanStart());
							GATEann_EndOffset.put(annNum, startDocumentOffset + w.getSpanFinish());
							GATEann_annName.put(annNum, TDDocument.tokenAnnType);
							GATEann_FeatureMap.put(annNum, wordFeats);
							annNum++;
						}
					}

					GtUtils.initGate();

					// Writing textual contents to GATE document
					try {
						// Delete Token annotations inside the span of the sentence
						AnnotationSet overlappingTokens = inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.tokenAnnType).getContained(toParseAnn.getStartNode().getOffset(), toParseAnn.getEndNode().getOffset());
						Iterator<Annotation> overlappingTokensIter = overlappingTokens.iterator();
						List<Annotation> annToDel = new ArrayList<Annotation>();
						while(overlappingTokensIter.hasNext()) {
							Annotation ann = overlappingTokensIter.next();
							annToDel.add(ann);
						}
						for(Annotation annToD : annToDel) {
							inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).remove(annToD);
						}


						// Adding annotations to GATE document
						for(Entry<Long, Long> featID : GATEann_StartOffset.entrySet()) {
							Long featIDvalue = featID.getKey();

							if(GATEann_StartOffset.get(featIDvalue) != null && GATEann_EndOffset.get(featIDvalue) != null &&
									GATEann_annName.get(featIDvalue) != null && !GATEann_annName.get(featIDvalue).equals("") && GATEann_FeatureMap.get(featIDvalue) != null) {
								try {
									inputDoc.getGATEdoc().getAnnotations(TDDocument.mainAnnSet).add(GATEann_StartOffset.get(featIDvalue), GATEann_EndOffset.get(featIDvalue), GATEann_annName.get(featIDvalue), GATEann_FeatureMap.get(featIDvalue));
									logger.debug("Added feature: " + GATEann_annName.get(featIDvalue) + " (" + GATEann_StartOffset.get(featIDvalue) + ", " + GATEann_EndOffset.get(featIDvalue) + ") feature map size: " + GATEann_FeatureMap.get(featIDvalue).size());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							else {
								logger.debug("******** IMPOSSIBLE TO ADD ANNOTATION *****************");
							}
						}


						logger.info("Parsed sentence text (language " + LANG + ", text length " + sentText.length() + " chars) - added " + GATEann_StartOffset.size() + " text annotations.");

					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		return inputDoc;
	}
}
