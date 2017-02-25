package edu.upf.taln.textdigester;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.importer.HTMLimporter;
import edu.upf.taln.textdigester.importer.TDXMLimporter;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.setting.PropertyManager;

public class LoadDocumentExample {
	
	private static final Logger logger = LoggerFactory.getLogger(LoadDocumentExample.class);
	
	public static void main(String[] args) {
		
		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");
		
		/* Load documents from TDXML */
		List<TDDocument> docList = null;
		try {
			docList = TDXMLimporter.extractDocuments("/home/francesco/Desktop/NLP_HACHATHON_4YFN/EXAMPLE_TEXTS/documetTEXTDIGESTER.xml");
			logger.debug("TEXTS LOADED: " + docList.size());
			
			for(TDDocument document : docList) {
				logger.debug(">>> NAME: '" + document.getDocumentName() + "' :\n" + document.getOriginalText());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Extract main-text from HTML page and parse it */
		TDDocument HTMLdoc = null;
		try {
			HTMLdoc = HTMLimporter.extractText(new URL("http://politica.elpais.com/politica/2017/02/22/actualidad/1487762346_547683.html"));
			logger.debug("TEXT: " + HTMLdoc.getOriginalText());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
