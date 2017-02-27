package edu.upf.taln.textdigester;

import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.importer.HTMLimporter;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.ConfigurableSummarizer;
import edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM;
import edu.upf.taln.textdigester.summarizer.util.SummaryUtil;
import gate.Annotation;

public class CoreExample {
	
	private static final Logger logger = LoggerFactory.getLogger(CoreExample.class);
	
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

		/* Process text document by identifying its language and then parsing its contents by Freeling */
		LangENUM languageOfHTMLdoc = FlProcessor.getLanguage(HTMLdoc.getOriginalText());

		HTMLdoc = FlProcessor.parseDocumentGTSentences(HTMLdoc, languageOfHTMLdoc);
		
		String myText = "Here you can load the text to summarize";
		LangENUM lang = LangENUM.Catalan; // Supported languages: Catalan, English, Spanish
		TDDocument myDoc = FlProcessor.generateDocumentFromFreeText(myText, "Name_of_the_docment_or_null_to_autogenerate", lang);
		
				
		/* Try different summarization methods that return a map with key a sentence Annotation instance and value the relevance score assigned to that sentence
		 * List of summarization methods available - in the class: edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM */
		
		try {
			// Summarization method: Centroid_TFIDF
			Map<Annotation, Double> orderedSentences_Centroid_TFIDF = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Centroid_TFIDF);
			
			// Summarization method: Centroid_EMBED
			Map<Annotation, Double> orderedSentences_Centroid_EMBED = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Centroid_EMBED);
			
			// Summarization method: TextRank_TFIDF
			Map<Annotation, Double> orderedSentences_TextRank_TFIDF = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.TextRank_TFIDF);
			
			// Summarization method: TextRank_EMBED
			Map<Annotation, Double> orderedSentences_TextRank_EMBED = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.TextRank_EMBED);
			
			// Summarization method: FirstSim
			Map<Annotation, Double> orderedSentences_FirstSim = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.FirstSim);
			
			// Summarization method: NEScore
			Map<Annotation, Double> orderedSentences_NEScore = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.NEScore);
			
			// Summarization method: SemScore
			Map<Annotation, Double> orderedSentences_SemScore = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.SemScore);
			
			// Print the text of one of these summaries
			Map<Annotation, Double> orderedSentences_SemScore_top20perc = SummaryUtil.getSummary(orderedSentences_SemScore, HTMLdoc, 20d);
			System.out.println("SUMMARY: \n " + SummaryUtil.getStringSummaryText(orderedSentences_SemScore_top20perc, HTMLdoc));
			
			
			
			
		} catch (TextDigesterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
