/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upf.taln.textdigester.summarizer.summa.analyzer;

import gate.Document;

/**
 *
 * @author Horacio Saggion
 */
public class DocSim {
     public static summa.scorer.SentenceDocumentSimilarity docSim;
    
    public static void init() {
        docSim=new summa.scorer.SentenceDocumentSimilarity();
        docSim.setSentAnnSet("Analysis");
        docSim.setSentAnn("Sentence");
        docSim.setDocVecName("DocVector_Norm");
        docSim.setVecAnn("Vector_Norm");
        
        
        
    }
    
    public static void setDoc(Document d) {
        docSim.setDocument(d);
    }
    
    public static void run() {
        docSim.execute();
        
    }
    
}
