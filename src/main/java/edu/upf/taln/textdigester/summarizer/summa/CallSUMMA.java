package edu.upf.taln.textdigester.summarizer.summa;


import static edu.upf.taln.textdigester.summarizer.summa.analyzer.SUMMAAnalyser.AnalyseCaDocument;
import static edu.upf.taln.textdigester.summarizer.summa.analyzer.SUMMAAnalyser.AnalyseEnDocument;
import static edu.upf.taln.textdigester.summarizer.summa.analyzer.SUMMAAnalyser.AnalyseEsDocument;
import static edu.upf.taln.textdigester.summarizer.summa.analyzer.SUMMAAnalyser.createSummaResources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.upf.taln.textdigester.resource.gate.GtUtils;
import edu.upf.taln.textdigester.setting.LangENUM;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.CleanSUMMADocument;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.DocSim;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.First;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.Frequency;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.Position;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.SUMMAAnalyser;
import edu.upf.taln.textdigester.summarizer.summa.analyzer.Semantic;
import edu.upf.taln.textdigester.summarizer.util.CreateSUMMAidfs;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.util.GateException;

/**
 * Class the invoke the summarization methods implemented by means do the SUMMA library http://www.taln.upf.edu/pages/summa.upf/.
 * 
 * @author Horacio Saggion
 */
public class CallSUMMA {

	private static boolean summaInitialized = false;

	public static void analyze(Document doc, LangENUM lang) {

		if(lang == null) {
			return;
		}

		if(!summaInitialized) {
			GtUtils.initGate();
			createSummaResources();
			CreateSUMMAidfs.createIgnoreTags();
			summaInitialized = true;
		}

		try {
			switch(lang) {
			case English:
				CleanSUMMADocument.clean(doc);

				AnalyseEnDocument(doc);

				First.init();
				First.setDoc(doc);
				First.run();
				Frequency.init();
				Frequency.setDoc(doc);
				Frequency.run();
				Position.init();
				Position.setDoc(doc);
				Position.run();
				Semantic.init();
				Semantic.setDoc(doc);
				Semantic.run();
				DocSim.init();
				DocSim.setDoc(doc);
				DocSim.run();

				CleanSUMMADocument.clean(doc);

				break;
			case Spanish:
				CleanSUMMADocument.clean(doc);

				AnalyseEsDocument(doc);

				First.init();
				First.setDoc(doc);
				First.run();
				Frequency.init();
				Frequency.setDoc(doc);
				Frequency.run();
				Position.init();
				Position.setDoc(doc);
				Position.run();
				Semantic.init();
				Semantic.setDoc(doc);
				Semantic.run();
				DocSim.init();
				DocSim.setDoc(doc);
				DocSim.run();

				CleanSUMMADocument.clean(doc);

				break;
			case Catalan:
				CleanSUMMADocument.clean(doc);

				AnalyseCaDocument(doc);

				First.init();
				First.setDoc(doc);
				First.run();
				Frequency.init();
				Frequency.setDoc(doc);
				Frequency.run();
				Position.init();
				Position.setDoc(doc);
				Position.run();
				Semantic.init();
				Semantic.setDoc(doc);
				Semantic.run();
				DocSim.init();
				DocSim.setDoc(doc);
				DocSim.run();

				CleanSUMMADocument.clean(doc);

				break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {

		try {
			Gate.init();
			Document esDoc;
			Document enDoc;
			Document caDoc;
			// Spanish
			esDoc=Factory.newDocument(new 
					URL("file:///C:\\work\\programs\\hakathon\\data\\es_3df84362f3b3995141528748a68408bd_body_GATE.xml"), "UTF-8");

			// English
			enDoc=Factory.newDocument(new 
					URL("file:///C:\\work\\programs\\hakathon\\data\\en_2c02d6960625b1d510098f7857f90ab9_body_GATE.xml"), "UTF-8");


			// Catalan
			caDoc=Factory.newDocument(new 
					URL("file:///C:\\work\\programs\\hakathon\\data\\ca_1f80345fa12d9443e1e68feae1688be8_body_GATE.xml"), "UTF-8");


			createSummaResources();
			CreateSUMMAidfs.createIgnoreTags();

			// Spanish
			AnalyseEsDocument(esDoc);







			First.init();
			First.setDoc(esDoc);
			First.run();
			Frequency.init();
			Frequency.setDoc(esDoc);
			Frequency.run();
			Position.init();
			Position.setDoc(esDoc);
			Position.run();
			Semantic.init();
			Semantic.setDoc(esDoc);
			Semantic.run();
			DocSim.init();
			DocSim.setDoc(esDoc);
			DocSim.run();

			System.out.println(">>>SPANISH<<<");

			CleanSUMMADocument.clean(esDoc);
			System.out.println(esDoc.getAnnotations("Analysis").get("Sentence"));
			System.out.println(esDoc.getAnnotations("Analysis").get("Vector"));
			// English
			AnalyseEnDocument(enDoc);







			First.init();
			First.setDoc(enDoc);
			First.run();
			Frequency.init();
			Frequency.setDoc(enDoc);
			Frequency.run();
			Position.init();
			Position.setDoc(enDoc);
			Position.run();
			Semantic.init();
			Semantic.setDoc(enDoc);
			Semantic.run();
			DocSim.init();
			DocSim.setDoc(enDoc);
			DocSim.run();

			System.out.println(">>>ENGLISH<<<");
			CleanSUMMADocument.clean(enDoc);
			System.out.println(enDoc.getAnnotations("Analysis").get("Sentence"));
			System.out.println(enDoc.getAnnotations("Analysis").get("Vector"));
			// Catalan
			AnalyseCaDocument(caDoc);







			First.init();
			First.setDoc(caDoc);
			First.run();
			Frequency.init();
			Frequency.setDoc(caDoc);
			Frequency.run();
			Position.init();
			Position.setDoc(caDoc);
			Position.run();
			Semantic.init();
			Semantic.setDoc(caDoc);
			Semantic.run();
			DocSim.init();
			DocSim.setDoc(caDoc);
			DocSim.run();
			System.out.println(">>>CATALAN<<<");
			CleanSUMMADocument.clean(caDoc);
			System.out.println(caDoc.getAnnotations("Analysis").get("Sentence"));
			System.out.println(caDoc.getAnnotations("Analysis").get("Vector"));
		} catch(GateException ge) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ge);
		} catch (MalformedURLException ex) {
			Logger.getLogger(SUMMAAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CallSUMMA.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
