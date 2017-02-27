/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.summarizer.txtvect;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.resource.lex.WikipediaSentenceEmbeddingModel;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import gate.Annotation;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class EmbeddingVectorWiki {
	
	private static final Logger logger = LoggerFactory.getLogger(TFIDFVectorWiki.class);
	
	public static INDArray computeEmbeddingVect(Annotation ann, TDDocument doc, LangENUM lang) {
		
		String sentence = GtUtils.getTextOfAnnotation(ann, doc.getGATEdoc());
		
		INDArray vector = null;
		try {
			vector = WikipediaSentenceEmbeddingModel.getSentenceEmbedding(lang, sentence);
		} catch (TextDigesterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return vector;
	}
	
	/**
	 * Compute the cosine similarity among the two embedding vectors
	 * 
	 * @param embed1
	 * @param embed2
	 * @return
	 */
	public static double cosSimEMBED(INDArray embed1, INDArray embed2) {
		
		if(embed1 == null || embed2 == null || embed1.length() != embed2.length()) {
			logger.warn("Null embeddings!");
			return 0d;
		}
		
		return Transforms.cosineSim(embed1, embed2);
	}
	
	/**
	 * Compute the cosine similarity among the two document annotation embeddings
	 * 
	 * @param ann1
	 * @param doc1
	 * @param ann2
	 * @param doc2
	 * @return
	 */
	public static double cosSimEMBED(Annotation ann1, TDDocument doc1, Annotation ann2, TDDocument doc2, LangENUM lang) {
		return cosSimEMBED(computeEmbeddingVect(ann1, doc1, lang), computeEmbeddingVect(ann2, doc2, lang));
	}
}
