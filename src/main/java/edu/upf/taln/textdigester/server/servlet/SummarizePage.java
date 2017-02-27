/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.CoreExample;
import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.freeling.FlProcessor;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.MapUtil;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.summarizer.ConfigurableSummarizer;
import edu.upf.taln.textdigester.summarizer.SummarizationMethodENUM;
import edu.upf.taln.textdigester.summarizer.util.SummaryUtil;
import gate.Annotation;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class SummarizePage extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(SummarizePage.class);

	private static VelocityEngine ve = new VelocityEngine();
	private static ToolManager velocityToolManager = new ToolManager();

	public void init() throws ServletException {
		// Velocity engine initialization
		try {

			File outputDirVelocity = new File("/home/francesco/Desktop/NLP_HACHATHON_4YFN/VELOCITY", "velocity.log");
			URL toolboxConfigURL = outputDirVelocity.getClass().getResource("/velocity/velocity-tools.xml");
			File toolboxConfigFile = new File(toolboxConfigURL.toURI());
			velocityToolManager.configure(toolboxConfigFile.getAbsolutePath());
			URL baseModelStream = outputDirVelocity.getClass().getResource("/velocity");
			ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, baseModelStream.getPath());
			ve.setProperty(RuntimeConstants.RUNTIME_LOG, outputDirVelocity.getPath() + "");

			// Escape HTML entities in template variables
			ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference");
			ve.setProperty("eventhandler.escape.html.match", "/.*/");

			ve.init();

		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Global file processing error - " + e.getMessage());
		}

		/* Load property file */
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties");

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		Template t_cont = ve.getTemplate("summarizePage.vm");
		VelocityContext context_cont = new VelocityContext(velocityToolManager.createContext());

		String summary = "Please, specify the parameter and the text to summarize...";

		request.setCharacterEncoding("UTF-8");

		String docText = request.getParameter("docText");
		String methodS = request.getParameter("methodS");
		String langS = request.getParameter("langS");
		String lengthS = request.getParameter("lengthS");

		if(docText != null && methodS != null && langS != null && lengthS != null) {
			context_cont.put("docText", request.getParameter("docText"));
			context_cont.put("methodS", request.getParameter("methodS"));
			context_cont.put("langS", request.getParameter("langS"));
			context_cont.put("lengthS", request.getParameter("lengthS"));

			summary = "ERROR WHILE GENERATING SUMMARY";
			if(docText != null && !docText.equals("")) {

				Map<Annotation, Double> orderedSentences = new HashMap<Annotation, Double>();

				TDDocument docToSummarize = null;
				try {
					LangENUM lang = LangENUM.valueOf(langS);
					SummarizationMethodENUM method = SummarizationMethodENUM.valueOf(methodS);

					docToSummarize = FlProcessor.generateDocumentFromFreeText(docText, null, lang);

					switch(method) {
					case LexRank_TFIDF:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.LexRank_TFIDF);
						break;
					case LexRank_EMBED:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.LexRank_EMBED);
						break;
					case Centroid_TFIDF:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Centroid_TFIDF);
						break;
					case Centroid_EMBED:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Centroid_EMBED);
						break;
					case FirstSim:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.FirstSim);
						break;
					case SemScore:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.SemScore);
						break;
					case TFscore:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.TFscore);
						break;
					case Position:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Position);
						break;
					case Centroid_TFIDF_SUMMA:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Centroid_TFIDF_SUMMA);
						break;
					default:
						orderedSentences = null;
					}

				} catch(Exception e) {
					e.printStackTrace();
					logger.warn("Error while summarizing - " + e.getMessage());
					summary = "Error while summarizing - " + e.getMessage();
				}

				if(docToSummarize != null && orderedSentences != null && orderedSentences.size() > 0) {
					try {
						Double lengthSdouble = Double.valueOf(lengthS);
						// Print the text of one of these summaries
						Map<Annotation, Double> orderedSentences_SemScore_topPerc = SummaryUtil.getSummary(orderedSentences, docToSummarize, lengthSdouble);
						summary = SummaryUtil.getStringSummaryText(orderedSentences_SemScore_topPerc, docToSummarize);

						Map<String, Double> orderedSentencesString = new HashMap<String, Double>();
						for(Entry<Annotation, Double> orderedSentencesEntry : orderedSentences.entrySet()) {
							try {
								orderedSentencesString.put(GtUtils.getTextOfAnnotation(orderedSentencesEntry.getKey(), docToSummarize.getGATEdoc()), orderedSentencesEntry.getValue());
							} catch(Exception e) {
								/* Do nothing */
							}
						}
						
						orderedSentencesString = MapUtil.sortByValue(orderedSentencesString);
						
						context_cont.put("orderedSentencesString", orderedSentencesString);
						context_cont.put("summaryGenerated", "true");
					} catch(Exception e) {
						e.printStackTrace();
						logger.warn("Error while selecting sentences - " + e.getMessage());
						summary = "Error while selecting sentences - " + e.getMessage();
					}
				}
			}
		}

		context_cont.put("summary", summary);

		StringWriter writer_cont = new StringWriter();
		t_cont.merge(context_cont, writer_cont);

		// Set response content type
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		out.println(writer_cont);
	}


	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		Template t_cont = ve.getTemplate("summarizePage.vm");
		VelocityContext context_cont = new VelocityContext(velocityToolManager.createContext());

		String summary = "Please, specify the parameter and the text to summarize...";

		request.setCharacterEncoding("UTF-8");

		String docText = request.getParameter("docText");
		String methodS = request.getParameter("methodS");
		String langS = request.getParameter("langS");
		String lengthS = request.getParameter("lengthS");

		if(docText != null && methodS != null && langS != null && lengthS != null) {
			context_cont.put("docText", request.getParameter("docText"));
			context_cont.put("methodS", request.getParameter("methodS"));
			context_cont.put("langS", request.getParameter("langS"));
			context_cont.put("lengthS", request.getParameter("lengthS"));

			summary = "ERROR WHILE GENERATING SUMMARY";
			if(docText != null && !docText.equals("")) {

				Map<Annotation, Double> orderedSentences = new HashMap<Annotation, Double>();

				TDDocument docToSummarize = null;
				try {
					LangENUM lang = LangENUM.valueOf(langS);
					SummarizationMethodENUM method = SummarizationMethodENUM.valueOf(methodS);

					docToSummarize = FlProcessor.generateDocumentFromFreeText(docText, null, lang);

					switch(method) {
					case LexRank_TFIDF:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.LexRank_TFIDF);
						break;
					case LexRank_EMBED:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.LexRank_EMBED);
						break;
					case Centroid_TFIDF:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Centroid_TFIDF);
						break;
					case Centroid_EMBED:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Centroid_EMBED);
						break;
					case FirstSim:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.FirstSim);
						break;
					case SemScore:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.SemScore);
						break;
					case TFscore:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.TFscore);
						break;
					case Position:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Position);
						break;
					case Centroid_TFIDF_SUMMA:
						orderedSentences = ConfigurableSummarizer.summarize(docToSummarize, lang, SummarizationMethodENUM.Centroid_TFIDF_SUMMA);
						break;
					default:
						orderedSentences = null;
					}

				} catch(Exception e) {
					e.printStackTrace();
					logger.warn("Error while summarizing - " + e.getMessage());
					summary = "Error while summarizing - " + e.getMessage();
				}

				if(docToSummarize != null && orderedSentences != null && orderedSentences.size() > 0) {
					try {
						Double lengthSdouble = Double.valueOf(lengthS);
						// Print the text of one of these summaries
						Map<Annotation, Double> orderedSentences_SemScore_topPerc = SummaryUtil.getSummary(orderedSentences, docToSummarize, lengthSdouble);
						summary = SummaryUtil.getStringSummaryText(orderedSentences_SemScore_topPerc, docToSummarize);

						Map<String, Double> orderedSentencesString = new HashMap<String, Double>();
						for(Entry<Annotation, Double> orderedSentencesEntry : orderedSentences.entrySet()) {
							try {
								orderedSentencesString.put(GtUtils.getTextOfAnnotation(orderedSentencesEntry.getKey(), docToSummarize.getGATEdoc()), orderedSentencesEntry.getValue());
							} catch(Exception e) {
								/* Do nothing */
							}
						}
						
						orderedSentencesString = MapUtil.sortByValue(orderedSentencesString);
						
						context_cont.put("orderedSentencesString", orderedSentencesString);
						context_cont.put("summaryGenerated", "true");
					} catch(Exception e) {
						e.printStackTrace();
						logger.warn("Error while selecting sentences - " + e.getMessage());
						summary = "Error while selecting sentences - " + e.getMessage();
					}
				}
			}
		}

		context_cont.put("summary", summary);

		StringWriter writer_cont = new StringWriter();
		t_cont.merge(context_cont, writer_cont);

		// Set response content type
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		out.println(writer_cont);
	}

	public void destroy()
	{
		// do nothing.
	}
}
