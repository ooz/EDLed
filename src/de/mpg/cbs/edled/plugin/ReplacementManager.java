package de.mpg.cbs.edled.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * A ReplacementManager bundles all nodes that are built from the plugin's
 * model(s). Every node has its XPath and model keyword associated. The main
 * application uses the plugin's ReplacementManager to update the main
 * application model (replacing the nodes in the main model with the nodes from
 * the ReplacementManager).
 * 
 * @author Oliver Z.
 */
public class ReplacementManager {
	/** */
	private static final Logger logger = Logger.getLogger(ReplacementManager.class);
	
	private final Map<String, XPathExpression> xpathsOfNodesToReplace;
	private final Map<XPathExpression, Node> nodesToReplace;
	
	/**
	 * Constructs an empty manager that cannot be filled.
	 */
	public ReplacementManager() {
		this.xpathsOfNodesToReplace = new LinkedHashMap<String, XPathExpression>();
		this.nodesToReplace = new LinkedHashMap<XPathExpression, Node>();
	}
	
	/**
	 * 
	 * @param xpaths
	 */
	public ReplacementManager(final Map<String, XPathExpression> xpaths) {
		this.xpathsOfNodesToReplace = new LinkedHashMap<String, XPathExpression>();
		if (xpaths != null) {
			this.xpathsOfNodesToReplace.putAll(xpaths);
		}
		this.nodesToReplace = new LinkedHashMap<XPathExpression, Node>();
	}
	
	/**
	 * Creates a ReplacementManager from a map file.
	 * 
	 * @param mapFile File containing model keyword-XPath pairs. 
	 * @return        ReplacementManager initialized with the information from
	 * 				  map file.
	 */
	public static ReplacementManager createFrom(final File mapFile) {
		return new ReplacementManager(getMapFrom(mapFile));
	}
	private static Map<String, XPathExpression> getMapFrom(final File mapFile) {
		Map<String, XPathExpression> map = new LinkedHashMap<String, XPathExpression>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(mapFile));
			String line = read(reader);
			while (line != null) {
				String key = line.trim();
				String xpath = read(reader).trim();
				if (xpath != null) {
					XPath xpathEval = XPathFactory.newInstance().newXPath();
					try {
						XPathExpression xpathExpr = xpathEval.compile(xpath);
						if (xpathExpr != null) {
							map.put(key, xpathExpr);
						}
					} catch (XPathExpressionException e) {
						logger.warn("Could not evaluate XPath " + xpath + " for key " + key + "!");
					}
				}
				line = read(reader);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.warn("Could not find map file at " + mapFile.getPath(), e);
		} catch (IOException e) {
			logger.warn("I/O error related to " + mapFile.getPath(), e);
		}
		
		return map;
	}
	private static String read(final BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
			while (line != null) {
				if (line.startsWith("#") || line.trim().equals("")) {
					line = reader.readLine();
				} else {
					return line;
				}
			}
		} catch (IOException e) {
			logger.warn("I/O error!", e);
		}
		
		return line;
	}
	
	/**
	 * Puts a XPath-Node pair into the ReplacementManager.
	 * 
	 * @param xpath The XPathExpression to put (key).
	 * @param node  The associated node build from the plugin's model (value).
	 * @return      The node that was previously associated with the given
	 * 				xpath. Returns null if the ReplacementManager did not
	 * 				contain xpath, yet.
	 */
	public Node put(final XPathExpression xpath, final Node node) {
		if (this.xpathsOfNodesToReplace.values().contains(xpath)) {
			return this.nodesToReplace.put(xpath, node);
		}
		
		return null;
	}
	/**
	 * Puts a model keyword-Node pair into the ReplacementManager.
	 * 
	 * Convenience method for the following process: 
	 * Goal: putting a new node representing the updated plugin model into
	 *       the ReplacementManager.
     * Without this method: 1. Query XPath for the model keyword key
     * 						2. Put XPath and DOM Node into the ReplacementManager 
	 * 
	 * @param key  The model keyword.
	 * @param node DOM Node built from the plugin's model (attributed by the model keyword).
	 * @return     The DOM Node that was previously associated with the model
	 * 			   keyword key.
	 */
	public Node put(final String key, final Node node) {
		if (this.xpathsOfNodesToReplace.keySet().contains(key)) {
			return this.nodesToReplace.put(this.xpathsOfNodesToReplace.get(key), node);
		}
		
		return null;
	}
	
	public List<String> getKeys() {
		return new LinkedList<String>(this.xpathsOfNodesToReplace.keySet());
	}
	public List<XPathExpression> getXPaths() {
		return new LinkedList<XPathExpression>(this.xpathsOfNodesToReplace.values());
	}
	
	public XPathExpression xpathFor(final String key) {
		return this.xpathsOfNodesToReplace.get(key);
	}
	public Node nodeFor(final XPathExpression xpath) {
		return this.nodesToReplace.get(xpath);
	}
	public Node nodeFor(final String key) {
		if (this.xpathsOfNodesToReplace.keySet().contains(key)) {
			return this.nodesToReplace.get(this.xpathsOfNodesToReplace.get(key));
		}
		
		return null;
	}

}
