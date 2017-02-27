/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upf.taln.textdigester.summarizer.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

/**
 * Creates IDF tables from frequencies computed from freeling 
 * tagged texts.
 * Input table
 * lemma POS_TAG term_freq doc_freq
 * aggregates the lemma adding up doc_freq  for same lemma.
 * Ignores some POS tags 
 * English: PRP*,F*, W*, IN, UH, TO, RP, DT
 * Spanish/Catalan: C*,D*,P*,Z*,W*,I*,F*
 * Num docs will be the max aggregated doc_freq.
 * @author Horacio
 */
public class CreateSUMMAidfs {

	public static Set<String> ignoreTagsEn;
	public static Set<String> ignoreTagsEs;
	public static Set<String> ignoreTagsCa;


	public static void main(String[] args) {
		/*
        String inTblLoc="C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\eswiki_lemma_POS_TF_DF.dat";
        String outTblLoc="C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\eswiki_lemma.idf";
		 */
		createIgnoreTags();
		String inTblLoc="C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\enwiki_lemma_POS_TF_DF.dat";
		String outTblLoc="C:\\work\\programs\\textdigester-master\\TEXTDIGESTER_RESOURCES_v_0_0_1\\enwiki_lemma.idf";
		computeIDFSumma(inTblLoc,outTblLoc,ignoreTagsEn);
	}

	public static void createIgnoreTags() {

		ignoreTagsEn=new TreeSet();
		ignoreTagsEn.add("P");
		ignoreTagsEn.add("F");
		ignoreTagsEn.add("W");
		ignoreTagsEn.add("I");
		ignoreTagsEn.add("U");
		ignoreTagsEn.add("T");
		ignoreTagsEn.add("D");
		ignoreTagsEn.add("Z");


		ignoreTagsEs=new TreeSet();
		ignoreTagsEs.add("C");
		ignoreTagsEs.add("D");
		ignoreTagsEs.add("P");
		ignoreTagsEs.add("Z");
		ignoreTagsEs.add("W");
		ignoreTagsEs.add("I");
		ignoreTagsEs.add("F");



		ignoreTagsCa=new TreeSet();
		ignoreTagsCa.add("C");
		ignoreTagsCa.add("D");
		ignoreTagsCa.add("P");
		ignoreTagsCa.add("Z");
		ignoreTagsCa.add("W");
		ignoreTagsCa.add("I");
		ignoreTagsCa.add("F");

	}



	public static void computeIDFSumma(String inTBLLoc,String outTBLLoc,Set ignoreTags) {

		BufferedReader in=null;
		PrintWriter pw=null;
		Map<String,Long> idfTable=new TreeMap();
		StringTokenizer tokenizer;
		String line;
		String lemma;
		String POS;
		String freq;
		String dfreq;
		String first;

		long max=0;
		long idf;
		long dfreq_n;
		try {
			in=new BufferedReader(new FileReader(inTBLLoc));
			pw=new PrintWriter(new FileWriter(outTBLLoc));
			while((line=in.readLine())!=null) {
				tokenizer=new StringTokenizer(line," ");
				if(tokenizer.countTokens()==4) {

					lemma=tokenizer.nextToken();
					POS=tokenizer.nextToken();
					first=POS.substring(0, 1);
					freq=tokenizer.nextToken();
					dfreq=tokenizer.nextToken();
					dfreq_n=(new Long(dfreq)).longValue();
					if(!ignoreTags.contains(first) && !StringUtils.isNumeric(lemma) && !StringUtils.isNumeric(lemma.substring(0, 1))) {
						if(idfTable.containsKey(lemma)) {
							idf=((Long)idfTable.get(lemma)).longValue();
						} else {
							idf=0;
						}
						idf=idf+dfreq_n;
						if(idf>max) {
							max=idf;
						}
						idfTable.put(lemma,new Long(idf));
						// pw.println(lemma+"\t"+dfreq);
						// pw.flush();

					}
				}

			}
			Iterator<String> iteLemma=idfTable.keySet().iterator();
			pw.println(max);
			while(iteLemma.hasNext()) {
				lemma=iteLemma.next();
				pw.println(lemma+"\t"+idfTable.get(lemma));
				pw.flush();

			}

			pw.close();


		} catch (FileNotFoundException ex) {
			Logger.getLogger(CreateSUMMAidfs.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CreateSUMMAidfs.class.getName()).log(Level.SEVERE, null, ex);
		}


	}




}
