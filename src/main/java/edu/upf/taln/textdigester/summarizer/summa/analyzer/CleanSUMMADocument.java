/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upf.taln.textdigester.summarizer.summa.analyzer;

import gate.AnnotationSet;
import gate.Document;

/**
 *
 * @author Horacio Saggion
 */
public class CleanSUMMADocument {
    
    // removes vectors from document
    public static void clean(Document doc) {
        
        AnnotationSet vectors=doc.getAnnotations("Analysis").get("Vector");
        AnnotationSet norms=doc.getAnnotations("Analysis").get("Vector_Norm");
        AnnotationSet dvectors=doc.getAnnotations("Analysis").get("DocVector");
        AnnotationSet dnorms=doc.getAnnotations("Analysis").get("DocVector_Norm");
        doc.getAnnotations("Analysis").removeAll(vectors);
        doc.getAnnotations("Analysis").removeAll(norms);
        doc.getAnnotations("Analysis").removeAll(dvectors);
        doc.getAnnotations("Analysis").removeAll(dnorms);
        
    }
    
}
