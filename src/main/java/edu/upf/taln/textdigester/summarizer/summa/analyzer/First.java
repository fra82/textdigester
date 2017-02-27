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
public class First {
    
    public static summa.scorer.FirstSentenceSimilarity firstSim;
    
    public static void init() {
        firstSim=new summa.scorer.FirstSentenceSimilarity();
        firstSim.setAnnSetName("Analysis");
        firstSim.setSentAnn("Sentence");
        firstSim.setVecAnn("Vector_Norm");
        
        
        
    }
    
    public static void setDoc(Document d) {
        firstSim.setDocument(d);
    }
    
    public static void run() {
        firstSim.execute();
        
    }
    
}
