/**
 * TextDigester: Document Summarization Framework
 */
package edu.upf.taln.textdigester.importer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.upf.taln.textdigester.model.TDDocument;
import edu.upf.taln.textdigester.resource.gate.GtUtils;
import gate.Factory;

/**
 * 
 * @author Francesco Ronzano
 *
 */
public class TDXMLimporter {

	private static final Logger logger = LoggerFactory.getLogger(HTMLimporter.class);

	private static Random rnd = new Random();

	public static List<TDDocument> extractDocuments(String TDXMLpath) {

		List<TDDocument> retList = new ArrayList<TDDocument>();

		if(TDXMLpath == null || TDXMLpath.length() == 0) {
			return retList;
		}

		File TDXMLfile = new File(TDXMLpath);

		if(TDXMLfile == null || !TDXMLfile.exists() || !TDXMLfile.isFile()) {
			return retList;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(TDXMLfile);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("doc");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String docName = eElement.getAttribute("name");
					String docContent = eElement.getTextContent();
					String docContentXML = node2String(nNode);

					try{
						File dirOrInputTDXMLfile = TDXMLfile.getParentFile();

						//create a temp file
						File appoFile = new File(dirOrInputTDXMLfile + File.separator + "toDel_" + rnd.nextInt() + ".xml");

						//write it
						BufferedWriter bw = new BufferedWriter(new FileWriter(appoFile));
						bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>" + "\n<root>\n" + docContentXML + "\n</root>");
						bw.close();
						
						GtUtils.initGate();
						
						URL documentUrl = appoFile.toURI().toURL();
						gate.Document gateDoc = Factory.newDocument(documentUrl);
						
						appoFile.delete();
						
						retList.add(new TDDocument(docContent, gateDoc, docName));

					} catch(IOException e){
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retList;
	}

	static String node2String(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		// you may prefer to use single instances of Transformer, and
		// StringWriter rather than create each time. That would be up to your
		// judgement and whether your app is single threaded etc
		StreamResult xmlOutput = new StreamResult(new StringWriter());
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), xmlOutput);
		return xmlOutput.getWriter().toString();
	}
}
