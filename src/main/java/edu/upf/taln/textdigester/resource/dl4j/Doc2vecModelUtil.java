/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.resource.dl4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.models.embeddings.learning.impl.sequence.DM;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;

/**
 * Deeplearning4j Utility Class
 * 
 * @author Francesco Ronzano
 *
 */
public class Doc2vecModelUtil {

	private static final Logger logger = LoggerFactory.getLogger(Doc2vecModelUtil.class);

	public static void main(String[] args) {
		PropertyManager.setPropertyFilePath(PropertyManager.defaultPropertyFilePath);
		
		// STEP 1: pre-processing
		// prepareFiles("/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.es", LangENUM.Spanish, false, false);

		// STEP 2: generated doc2Vec
		// generateDoc2VecModel("/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.ca/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.ca/PROCESSED_SAMPLE/par2vec_model_v1.zip");
		
		// generateDocFromWord2VecModel("/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.ca/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.ca/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.ca/cawiki_parw2vec.zip");
		// generateDocFromWord2VecModel("/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.en/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.en/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.en/enwiki_parw2vec.zip");
		generateDocFromWord2VecModel("/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.es/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.es/PROCESSED_SAMPLE", "/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.es/eswiki_parw2vec.zip");

		// STEP 3: eval sentence
		/*
		long start = System.currentTimeMillis();
		INDArray vector = loadDoc2VecModelAndEvalVect("/home/francesco/Desktop/NLP_HACHATHON_4YFN/RAW_WIKIPEDIA/raw.ca/parw2vec_model_v1.zip",
				"L'exconseller de la Presidència farà notar aquest dilluns al Tribunal Suprem la \"contradicció\" de la fiscalia: \"En funció de les idees, les coses poden ser motiu d'arxivament o un delicte molt greu",
				false, false, LangENUM.Catalan);

		System.out.println("Loading time: " + (System.currentTimeMillis() - start));
		System.out.println("VECTOR: " + vector.toString());
		*/
	}	


	/**
	 * Load a model and build the paragraph vector of the sentence 
	 *
	 * @param pathModel
	 * @param isWord2VecModel
	 * @param sentenceToEval
	 * @param getLemma
	 * @param toLowercase
	 * @param lang
	 * @return
	 */
	public static INDArray loadDoc2VecModelAndEvalVect(String pathModel, String sentenceToEval,
			boolean getLemma, boolean toLowercase, LangENUM lang) {
		try {
			ParagraphVectors vecLoaded = null;
			long start = System.currentTimeMillis();

			vecLoaded = WordVectorSerializer.readParagraphVectors(new File(pathModel));

			logger.info("Model loaded in " + (System.currentTimeMillis() - start) + " ms.");

			TokenizerFactory t = new DefaultTokenizerFactory();
			t.setTokenPreProcessor(new CommonPreprocessor());
			vecLoaded.setTokenizerFactory(t);
			
			start = System.currentTimeMillis();

			String line = lineToSent(sentenceToEval, getLemma, toLowercase, lang);

			INDArray vector = vecLoaded.inferVector(line);

			logger.info("Computing time: " + (System.currentTimeMillis() - start));

			return vector;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}


	/**
	 * Generate a doc2vec model starting from the text files
	 * @param pathDrWithTxtFiles absolute path of the text file or directory including text files - one sentence per line
	 * @param pathOutputModel absolute path where to store the output model (as a .zip file)
	 */
	public static void generateDoc2VecModel(String pathDirWithTxtFiles, String pathOutputModel) {

		File sentenceFile = new File(pathDirWithTxtFiles);
		SentenceIterator iter = null;
		try {
			iter = new FileSentenceIterator(sentenceFile);
			iter.setPreProcessor(new SentencePreProcessor() {
				private static final long serialVersionUID = 1L;

				@Override
				public String preProcess(String sentence) {
					return sentence; // Here any pre-processing to sentence text can be performed
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} //UimaSentenceIterator.createWithPath(file.getAbsolutePath());

		iter.reset();

		// InMemoryLookupCache cache = new InMemoryLookupCache(false);
		AbstractCache<VocabWord> cache = new AbstractCache.Builder<VocabWord>().build();

		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());

		ParagraphVectors vec = new ParagraphVectors.Builder()
				.minWordFrequency(3)
				.iterations(5)
				.epochs(1)
				.layerSize(100)
				.learningRate(0.025)
				.windowSize(5)
				.iterate(iter)
				.trainWordVectors(true)
				.vocabCache(cache)
				.tokenizerFactory(t)
				.sampling(0)
				.sequenceLearningAlgorithm(new DM<VocabWord>())
				.build();

		vec.fit();

		logger.info("Model trained.");

		File model = new File(pathOutputModel);
		try {
			model.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		WordVectorSerializer.writeParagraphVectors(vec, model);

		logger.info("Model stored to: " + model.getAbsolutePath());
	}


	/**
	 * Generate a word2vec model starting from the text files
	 * @param pathDrWithTxtFiles absolute path of the text file or directory including text files - one sentence per line
	 * @param pathOutputModel absolute path where to store the output model (as a .zip file)
	 */
	public static void generateDocFromWord2VecModel(String pathDirWithWord2VecTxtFiles, String pathDirWithPar2VecTxtFiles, String pathOutputModel) {

		File sentenceFile = new File(pathDirWithWord2VecTxtFiles);
		SentenceIterator iter = null;
		try {
			iter = new FileSentenceIterator(sentenceFile);
			iter.setPreProcessor(new SentencePreProcessor() {
				private static final long serialVersionUID = 1L;

				@Override
				public String preProcess(String sentence) {
					return sentence; // Here any pre-processing to sentence text can be performed
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} //UimaSentenceIterator.createWithPath(file.getAbsolutePath());

		iter.reset();

		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());

		Word2Vec wordVectModel = new Word2Vec.Builder()
				.minWordFrequency(1)
				.batchSize(250)
				.iterations(3)
				.epochs(1)
				.learningRate(0.025)
				.layerSize(100)
				.minLearningRate(0.001)
				.windowSize(5)
				.iterate(iter)
				.tokenizerFactory(t)
				.build();

		wordVectModel.fit();

		logger.info("Word2vec model trained.");

		File sentenceFile_par = new File(pathDirWithPar2VecTxtFiles);
		SentenceIterator iter_par = null;
		try {
			iter_par = new FileSentenceIterator(sentenceFile_par);
			iter_par.setPreProcessor(new SentencePreProcessor() {
				private static final long serialVersionUID = 1L;

				@Override
				public String preProcess(String sentence) {
					return sentence; // Here any pre-processing to sentence text can be performed
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} //UimaSentenceIterator.createWithPath(file.getAbsolutePath());

		iter_par.reset();

		// InMemoryLookupCache cache = new InMemoryLookupCache(false);
		AbstractCache<VocabWord> cache = new AbstractCache.Builder<VocabWord>().build();

		ParagraphVectors paragraphVectModel = new ParagraphVectors.Builder()
				.minWordFrequency(3)
				.iterations(5)
				.epochs(1)
				.layerSize(100)
				.learningRate(0.025)
				.windowSize(5)
				.iterate(iter_par)
				.trainWordVectors(true)
				.vocabCache(cache)
				.tokenizerFactory(t)
				.sampling(0)
				.sequenceLearningAlgorithm(new DM<VocabWord>())
				.useExistingWordVectors(wordVectModel)
				.build();

		paragraphVectModel.fit();


		File model = new File(pathOutputModel);
		try {
			model.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		WordVectorSerializer.writeWord2VecModel(paragraphVectModel, model);

		logger.info("Model stored to: " + model.getAbsolutePath());
	}
	
	
	/**
	 * Convert a line of text (including one or multiple sentences) to a list of sentences, each one represented as a token sequence 
	 * by specifying if to get the lemma or form of each token and if to lowecase it
	 * 
	 * @param line
	 * @param flProc
	 * @param getLemma getLemma if true each token is replaced by its lemma, if false by its word-form
	 * @param toLowercase if true each token is lowercased
	 * @return
	 */
	public static List<String> lineToSentList(String line, FlProcessor flProc, boolean getLemma, boolean toLowercase, LangENUM lang) {
		List<String> sentStringList = new ArrayList<String>();

		try {
			GtUtils.initGate();
			Document gateDocPre = Factory.newDocument(line);
			TDDocument doc = flProc.parseDocumentGTSentences(new TDDocument(line, gateDocPre, "name"), lang);
			gateDocPre = doc.getGATEdoc();

			AnnotationSet sent = gateDocPre.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
			Iterator<Annotation> sentIter = sent.iterator();
			while(sentIter.hasNext()) {
				String sentString = "";

				Annotation senten = sentIter.next();

				AnnotationSet tokenOfSent = gateDocPre.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.tokenAnnType).getContained(senten.getStartNode().getOffset(), senten.getEndNode().getOffset());
				Iterator<Annotation> tokIter = tokenOfSent.iterator();
				while(tokIter.hasNext()) {
					try {
						Annotation tokenAnn = tokIter.next();
						if(tokenAnn != null && tokenAnn.getFeatures() != null) {
							if(!getLemma && tokenAnn.getFeatures().containsKey(TDDocument.token_formStringFeatName) &&
									tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName) != null && 
									!((String) tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName)).trim().equals("")) {
								String sentTokenized = (sentString.length() == 0) ? ((String) tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName)) : " " + ((String) tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName));
								sentString += (toLowercase) ? sentTokenized.toLowerCase() : sentTokenized;
							}
							else if(getLemma && tokenAnn.getFeatures().containsKey(TDDocument.token_lemmaFeatName) &&
									tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName) != null && 
									!((String) tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName)).trim().equals("")) {
								String sentTokenized = (sentString.length() == 0) ? ((String) tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName)) : " " + ((String) tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName));
								sentString += (toLowercase) ? sentTokenized.toLowerCase() : sentTokenized;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if(sentString != null && sentString.trim().length() > 0 && sentString.trim().length() > 30 && 
						!sentString.trim().startsWith("<") && !sentString.trim().startsWith("[")) {
					sentString = sentString.trim();
					sentStringList.add(sentString);
				}
			}

			gateDocPre.cleanup();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sentStringList;
	}

	
	/**
	 * Convert a line of text (including one or multiple sentences) to a token sequence by specifying if to get the lemma or form of each
	 * token and if to lowecase it
	 * 
	 * @param line
	 * @param flProc
	 * @param getLemma getLemma if true each token is replaced by its lemma, if false by its word-form
	 * @param toLowercase if true each token is lowercased
	 * @return
	 */
	public static String lineToSent(String line, boolean getLemma, boolean toLowercase, LangENUM lang) {
		String sentStringRet = "";

		try {
			GtUtils.initGate();
			Document gateDocPre = Factory.newDocument(line);
			TDDocument doc = FlProcessor.parseDocumentGTSentences(new TDDocument(line, gateDocPre, "name"), lang);
			gateDocPre = doc.getGATEdoc();

			AnnotationSet sent = gateDocPre.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
			Iterator<Annotation> sentIter = sent.iterator();
			while(sentIter.hasNext()) {
				String sentString = "";

				Annotation senten = sentIter.next();

				AnnotationSet tokenOfSent = gateDocPre.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.tokenAnnType).getContained(senten.getStartNode().getOffset(), senten.getEndNode().getOffset());
				Iterator<Annotation> tokIter = tokenOfSent.iterator();
				while(tokIter.hasNext()) {
					try {
						Annotation tokenAnn = tokIter.next();
						if(tokenAnn != null && tokenAnn.getFeatures() != null) {
							if(!getLemma && tokenAnn.getFeatures().containsKey(TDDocument.token_formStringFeatName) &&
									tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName) != null && 
									!((String) tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName)).trim().equals("")) {
								String sentTokenized = (sentString.length() == 0) ? ((String) tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName)) : " " + ((String) tokenAnn.getFeatures().get(TDDocument.token_formStringFeatName));
								sentString += (toLowercase) ? sentTokenized.toLowerCase() : sentTokenized;
							}
							else if(getLemma && tokenAnn.getFeatures().containsKey(TDDocument.token_lemmaFeatName) &&
									tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName) != null && 
									!((String) tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName)).trim().equals("")) {
								String sentTokenized = (sentString.length() == 0) ? ((String) tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName)) : " " + ((String) tokenAnn.getFeatures().get(TDDocument.token_lemmaFeatName));
								sentString += (toLowercase) ? sentTokenized.toLowerCase() : sentTokenized;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if(sentString != null && sentString.trim().length() > 0 && sentString.trim().length() > 30 && 
						!sentString.trim().startsWith("<") && !sentString.trim().startsWith("[")) {
					sentStringRet += (sentStringRet.length() == 0) ? sentString.trim() : " " + sentString.trim();
				}
			}

			gateDocPre.cleanup();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sentStringRet;
	}


	/**
	 * Text file pre-processing utility
	 * 
	 * Parse all the text files of a directory with a specific language parser and store the parsing results in
	 * a file with the same location of the original one and with the name of the original with prefix "_REF.txt".
	 * 
	 * Each file of the input file is not processed if shorter than 30 characters or if starts with '<'.
	 * Each line is processed by Freeling, each token is replaced by its form or lemma and eventually lowercased.
	 *  
	 * @param dirToParse absolute local path of the directoy including the file to pre-process. All the files of this dir
	 * are parsed, except the ones ending in '_REF.txt'
	 * @param lang the language of the text file to parse
	 * @param getLemma if true each token is replaced by its lemma, if false by its word-form
	 * @param toLowercase if true each token is lowercased
	 */
	public static void prepareFiles(String dirToParse, LangENUM lang, boolean getLemma, boolean toLowercase) {

		File dirToP = new File(dirToParse);

		File[] fileOfDir = dirToP.listFiles();
		
		int appoGC = 0;

		for(File filD : fileOfDir) {

			if(filD == null || !filD.exists() || !filD.isFile()) {
				continue;
			}

			if(filD.getName().endsWith("_REF.txt")) {
				continue;
			}

			System.out.println("Start parsing file: " + filD.getAbsolutePath());

			File desdFile = new File(filD.getAbsolutePath() + "_REF.txt");

			if(desdFile != null && desdFile.exists() && desdFile.isFile()) {
				continue;
			}


			Writer out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(desdFile)));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filD), "ISO-8859-1"))) {
				String line;
				while ((line = br.readLine()) != null) {
					try {

						String sentString = lineToSent(line, getLemma, toLowercase, lang);

						if(sentString != null && sentString.trim().length() > 0 && sentString.trim().length() > 30 && 
								!sentString.trim().startsWith("<") && !sentString.trim().startsWith("[")) {
							sentString = sentString.trim();
							out.write(sentString.toLowerCase() + "\n");
						}

						appoGC++;
						if(appoGC % 1000 == 0) {
							System.gc();
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.gc();


			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
