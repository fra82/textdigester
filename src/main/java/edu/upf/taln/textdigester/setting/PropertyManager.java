package edu.upf.taln.textdigester.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.textdigester.setting.exception.TextDigesterException;


public class PropertyManager {
	
	public static final String resourceFolder_fullPath = "resourceFolder.fullPath";
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyManager.class);
	
	private static String propertyPath;
	private static Properties holder = null; 
	
	
	public static String defaultPropertyFilePath = "/home/francesco/Desktop/NLP_HACHATHON_4YFN/TextDigesterConfig.properties";
	
	/**
	 * Load the property file.
	 * The path of the Text Digester property file is specified as a local absolute 
	 * (without trailing slash, for instance /home/mydir/TextDigesterConfig.properties)
	 * 
	 * @return
	 * @throws TextDigesterException 
	 * @throws InternalProcessingException
	 */
	public static boolean loadProperties() throws TextDigesterException {
		
		FileInputStream input;
		try {
			input = new FileInputStream(propertyPath);
		} catch (FileNotFoundException e) {
			throw new TextDigesterException("PROPERTY FILE INITIALIZATION ERROR: property file '" + propertyPath + "' cannot be found");
		}
		
		try {
			holder = new Properties();
			holder.load(input);
		} catch (IOException e) {
			throw new TextDigesterException("PROPERTY FILE INITIALIZATION ERROR: property file '" + propertyPath + "' cannot be read (" +  e.getMessage() + ")");
		}
		
		return false;
	}
	
	/**
	 * Set the Text Digester property file path
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean setPropertyFilePath(String filePath) {
		if(filePath != null) {
			File propFile = new File(filePath);
			if(propFile.exists() && propFile.isFile()) {
				propertyPath = filePath;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieve a property from the Text Digester property file.
	 * The path of the Text Digester property file is specified as a local absolute 
	 * (without trailing slash, for instance /home/mydir/TextDigesterConfig.properties)
	 * 
	 * @param propertyName
	 * @return
	 * @throws InternalProcessingException
	 */
	public static String getProperty(String propertyName) {
		if(StringUtils.isNotBlank(propertyName)) {
			if(holder == null) {
				try {
					loadProperties();
				} catch (TextDigesterException e) {
					Util.notifyException("Property file not correctly loaded", e, logger);
				}
			}
			
			return holder.getProperty(propertyName);
		}
		else {
			return null;
		}
	}
	
}
