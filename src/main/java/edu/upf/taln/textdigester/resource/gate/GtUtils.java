/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.resource.gate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Strings;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.setting.MapUtil;
import edu.upf.taln.textdigester.setting.PropertyManager;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Gate;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class GtUtils {

	private static boolean isInitialized = false;

	/**
	 * Initialize GATE library
	 * 
	 * @return
	 */
	public static boolean initGate() {

		if(!isInitialized) {
			// Init GATE if not already initialized
			if(Gate.getGateHome() == null) {
				try {

					String gateHomePath = PropertyManager.getProperty("gate.home");
					gateHomePath = (gateHomePath.endsWith(File.separator)) ? gateHomePath : gateHomePath + File.separator;


					Gate.setGateHome(new File(PropertyManager.getProperty("gate.home")));
					Gate.setPluginsHome(new File(PropertyManager.getProperty("gate.plugins")));
					Gate.setSiteConfigFile(new File(gateHomePath + "gate_uc.xml"));
					Gate.setUserConfigFile(new File(gateHomePath + "gate_uc.xml"));

					Gate.init();

					isInitialized = true;
				}
				catch(Exception ge) {
					ge.printStackTrace();
				}
			}
		}

		return isInitialized;
	} 


	public static String getTextOfAnnotation(Annotation ann, Document doc) {
		String retText = "";

		if(ann != null && doc != null) {
			try {
				retText = doc.getContent().getContent(ann.getStartNode().getOffset(), ann.getEndNode().getOffset()).toString();
			}
			catch(Exception e) {

			}
		}

		return retText;
	}

	public static List<Annotation> orderAnnotationList(List<Annotation> annList, Document doc, Integer numTokenLimit) {
		List<Annotation> retAnnList = new ArrayList<Annotation>();

		if(annList != null && annList.size() > 0 && doc != null) {
			int totalTokens = 0;
			Map<Long, Annotation> startOffserAnnMap = new HashMap<Long, Annotation>();
			try {
				Set<String> swAppo = new HashSet<String>();

				for(Annotation ann : annList) {
					if(ann != null) {
						List<String> annotation_terms = TDDocument.extractTokenList(ann, new TDDocument("", doc, null), null, true, true, true, false, false, swAppo);
						totalTokens += annotation_terms.size();
						if(numTokenLimit != null && numTokenLimit > 0 && totalTokens >= numTokenLimit) {
							break;
						}

						startOffserAnnMap.put(ann.getStartNode().getOffset(), ann);
					}
				}

				Map<Long, Annotation> startOffserAnnMapOrdered = new TreeMap<Long, Annotation>(startOffserAnnMap);

				for(Entry<Long, Annotation> annOrderedEntry : startOffserAnnMapOrdered.entrySet()) {
					retAnnList.add(annOrderedEntry.getValue());
				}
			}
			catch(Exception e) {

			}
		}

		return retAnnList;
	}
	
	public static Document removeAnnotationOfType(Document doc, String annSet, String annType) {
		if(doc != null && !Strings.isNullOrEmpty(annSet) && !Strings.isNullOrEmpty(annType)) {
			List<Annotation> annToDelList = new ArrayList<Annotation>();

			AnnotationSet annSetToRefine = doc.getAnnotations(annSet);
			if(annSetToRefine != null && annSetToRefine.size() > 0) {
				Iterator<Annotation> annSetToRefineIter = annSetToRefine.iterator();

				while(annSetToRefineIter.hasNext()) {
					Annotation annLocal = annSetToRefineIter.next();
					if(annLocal != null && annLocal.getType().equals(annType)) {
						annToDelList.add(annLocal);
					}
				}
			}

			for(Annotation annToDel : annToDelList) {
				doc.getAnnotations(annSet).remove(annToDel);
			}
		}

		return doc;
	}

	public static Map<Annotation, Double> orderSentencesBySentFeatValue(Document doc, String featName) {
		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

		List<Annotation> sentences = gate.Utils.inDocumentOrder(doc.getAnnotations(TDDocument.mainAnnSet).get(TDDocument.sentenceAnnType));
		for(Annotation sent : sentences) {
			try {
				if(sent != null && sent.getFeatures() != null && sent.getFeatures().containsKey(featName) &&
						sent.getFeatures().get(featName) != null && ((String) sent.getFeatures().get(featName)) != null) {
					String featVal = (String) sent.getFeatures().get(featName);
					Double featValNum = Double.valueOf(featVal.trim());
					retMap.put(sent, featValNum);
				}
			} catch(Exception e) {

			}
		}

		return MapUtil.sortByValue(retMap);
	}



}
