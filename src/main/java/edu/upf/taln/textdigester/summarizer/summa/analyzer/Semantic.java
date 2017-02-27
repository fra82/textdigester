/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upf.taln.textdigester.summarizer.summa.analyzer;

import gate.Document;
import java.util.ArrayList;

/**
 *
 * @author Horacio Saggion
 */
public class Semantic {
   public static summa.scorer.SemanticScorer semScorer;
    
    public static void init() {
        ArrayList<String> list=new ArrayList();
        list.add("NE");
        semScorer=new summa.scorer.SemanticScorer();
        semScorer.setAnnSetName("Analysis");
        semScorer.setSentAnn("Sentence");
        semScorer.setSemTypes(list);
        
        
        
        
    }
    
    public static void setDoc(Document d) {
        semScorer.setDocument(d);
    }
    
    public static void run() {
        
        semScorer.execute();
        
    }
}
