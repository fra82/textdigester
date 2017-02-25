package edu.upf.taln.textdigester.resource.lex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.setting.exception.TextDigesterException;

public class StopWordList {

	private static final Logger logger = LoggerFactory.getLogger(StopWordList.class);

	private static Map<LangENUM, Set<String>> stopwordLists = new HashMap<LangENUM, Set<String>>();

	private static void initStopWordList(LangENUM lang) throws TextDigesterException {

		if(lang != null && !stopwordLists.containsKey(lang)) {
			String resourcePath = PropertyManager.getProperty("textdigester.resource");
			resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;

			String stopwordsFileName = resourcePath + "stopwords" + File.separator;

			switch(lang) {
			case English:
				stopwordsFileName += "stopwords_EN.list";
				break;
			case Spanish:
				stopwordsFileName += "stopwords_ES.list";
				break;
			case Catalan:
				stopwordsFileName += "stopwords_CA.list";
				break;
			default:
				stopwordsFileName += "stopwords_EN.list";
			}

			File stopwordsFile = new File(stopwordsFileName);

			if(stopwordsFile != null && stopwordsFile.exists() && stopwordsFile.isFile()) {
				// Load stopwords
				Set<String> stopwordsLst = new HashSet<String>();
				stopwordLists.put(lang, stopwordsLst);

				try(BufferedReader br = new BufferedReader(new FileReader(stopwordsFile))) {
					for(String line; (line = br.readLine()) != null; ) {
						line = line.trim();
						if(line.length() > 0) {
							stopwordsLst.add(line);
						}
					}
				} catch (IOException e) {
					throw new TextDigesterException("Impossible to read stopword list for " + lang + " from file: '" +
							((stopwordsFileName != null) ? stopwordsFileName : "NULL")+ "' - " + e.getMessage());
				}

				logger.info("Loaded " + lang + " stop words: " + stopwordsLst.size() + " words.");

			}
			else {
				throw new TextDigesterException("Impossible to read stopword list for " + lang + " from file: '" +
						((stopwordsFileName != null) ? stopwordsFileName : "NULL")+ "'");
			}
		}
		else {
			throw new TextDigesterException("Specify a language to load a stopword list.");
		}

	}


	public static boolean getStopwords(LangENUM lang, String word) {

		if(lang != null && stopwordLists.get(lang) == null) {
			try {
				initStopWordList(lang);
			} catch (TextDigesterException e) {
				e.printStackTrace();
				logger.error("ERROR: " + e.getMessage());
			}
		}

		if(lang != null && word != null && !word.equals("") && stopwordLists.get(lang) != null) {
			return stopwordLists.get(lang).contains(word.trim().toLowerCase());
		}

		return false;
	}

	public static Set<String> getStopwordList(LangENUM lang) {

		if(lang != null && stopwordLists.get(lang) == null) {
			try {
				initStopWordList(lang);
			} catch (TextDigesterException e) {
				e.printStackTrace();
				logger.error("ERROR: " + e.getMessage());
			}
		}

		if(lang != null && stopwordLists.get(lang) != null) {
			return Collections.unmodifiableSet(stopwordLists.get(lang));
		}

		return new HashSet<String>();
	}

}
