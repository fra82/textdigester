/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.importer;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;

/**
 * Import an HTML page to summarize from its URL by removing boilerplate.
 * 
 * @author Francesco Ronzano
 *
 */
public class HTMLimporter {

	private static final Logger logger = LoggerFactory.getLogger(HTMLimporter.class);

	/**
	 * Extract the main text from an HTML page
	 * 
	 * @param originURL
	 * @return
	 * @throws TextDigesterException 
	 */
	public static TDDocument extractText(URL originURL) throws TextDigesterException {
		try {
			return new TDDocument(ArticleExtractor.INSTANCE.getText(originURL), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}