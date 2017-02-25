package edu.upf.taln.textdigester;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.importer.multing.MultilingImport;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.method.SentenceSimilarityENUM;
import edu.upf.taln.textdigester.summarizer.method.lexrank.LexRankSummarizer;
import gate.Annotation;

public class ParseMultiLingDocumentExample {

	private static final Logger logger = LoggerFactory.getLogger(ParseMultiLingDocumentExample.class);

	private static String lang = "ca";
	private static String docName = "0f6b5428d4c8c3aa826d057281717efa_body.txt";

	public static void main(String[] args) {
		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");
		
		String text = MultilingImport.readText("/home/francesco/Desktop/NLP_HACHATHON_4YFN/EXAMPLE_TEXTS/multilingMss2015Training/body/text/" + lang + "/" + docName);

		/* Process text document */
		LangENUM languageOfHTMLdoc = FlProcessor.getLanguage(text);
		FlProcessor flProc = null;
		try {
			flProc = new FlProcessor(languageOfHTMLdoc);
		}
		catch(Exception e) {
			e.printStackTrace();
			return;
		}

		TDDocument TDdoc = flProc.generateDocumentFromFreeText(text, null);

		// Store GATE document
		/*
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/francesco/Desktop/NLP_HACHATHON_4YFN/EXAMPLE_TEXTS/" + lang + "_" + docName.replace(".txt", "_GATE.xml")), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			out.write(TDdoc.getGATEdoc().toXml());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
		
		try {
			LexRankSummarizer lexRank = new LexRankSummarizer(languageOfHTMLdoc, SentenceSimilarityENUM.cosineTFIDF, false, 0.01);
			Map<Annotation, Double> sortedSentences = lexRank.sortSentences(TDdoc);
			
			List<Annotation> sentListOrderedByRelevance = new ArrayList<Annotation>();
			for(Entry<Annotation, Double> sentence : sortedSentences.entrySet()) {
				logger.info("Score: " + sentence.getValue() + " - '" + GtUtils.getTextOfAnnotation(sentence.getKey(), TDdoc.getGATEdoc()) + "'");
				sentListOrderedByRelevance.add(sentence.getKey());
			}
			
			logger.info("Summary max 100 tokens: ");
			List<Annotation> summarySentences = GtUtils.orderAnnotations(sentListOrderedByRelevance, TDdoc.getGATEdoc(), 100);
			for(Annotation ann : summarySentences) {
				logger.info(GtUtils.getTextOfAnnotation(ann, TDdoc.getGATEdoc()));
			}
		} catch (TextDigesterException e) {
			e.printStackTrace();
		}
	}

}
