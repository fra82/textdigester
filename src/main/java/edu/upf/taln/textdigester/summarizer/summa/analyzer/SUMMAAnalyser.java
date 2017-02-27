/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upf.taln.textdigester.summarizer.summa.analyzer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.upf.taln.textdigester.setting.PropertyManager;
import edu.upf.taln.textdigester.summarizer.util.CreateSUMMAidfs;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 *
 * @author Horacio Saggion
 */
public class SUMMAAnalyser {

	static summa.resources.frequency.NEFrequency NEstatistics;
	static summa.resources.frequency.NEFrequency esNEstatistics;
	static summa.resources.frequency.NEFrequency enNEstatistics;
	static summa.resources.frequency.NEFrequency caNEstatistics;

	static summa.resources.frequency.InvertedTable enTable;
	static summa.resources.frequency.InvertedTable esTable;
	static summa.resources.frequency.InvertedTable caTable;

	public static ProcessingResource vectorPR;
	public static ProcessingResource enVectorPR;
	public static ProcessingResource esVectorPR;
	public static ProcessingResource caVectorPR;
	public static ProcessingResource normVecPR;
	public static ProcessingResource docVectorPR;
	public static ProcessingResource enDocVectorPR;
	public static ProcessingResource esDocVectorPR;
	public static ProcessingResource caDocVectorPR;
	public static ProcessingResource docNormVecPR;

	public static void main(String[] args) {

		try {
			Gate.init();
			/*
Gate.getCreoleRegister().registerDirectories(new 
        URL("file:///C:\\work\\programs\\GATE-8.0\\plugins\\summa_plugin"));
			 */
			Document esDoc;
			Document enDoc;
			Document caDoc;
			// Spanish
			esDoc=Factory.newDocument(new 
					URL("file:///C:\\work\\programs\\hackadata\\es_3df84362f3b3995141528748a68408bd_body_GATE.xml"), "UTF-8");

			// English
			enDoc=Factory.newDocument(new 
					URL("file:///C:\\work\\programs\\hackadata\\en_2c02d6960625b1d510098f7857f90ab9_body_GATE.xml"), "UTF-8");


			// Catalan
			caDoc=Factory.newDocument(new 
					URL("file:///C:\\work\\programs\\hackadata\\ca_1f80345fa12d9443e1e68feae1688be8_body_GATE.xml"), "UTF-8");


			createSummaResources();
			CreateSUMMAidfs.createIgnoreTags();

			AnalyseEnDocument(enDoc);

			System.out.println(enDoc.getAnnotations("Analysis").get("Vector_Norm"));
		} catch(GateException ge) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ge);
		} catch (MalformedURLException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public static void createSummaResources() {
		
		String resourcePath = PropertyManager.getProperty("textdigester.resource");
		resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;

		String baseSUMMApath = "file:///" + resourcePath + "summa" + File.separator + "resources" + File.separator;
		
		try {
			enTable=new summa.resources.frequency.InvertedTable();
			enTable.setParameterValue("encoding", "UTF-8");
			//  enTable.setParameterValue("tableLocation", 
			//         new URL("file:///C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\enwiki_lemma.idf"));
			enTable.setParameterValue("tableLocation", 
					new URL(baseSUMMApath + "aquaint.idf"));
			enTable.init();



			esTable=new summa.resources.frequency.InvertedTable();
			esTable.setParameterValue("encoding", "UTF-8");
			// esTable.setParameterValue("tableLocation", 
			//         new URL("file:///C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\eswiki_lemma.idf"));
			esTable.setParameterValue("tableLocation", 
					new URL(baseSUMMApath + "spanish_IDFs_utf8.lst"));
			esTable.init();


			caTable=new summa.resources.frequency.InvertedTable();
			caTable.setParameterValue("encoding", "UTF-8");
			//   caTable.setParameterValue("tableLocation", 
			//           new URL("file:///C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\cawiki_lemma.idf"));
			caTable.setParameterValue("tableLocation", 
					new URL(baseSUMMApath + "ancora_cat.idf"));
			caTable.init();


			NEstatistics=new summa.resources.frequency.NEFrequency();
			NEstatistics.init();
			NEstatistics.setParameterValue("annSet","Analysis");
			NEstatistics.setParameterValue("annType","Token");
			NEstatistics.setParameterValue("featureName","f_lemma");
			NEstatistics.setParameterValue("sentAnn","Sentence");
			NEstatistics.setParameterValue("parAnn","para");
			NEstatistics.setParameterValue("paraStat","para");
			NEstatistics.setParameterValue("sentStat","sent");
			NEstatistics.setParameterValue("tokenStat","token");
			NEstatistics.setParameterValue("kindF","kind");
			NEstatistics.setParameterValue("kindV","word");






			/* English */
			URL stopTableEn=new URL(baseSUMMApath + "stop_word.lst");
			URL stopKindEn=new URL(baseSUMMApath + "stop_kind_haka.lst");


			/*  Spanish */
			URL stopTableEs=new URL(baseSUMMApath + "stopwords_es.txt");
			URL stopKindEs=new URL(baseSUMMApath + "stop_kind_haka.lst");



			/* Catalan  */
			URL stopTableCa=new URL(baseSUMMApath + "stopwords_ca.lst");
			URL stopKindCa=new URL(baseSUMMApath + "stop_kind_haka.lst");


			// English
			enVectorPR=new summa.resources.frequency.VectorComputation();
			enVectorPR.setParameterValue("initVectors",Boolean.TRUE);
			enVectorPR.setParameterValue("vecAnn","Vector");
			enVectorPR.setParameterValue("tokenAnn", "Token");
			enVectorPR.setParameterValue("tokenFeature", "f_lemma");
			enVectorPR.setParameterValue("sentAnn","Sentence");
			enVectorPR.setParameterValue("statistics", "sent_tf_idf");
			enVectorPR.setParameterValue("encoding","UTF-8");
			enVectorPR.setParameterValue("stopTag","kind");
			enVectorPR.setParameterValue("stopFeature", "string");
			enVectorPR.setParameterValue("lowercase",Boolean.TRUE);
			enVectorPR.setParameterValue("stopTagLoc", stopKindEn);
			enVectorPR.setParameterValue("stopWordLoc", stopTableEn);
			enVectorPR.setParameterValue("annSetName","Analysis");
			enVectorPR.init();

			// Spanish
			esVectorPR=new summa.resources.frequency.VectorComputation();
			esVectorPR.setParameterValue("initVectors",Boolean.TRUE);
			esVectorPR.setParameterValue("vecAnn","Vector");
			esVectorPR.setParameterValue("tokenAnn", "Token");
			esVectorPR.setParameterValue("tokenFeature", "f_lemma");
			esVectorPR.setParameterValue("sentAnn","Sentence");
			esVectorPR.setParameterValue("statistics", "sent_tf_idf");
			esVectorPR.setParameterValue("encoding","UTF-8");
			esVectorPR.setParameterValue("stopTag","kind");
			esVectorPR.setParameterValue("stopFeature", "string");
			esVectorPR.setParameterValue("lowercase",Boolean.TRUE);
			esVectorPR.setParameterValue("stopTagLoc", stopKindEs);
			esVectorPR.setParameterValue("stopWordLoc", stopTableEs);
			esVectorPR.setParameterValue("annSetName","Analysis");
			esVectorPR.init();



			// Catalan
			caVectorPR=new summa.resources.frequency.VectorComputation();
			caVectorPR.setParameterValue("initVectors",Boolean.TRUE);
			caVectorPR.setParameterValue("vecAnn","Vector");
			caVectorPR.setParameterValue("tokenAnn", "Token");
			caVectorPR.setParameterValue("tokenFeature", "f_lemma");
			caVectorPR.setParameterValue("sentAnn","Sentence");
			caVectorPR.setParameterValue("statistics", "sent_tf_idf");
			caVectorPR.setParameterValue("encoding","UTF-8");
			caVectorPR.setParameterValue("stopTag","kind");
			caVectorPR.setParameterValue("stopFeature", "string");
			caVectorPR.setParameterValue("lowercase",Boolean.TRUE);
			caVectorPR.setParameterValue("stopTagLoc", stopKindCa);
			caVectorPR.setParameterValue("stopWordLoc", stopTableCa);
			caVectorPR.setParameterValue("annSetName","Analysis");
			caVectorPR.init();


			normVecPR=new summa.analyser.NormalizeVector();
			normVecPR.setParameterValue("annSet", "Analysis");
			normVecPR.setParameterValue("vecAnn", "Vector");
			normVecPR.init();

			// vector for document English

			enDocVectorPR=new summa.resources.frequency.VectorComputation();
			enDocVectorPR.setParameterValue("initVectors",Boolean.TRUE);
			enDocVectorPR.setParameterValue("vecAnn","DocVector");
			enDocVectorPR.setParameterValue("tokenAnn", "Token");
			enDocVectorPR.setParameterValue("tokenFeature", "f_lemma");
			enDocVectorPR.setParameterValue("sentAnn","Document");
			enDocVectorPR.setParameterValue("statistics", "token_tf_idf");
			enDocVectorPR.setParameterValue("encoding","UTF-8");
			enDocVectorPR.setParameterValue("stopTag","kind");
			enDocVectorPR.setParameterValue("stopFeature", "string");
			enDocVectorPR.setParameterValue("lowercase",Boolean.TRUE);
			enDocVectorPR.setParameterValue("stopTagLoc", stopKindEn);
			enDocVectorPR.setParameterValue("stopWordLoc", stopTableEn);
			enDocVectorPR.setParameterValue("annSetName","Analysis");
			enDocVectorPR.init();


			// vector for document Spanish

			esDocVectorPR=new summa.resources.frequency.VectorComputation();
			esDocVectorPR.setParameterValue("initVectors",Boolean.TRUE);
			esDocVectorPR.setParameterValue("vecAnn","DocVector");
			esDocVectorPR.setParameterValue("tokenAnn", "Token");
			esDocVectorPR.setParameterValue("tokenFeature", "f_lemma");
			esDocVectorPR.setParameterValue("sentAnn","Document");
			esDocVectorPR.setParameterValue("statistics", "token_tf_idf");
			esDocVectorPR.setParameterValue("encoding","UTF-8");
			esDocVectorPR.setParameterValue("stopTag","kind");
			esDocVectorPR.setParameterValue("stopFeature", "string");
			esDocVectorPR.setParameterValue("lowercase",Boolean.TRUE);
			esDocVectorPR.setParameterValue("stopTagLoc", stopKindEs);
			esDocVectorPR.setParameterValue("stopWordLoc", stopTableEs);
			esDocVectorPR.setParameterValue("annSetName","Analysis");
			esDocVectorPR.init();


			// vector for document Catalan

			caDocVectorPR=new summa.resources.frequency.VectorComputation();
			caDocVectorPR.setParameterValue("initVectors",Boolean.TRUE);
			caDocVectorPR.setParameterValue("vecAnn","DocVector");
			caDocVectorPR.setParameterValue("tokenAnn", "Token");
			caDocVectorPR.setParameterValue("tokenFeature", "f_lemma");
			caDocVectorPR.setParameterValue("sentAnn","Document");
			caDocVectorPR.setParameterValue("statistics", "token_tf_idf");
			caDocVectorPR.setParameterValue("encoding","UTF-8");
			caDocVectorPR.setParameterValue("stopTag","kind");
			caDocVectorPR.setParameterValue("stopFeature", "string");
			caDocVectorPR.setParameterValue("lowercase",Boolean.TRUE);
			caDocVectorPR.setParameterValue("stopTagLoc", stopKindCa);
			caDocVectorPR.setParameterValue("stopWordLoc", stopTableCa);
			caDocVectorPR.setParameterValue("annSetName","Analysis");
			caDocVectorPR.init();


			// norm vector for document
			docNormVecPR=new summa.analyser.NormalizeVector();
			docNormVecPR.setParameterValue("annSet", "Analysis");
			docNormVecPR.setParameterValue("vecAnn", "DocVector");
			docNormVecPR.init();



		} catch (ResourceInstantiationException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (MalformedURLException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	// analyse according to language
	public static void AnalyseEnDocument(Document doc) {

		try {
			// add statistics to the tokens
			// add kind to token
			// add NE to document
			AnnotationSet analysis=doc.getAnnotations("Analysis");
			AnnotationSet tokens=analysis.get("Token");
			Annotation token;
			FeatureMap fm;
			String tag;
			String first;
			Iterator<Annotation> ite=tokens.iterator();

			while(ite.hasNext()) {
				token=ite.next();
				fm=token.getFeatures();
				if(fm.containsKey("f_tag")) {
					tag=(String)fm.get("f_tag");
					first=tag.substring(0, 1);
					if(CreateSUMMAidfs.ignoreTagsEn.contains(first)) {
						fm.put("kind","no");
					} else {
						fm.put("kind","word");
					}

					if(tag.equals("W") || tag.equals("NP")) {
						analysis.add(token.getStartNode().getOffset(), 
								token.getEndNode().getOffset(), 
								"NE", 
								Factory.newFeatureMap());

					}
				} else {
					fm.put("kind","no");
				}

			}

			NEstatistics.setParameterValue("table",enTable);

			NEstatistics.setParameterValue("document",doc);
			NEstatistics.execute();
			enVectorPR.setParameterValue("document",doc);
			enVectorPR.execute();   

			normVecPR.setParameterValue("document",doc);
			normVecPR.execute();

			enDocVectorPR.setParameterValue("document",doc);
			enDocVectorPR.execute();   
			docNormVecPR.setParameterValue("document",doc);
			docNormVecPR.execute();




		} catch (ResourceInstantiationException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvalidOffsetException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}



	}


	public static void AnalyseEsDocument(Document doc) {

		try {
			// add statistics to the tokens
			// add kind to token
			// add NE to document
			AnnotationSet analysis=doc.getAnnotations("Analysis");
			AnnotationSet tokens=analysis.get("Token");
			Annotation token;
			FeatureMap fm;
			String tag;
			String first;
			Iterator<Annotation> ite=tokens.iterator();

			while(ite.hasNext()) {
				token=ite.next();
				fm=token.getFeatures();
				if(fm.containsKey("f_tag")) {
					tag=(String)fm.get("f_tag");
					first=tag.substring(0, 1);
					if(CreateSUMMAidfs.ignoreTagsEs.contains(first)) {
						fm.put("kind","no");
					} else {
						fm.put("kind","word");
					}

					if(tag.equals("W") || tag.startsWith("NP")) {
						analysis.add(token.getStartNode().getOffset(), 
								token.getEndNode().getOffset(), 
								"NE", 
								Factory.newFeatureMap());

					}
				} else {
					fm.put("kind","no");
				}

			}

			NEstatistics.setParameterValue("table",esTable);

			NEstatistics.setParameterValue("document",doc);
			NEstatistics.execute();
			esVectorPR.setParameterValue("document",doc);
			esVectorPR.execute();   

			normVecPR.setParameterValue("document",doc);
			normVecPR.execute();

			esDocVectorPR.setParameterValue("document",doc);
			esDocVectorPR.execute();   
			docNormVecPR.setParameterValue("document",doc);
			docNormVecPR.execute();




		} catch (ResourceInstantiationException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvalidOffsetException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}



	}

	public static void AnalyseCaDocument(Document doc) {

		try {
			// add statistics to the tokens
			// add kind to token
			// add NE to document
			AnnotationSet analysis=doc.getAnnotations("Analysis");
			AnnotationSet tokens=analysis.get("Token");
			Annotation token;
			FeatureMap fm;
			String tag;
			String first;
			Iterator<Annotation> ite=tokens.iterator();

			while(ite.hasNext()) {
				token=ite.next();
				fm=token.getFeatures();
				if(fm.containsKey("f_tag")) {
					tag=(String)fm.get("f_tag");
					first=tag.substring(0, 1);
					if(CreateSUMMAidfs.ignoreTagsCa.contains(first)) {
						fm.put("kind","no");
					} else {
						fm.put("kind","word");
					}

					if(tag.equals("W") || tag.startsWith("NP")) {
						analysis.add(token.getStartNode().getOffset(), 
								token.getEndNode().getOffset(), 
								"NE", 
								Factory.newFeatureMap());

					}
				} else {
					fm.put("kind","no");
				}

			}

			NEstatistics.setParameterValue("table",caTable);

			NEstatistics.setParameterValue("document",doc);
			NEstatistics.execute();
			caVectorPR.setParameterValue("document",doc);
			caVectorPR.execute();   

			normVecPR.setParameterValue("document",doc);
			normVecPR.execute();

			caDocVectorPR.setParameterValue("document",doc);
			caDocVectorPR.execute();   
			docNormVecPR.setParameterValue("document",doc);
			docNormVecPR.execute();




		} catch (ResourceInstantiationException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvalidOffsetException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}



	}
}
