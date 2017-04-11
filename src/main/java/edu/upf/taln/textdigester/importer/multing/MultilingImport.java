/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.importer.multing;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Utility class for document import
 * 
 * @author Francesco Ronzano
 *
 */
public class MultilingImport {

	public static String readText(String path) {
		String outStr = "";
		
		try {
			File fileDir = new File(path);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fileDir), "UTF-8"));
			
			String str;

			while ((str = in.readLine()) != null) {
				if(!str.trim().startsWith("[")) {
					outStr += (outStr.length() == 0) ? str : "\n" + str;
				}
			}

			in.close();
		}
		catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return outStr;
	}
}
