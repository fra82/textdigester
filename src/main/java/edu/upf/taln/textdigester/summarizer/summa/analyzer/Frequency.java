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
public class Frequency {
     public static summa.scorer.SentenceTermFrequency tfScorer;
     
      public static void init() {
        tfScorer=new summa.scorer.SentenceTermFrequency();
        tfScorer.setAnnSetName("Analysis");
        tfScorer.setSentAnn("Sentence");
        tfScorer.setWordAnn("Token");
        tfScorer.setStatFeature("sent_tf_idf");
        tfScorer.setTermFreqFeature("tf_score");
      
        
        
        
    }
    
    public static void setDoc(Document d) {
        tfScorer.setDocument(d);
    }
    
    public static void run() throws ExecutionException {
        tfScorer.execute();
        
    }
}
