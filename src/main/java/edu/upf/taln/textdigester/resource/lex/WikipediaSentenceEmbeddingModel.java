/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.resource.lex;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import edu.upf.taln.textdigester.resource.dl4j.Doc2vecModelUtil;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class WikipediaSentenceEmbeddingModel {

	private static final Logger logger = LoggerFactory.getLogger(WikipediaSentenceEmbeddingModel.class);

	private static Map<LangENUM, ParagraphVectors> embeddingModels = new HashMap<LangENUM, ParagraphVectors>();

	/**
	 * Load an embedding model built from Wikipedia
	 * 
	 * @param lang
	 * @return
	 * @throws TextDigesterException
	 */
	private static void loadEmbeddingModel(LangENUM lang) throws TextDigesterException {
		
		if(lang != null) {
			
			if(embeddingModels.containsKey(lang) && embeddingModels.get(lang) != null) {
				return;
			}
			
			String resourcePath = PropertyManager.getProperty("textdigester.resource");
			resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;

			String embeddingFileName = resourcePath + "embeddingmodels" + File.separator;

			switch(lang) {
			case English:
				embeddingFileName += "enwiki_parw2vec.zip";
				break;
			case Spanish:
				embeddingFileName += "eswiki_parw2vec.zip";
				break;
			case Catalan:
				embeddingFileName += "cawiki_parw2vec.zip";
				break;
			default:
				embeddingFileName += "enwiki_parw2vec.zip";
			}

			File embeddingFile = new File(embeddingFileName);

			if(embeddingFile != null && embeddingFile.exists() && embeddingFile.isFile()) {
				
				ParagraphVectors vecLoaded = null;
				long start = System.currentTimeMillis();
				
				try {
					vecLoaded = WordVectorSerializer.readParagraphVectors(embeddingFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				TokenizerFactory t = new DefaultTokenizerFactory();
				t.setTokenPreProcessor(new CommonPreprocessor());
				vecLoaded.setTokenizerFactory(t);

				logger.info("Model loaded in " + (System.currentTimeMillis() - start) + " ms.");
				
				embeddingModels.put(lang, vecLoaded);	

			}
			else {
				throw new TextDigesterException("Impossible to read embedding model for " + lang + " from file: '" +
						((embeddingFileName != null) ? embeddingFileName : "NULL")+ "'");
			}
		}
		else {
			throw new TextDigesterException("Specify a language to load an embedding model.");
		}

		return;
	}

	public static INDArray getSentenceEmbedding(LangENUM lang, String sentence) throws TextDigesterException {

		if(lang == null) {
			throw new TextDigesterException("Please, specify a language");
		}
		
		if(Strings.isNullOrEmpty(sentence)) {
			return null;
		}
		
		loadEmbeddingModel(lang);
		
		long start = System.currentTimeMillis();
		
		String sentenceTokenized = Doc2vecModelUtil.lineToSent(sentence, false, false, lang);

		INDArray vector = embeddingModels.get(lang).inferVector(sentenceTokenized);
		
		logger.info("Computing time: " + (System.currentTimeMillis() - start));
		
		return vector;
	}
	
}
