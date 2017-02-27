/**
 * TextDigester: Document Summarization Framework
 */

package edu.upf.taln.textdigester.example;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.importer.HTMLimporter;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.ConfigurableSummarizer;
import edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM;
import edu.upf.taln.textdigester.summarizer.summa.CallSUMMA;
import gate.Annotation;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class SummarizeDocumentExample {

	private static final Logger logger = LoggerFactory.getLogger(SummarizeDocumentExample.class);

	public static void main(String[] args) {
		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");

		/* Extract main-text from HTML page and parse it */
		TDDocument HTMLdoc = null;
		try {
			HTMLdoc = HTMLimporter.extractText(new URL("http://www.ara.cat/economia/clients-seran-proper-negoci-Telefonica_0_1749425231.html"));
			logger.debug("TEXT: " + HTMLdoc.getOriginalText());

		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Process text document */
		LangENUM languageOfHTMLdoc = FlProcessor.getLanguage(HTMLdoc.getOriginalText());

		HTMLdoc = FlProcessor.parseDocumentGTSentences(HTMLdoc, languageOfHTMLdoc);

		CallSUMMA.analyze(HTMLdoc.getGATEdoc(), languageOfHTMLdoc);

		try {
			Map<Annotation, Double> sortedSentences = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Centroid_EMBED);

			List<Annotation> sentListOrderedByRelevance = new ArrayList<Annotation>();
			for(Entry<Annotation, Double> sentence : sortedSentences.entrySet()) {
				logger.info("Score: " + sentence.getValue() + " - '" + GtUtils.getTextOfAnnotation(sentence.getKey(), HTMLdoc.getGATEdoc()) + "'");
				sentListOrderedByRelevance.add(sentence.getKey());
			}

			logger.info("Summary max 100 tokens: ");
			List<Annotation> summarySentences = GtUtils.orderAnnotationList(sentListOrderedByRelevance, HTMLdoc.getGATEdoc(), 100);
			for(Annotation ann : summarySentences) {
				logger.info(GtUtils.getTextOfAnnotation(ann, HTMLdoc.getGATEdoc()));
			}
		} catch (TextDigesterException e) {
			e.printStackTrace();
		}


		// Store GATE document
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/francesco/Desktop/NLP_HACHATHON_4YFN/EXAMPLE_TEXTS/NEWAPPO.xml"), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			out.write(HTMLdoc.getGATEdoc().toXml());
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
