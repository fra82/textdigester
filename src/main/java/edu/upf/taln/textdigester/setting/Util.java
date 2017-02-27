/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.setting;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class Util {

	/**
	 * Compare two string - true if both not null and equal
	 * @return
	 */
	public static boolean strCompare(String str1, String str2) {
		return (str1 != null && str2 != null && str1.equals(str2));
	}

	/**
	 * Compare two string case insensitive - true if both not null and equal
	 * @return
	 */
	public static boolean strCompareCI(String str1, String str2) {
		return (str1 != null && str2 != null && str1.equalsIgnoreCase(str2));
	}

	/**
	 * Compare two string case insensitive - true if both not null and equal
	 * @return
	 */
	public static boolean strCompareTrimmed(String str1, String str2) {
		return (str1 != null && str2 != null && str1.trim().equals(str2.trim()));
	}

	/**
	 * Compare two string case insensitive - true if both not null and equal
	 * @return
	 */
	public static boolean strCompareTrimmedCI(String str1, String str2) {
		return (str1 != null && str2 != null && str1.trim().equalsIgnoreCase(str2.trim()));
	}

	/**
	 * Compare two Integers
	 * 
	 * @param int1
	 * @param int2
	 * @return
	 */
	public static boolean intCompare(Integer int1, Integer int2) {
		return (int1 != null && int2 != null && int1.intValue() == int2.intValue());
	}

	/**
	 * Notify exception
	 * 
	 * @param localMsg
	 * @param e
	 * @param l
	 */
	public static void notifyException(String localMsg, Exception e, Logger l){
		l.warn(localMsg + " (" + e.getClass().getName() + ")" + ((e.getMessage() != null) ? " - " + e.getMessage() : ""));

		try {
			if(l.isDebugEnabled()) {
				e.printStackTrace();
			}
			else {
				l.warn("   >>> Stack trace: " + ExceptionUtils.getStackTrace(e).replace("\n", " ^^ "));
			}
		}
		catch (Exception exc) {
			// DO NOTHING
		}
	}

	/**
	 * Sort a map by increasing value
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueInc( Map<K, V> map ) {
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();
		st.sorted(Comparator.comparing(e -> e.getValue())).forEach(e ->result.put(e.getKey(),e.getValue()));
		return result;
	}

	/**
	 * Sort a map by decreasing value
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDec( Map<K, V> map ) {
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();
		st.sorted(new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		}).forEach(e ->result.put(e.getKey(),e.getValue()));
		return result;
	}

	private static int minimum(int a, int b, int c) {                            
		return Math.min(Math.min(a, b), c);                                      
	}                                                                            

	/**
	 * Levenshtein Distance computation
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {      
		int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];        

		for (int i = 0; i <= lhs.length(); i++)                                 
			distance[i][0] = i;                                                  
		for (int j = 1; j <= rhs.length(); j++)                                 
			distance[0][j] = j;                                                  

		for (int i = 1; i <= lhs.length(); i++)                                 
			for (int j = 1; j <= rhs.length(); j++)                             
				distance[i][j] = minimum(                                        
						distance[i - 1][j] + 1,                                  
						distance[i][j - 1] + 1,                                  
						distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

		return distance[lhs.length()][rhs.length()];                           
	}
	
	
	public static Map<Object, Double> sortByValue(Map<Object, Double> map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
