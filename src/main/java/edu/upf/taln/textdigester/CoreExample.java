/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester;

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
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;
import edu.upf.taln.textdigester.summarizer.ConfigurableSummarizer;
import edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM;
import edu.upf.taln.textdigester.summarizer.util.SummaryUtil;
import gate.Annotation;

/**
 * 
 * @author Francesco Ronzano
 *
 */
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

		/* Try different summarization methods that return a map with key a sentence Annotation instance and value the relevance score assigned to that sentence
		 * List of summarization methods available - in the class: edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM */
		
		try {

			// Summarization method: Centroid_TFIDF
			Map<Annotation, Double> orderedSentences_Centroid_TFIDF = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Centroid_TFIDF);

			// Summarization method: Centroid_EMBED
			Map<Annotation, Double> orderedSentences_Centroid_EMBED = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Centroid_EMBED);

			// Summarization method: TextRank_TFIDF
			Map<Annotation, Double> orderedSentences_TextRank_TFIDF = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.LexRank_TFIDF);

			// Summarization method: TextRank_EMBED
			Map<Annotation, Double> orderedSentences_TextRank_EMBED = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.LexRank_EMBED);

			// Summarization method: FirstSim
			Map<Annotation, Double> orderedSentences_FirstSim = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.FirstSim);

			// Summarization method: TFscore
			Map<Annotation, Double> orderedSentences_TFscore = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.TFscore);

			// Summarization method: Centroid_TFIDF_SUMMA
			Map<Annotation, Double> orderedSentences_Centroid_TFIDF_SUMMA = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Centroid_TFIDF_SUMMA);

			// Summarization method: Position
			Map<Annotation, Double> orderedSentences_Position = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.Position);

			// Summarization method: SemScore
			Map<Annotation, Double> orderedSentences_SemScore = ConfigurableSummarizer.summarize(HTMLdoc, languageOfHTMLdoc, SummarizationMethodENUM.SemScore);

			// Print the text of one of these summaries
			Map<Annotation, Double> orderedSentences_SemScore_top20perc = SummaryUtil.getSummary(orderedSentences_SemScore, HTMLdoc, 20d);
			System.out.println("SUMMARY: \n " + SummaryUtil.getStringSummaryText(orderedSentences_SemScore_top20perc, HTMLdoc));


		} catch (TextDigesterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");

		/* Extract main-text from HTML page and parse it */
		TDDocument HTMLdoc_1 = null;
		try {
			HTMLdoc_1 = HTMLimporter.extractText(new URL("http://www.ara.cat/cultura/llista-tots-nominats-als-Oscars_0_1750025056.html"));
			logger.debug("TEXT: " + HTMLdoc.getOriginalText());

		} catch (Exception e) {
			e.printStackTrace();
		}

		TDDocument HTMLdoc_2 = null;
		try {
			HTMLdoc_2 = HTMLimporter.extractText(new URL("http://www.ara.cat/cultura/moonlight-guanya-oscars_0_1750025053.html"));
			logger.debug("TEXT: " + HTMLdoc.getOriginalText());

		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Process text document by identifying its language and then parsing its contents by Freeling */
		LangENUM languageOfHTMLdoc_1 = FlProcessor.getLanguage(HTMLdoc_1.getOriginalText());
		LangENUM languageOfHTMLdoc_2 = FlProcessor.getLanguage(HTMLdoc_2.getOriginalText());

		HTMLdoc_1 = FlProcessor.parseDocumentGTSentences(HTMLdoc_1, languageOfHTMLdoc_1);
		HTMLdoc_2 = FlProcessor.parseDocumentGTSentences(HTMLdoc_2, languageOfHTMLdoc_2);


		/* Try different summarization methods that return a map with key a sentence Annotation instance and value the relevance score assigned to that sentence
		 * List of summarization methods available - in the class: edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM */

		List<TDDocument> docList = new ArrayList<TDDocument>();
		docList.add(HTMLdoc_1);
		docList.add(HTMLdoc_2);

		try {

			// Summarization method: CentroidMultiDoc_TFIDF
			Map<Entry<Annotation, TDDocument>, Double> orderedSentences_CentroidMultiDoc_TFIDF = ConfigurableSummarizer.summarizeMultiDoc(docList, languageOfHTMLdoc, SummarizationMethodENUM.CentroidMultiDoc_TFIDF);

			// Summarization method: CentoridMultiDoc_EMDBED
			Map<Entry<Annotation, TDDocument>, Double> orderedSentences_CentoridMultiDoc_EMDBED = ConfigurableSummarizer.summarizeMultiDoc(docList, languageOfHTMLdoc, SummarizationMethodENUM.CentoridMultiDoc_EMDBED);


			// Print the text of one of these summaries
			Map<Entry<Annotation, TDDocument>, Double> orderedSentences_SemScore_top20perc = SummaryUtil.getSummary(orderedSentences_CentoridMultiDoc_EMDBED, docList, 20d);
			System.out.println("SUMMARY: \n " + SummaryUtil.getStringSummaryText(orderedSentences_SemScore_top20perc));


		} catch (TextDigesterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
