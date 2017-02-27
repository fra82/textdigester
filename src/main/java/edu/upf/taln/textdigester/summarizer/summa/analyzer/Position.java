/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upf.taln.textdigester.summarizer.summa.analyzer;

import gate.Document;
import gate.creole.ExecutionException;

/**
 *
 * @author Horacio Saggion
 */
public class Position {
    public static summa.scorer.PositionScorer posScorer;
     
      public static void init() {
        posScorer=new summa.scorer.PositionScorer();
        posScorer.setAnnSetName("Analysis");
        posScorer.setSentAnn("Sentence");
        posScorer.setScoreName("position"); 
       
      
      
        
        
        
    }
    
    public static void setDoc(Document d) {
        posScorer.setDocument(d);
    }
    
    public static void run() throws ExecutionException {
        posScorer.execute();
        
    }
}
