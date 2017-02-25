package edu.upf.taln.textdigester;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.importer.HTMLimporter;
import edu.upf.taln.textdigester.importer.TDXMLimporter;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import gate.AnnotationSet;

public class ParseDocumentExample {

	private static final Logger logger = LoggerFactory.getLogger(ParseDocumentExample.class);

	public static void main(String[] args) {
		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");

		/* Extract main-text from HTML page and parse it */
		TDDocument HTMLdoc = null;
		try {
			HTMLdoc = HTMLimporter.extractText(new URL("http://politica.elpais.com/politica/2017/02/22/actualidad/1487762346_547683.html"));
			logger.debug("TEXT: " + HTMLdoc.getOriginalText());

		} catch (Exception e) {
			e.printStackTrace();
		}

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


		/* Process text document */
		LangENUM languageOfHTMLdoc = FlProcessor.getLanguage(docList.get(0).getOriginalText());
		FlProcessor flProc = null;
		try {
			flProc = new FlProcessor(languageOfHTMLdoc);
		}
		catch(Exception e) {
			e.printStackTrace();
			return;
		}

		AnnotationSet sentenceAnnotationsBefore = docList.get(0).getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
		AnnotationSet tokenAnnotationsBefore = docList.get(0).getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.tokenAnnType);

		logger.info("Number of sentences identified BEFORE Freeling processing: " + sentenceAnnotationsBefore.size());
		logger.info("Number of tokens identified BEFORE Freeling processing: " + tokenAnnotationsBefore.size());

		flProc.parseDocumentGTSentences(docList.get(0));

		AnnotationSet sentenceAnnotations = docList.get(0).getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType);
		AnnotationSet tokenAnnotations = docList.get(0).getGATEdoc().getAnnotations(TDDocument.mainAnnSet).get(TDDocument.tokenAnnType);

		logger.info("Number of sentences identified after Freeling processing: " + sentenceAnnotations.size());
		logger.info("Number of tokens identified after Freeling processing: " + tokenAnnotations.size());

		// Store GATE document
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/francesco/Desktop/NLP_HACHATHON_4YFN/EXAMPLE_TEXTS/OUTPUT_TEST_GATE.xml"), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			out.write(docList.get(0).getGATEdoc().toXml());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

}
