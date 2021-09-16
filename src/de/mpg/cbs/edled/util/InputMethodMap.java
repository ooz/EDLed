package de.mpg.cbs.edled.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.metatree.NodeConstraint;
import de.mpg.cbs.edled.xml.XSDTypeName;


/**
 * Class that contains the specified the input methods for certain XML entries.
 * E.g. if a URL should be entered via a text field or a file chooser.
 *  
 * @author Oliver Z.
 */
public class InputMethodMap {
	
	/**
	 * Possible input methods. 
	 * 
	 * @author Oliver Z.
	 */
	public enum InputMethod {
		NOT_SPECIFIED,
		TEXTFIELD,
		FILECHOOSER,
		DIRCHOOSER,
		COLORCHOOSER;
		
		/**
		 * Save version of valueOf.
		 * If no conversation is possible, null is returned (valueOf
		 * would throw an exception)
		 * 
		 * @param str String that should be converted into an InputMethod
		 * 			  enum value.
		 * @return	  InputMethod enum value representing str. 
		 * 			  Null if no such representation exists.			
		 */
		public static InputMethod valueOfSave(final String str) {
			try {
				return InputMethod.valueOf(str);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	/** */
	private static final Logger logger = Logger.getLogger(InputMethodMap.class);
	
	/** 
	 * Contains all input methods that were assigned on a per node basis. 
	 * key:   XPath of a node.
	 * value: Input method for that node. 
	 */
	private final Map<XPathExpression, InputMethod> xpaths = new HashMap<XPathExpression, InputMethod>();
	/** 
	 * Contains all input methods that were assigned per XSD type.
	 * key:   XSD type in string representation.
	 * value: Input method for all nodes that use key as their type.
	 */
	private final Map<String, InputMethod> types = new HashMap<String, InputMethod>();
	
	/** Constructs an empty map. */
	public InputMethodMap() {
	}
	
	/**
	 * Convenience constructor for {@link InputMethodMap#InputMethodMap(File)}.
	 * 
	 * @param inputMethodMapFilePath The path to the map file to load.
	 */
	public InputMethodMap(final String inputMethodMapFilePath) {
		Map<String, String> map = FileUtility.readMapFile(inputMethodMapFilePath);
		splitMap(map);
	}
	
	/**
	 * Constructs an InputMethodMap from a map file.
	 * 
	 * @param inputMethodMapFile The map file to load.
	 */
	public InputMethodMap(final File inputMethodMapFile) {
		Map<String, String> map = FileUtility.readMapFile(inputMethodMapFile);
		splitMap(map);
	}
	
	/**
	 * Splits the raw string-string-map into the two maps xpaths and types.
	 * 
	 * @param map A map containing the specified input methods in raw form.
	 * 			  key:   Mixture of XPaths and XSD types in string representation.
	 * 			  value: Associated input method in string representation. 
	 */
	private void splitMap(final Map<String, String> map) {
		for (String key : map.keySet()) {
			String value = map.get(key);
			InputMethod method = InputMethod.valueOfSave(value);
			if (method != null) {
				if (XSDTypeName.valueOfSave(key) == null) {
					// key is no type, must be a XPath
					XPath xpathEval = XPathFactory.newInstance().newXPath();
					try {
						XPathExpression xpathExpr = xpathEval.compile(key);
						if (xpathExpr != null) {
							xpaths.put(xpathExpr, method);
						}
					} catch (XPathExpressionException e) {
						logger.warn("Could not evaluate XPath " + key + " in input methods map.");
					}
				} else {
					// key represents a XSD type
					this.types.put(key, method);
				}
			}
		}
	}
	
	/**
	 * Returns the InputMethod specified for a DOM node.
	 * 
	 * @param node       Node for which the InputMethod is requested.
	 * @param constraint NodeConstraint containing the XSD type of node.
	 * @return           InputMethod for node. Null if no method was specified.
	 */
	public InputMethod getMethodFor(final Node node, 
									final NodeConstraint constraint) {
		
		InputMethod methodForType = getMethodFor(constraint);
		InputMethod methodForNode = getMethodFor(node);
		
		if (methodForNode != InputMethod.NOT_SPECIFIED) {
			return methodForNode;
		} else {
			return methodForType;
		}
	}
	
	public InputMethod getMethodFor(final Node node) {
		Document xmlDoc = node.getOwnerDocument();
		for (XPathExpression xpath : this.xpaths.keySet()) {
			try {
				Node maybeNode = (Node) xpath.evaluate(xmlDoc, XPathConstants.NODE);
				if (node == maybeNode) {
					return this.xpaths.get(xpath);
				}
			} catch (XPathExpressionException e) {
				logger.warn("Unvalid XPath in input methods mapfile.", e);
			}
		}
		
		return InputMethod.NOT_SPECIFIED;
	}
	public InputMethod getMethodFor(final NodeConstraint constraint) {
		String typeName = constraint.getTypeName();
		InputMethod method = this.types.get(typeName);
		if (method != null) {
			return method;
		}
		
		return InputMethod.NOT_SPECIFIED;
	}
	
	public InputMethod getMethodFor(final String xsdType) {
		InputMethod method = this.types.get(xsdType);
		if (method != null) {
			return method;
		}
		
		return InputMethod.NOT_SPECIFIED;
	}
	public InputMethod getMethodFor(final XSDTypeName xsdType) {
		InputMethod method = this.types.get(xsdType.toString());
		if (method != null) {
			return method;
		}
		
		return InputMethod.NOT_SPECIFIED;
	}

}
