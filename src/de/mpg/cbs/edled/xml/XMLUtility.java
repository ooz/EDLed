package de.mpg.cbs.edled.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Collection of convenience functions to handle
 * common XML tasks.
 * 
 * @author Oliver Z.
 */
public class XMLUtility {
	
	private static final Logger logger = Logger.getLogger(XMLUtility.class);
	
	/**
	 * Returns the occurrences of a given node (specified by its name)
	 * in a given list of nodes. Counts all subsequent occurs after the
	 * first one.
	 * 
	 * @param nodeName The name of the node whose occurrences are requested
	 * @param nodes	   The list of nodes that will be searched for occurrences of
	 * 				   nodeName.
	 * @return		   The number of the first subsequent occurences of nodeName. 
	 * 				   Greater than or equal to 0.
	 */
	public static int getOccurs(String nodeName, NodeList nodes) {
		int occurs = 0;
		
		int childNr = 0;
		boolean foundFirstOccurence = false;
		while (childNr < nodes.getLength()) {
			Node node = nodes.item(childNr);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().compareTo(nodeName) == 0) {
					occurs++;
					foundFirstOccurence = true;
				} else {
					if (foundFirstOccurence) {
						return occurs;
					}
				}
			}
			childNr++;
		}
		
		return occurs;
	}

	/**
	 * Returns the occurrences of a given node/element. Checks whether the previous/following
	 * elements have the same name.
	 * 
	 * @param node The node whose occurrences should be determined.
	 * @return	   The occurrences of node. Greater than or equal to 1.
	 */
	public static int getOccurs(Node node) {
		int occurs = 1;
		String nodeName = node.getNodeName();
		
		Node prevSib = node.getPreviousSibling();
		while (prevSib != null) {
			if (prevSib.getNodeName().compareTo(nodeName) == 0
				&& prevSib.getNodeType() == Node.ELEMENT_NODE) {
				occurs++;
				prevSib = prevSib.getPreviousSibling();
			} else {
				prevSib = null;
			}
		}
		
		Node nextSib = node.getNextSibling();
		while (nextSib != null) {
			if (nextSib.getNodeName().compareTo(nodeName) == 0
				&& nextSib.getNodeType() == Node.ELEMENT_NODE) {
				occurs++;
				nextSib = nextSib.getNextSibling();
			} else {
				nextSib = null;
			}
		}
		
		return occurs;
	}
	
	/**
	 * Convenience method for getting the content of the first
	 * text child of a given element.
	 * 
	 * @param elem The element whose text content is queried.
	 * @return	   The text content of the given element. 
	 * 			   Null if is has only node children and no text content.
	 */
	public static String getTextFromElem(final Element elem) {
		NodeList children = elem.getChildNodes();
		int childNr = 0;
		while (childNr < children.getLength()) {
			Node possibleTextNode = children.item(0);
			short nodeType = possibleTextNode.getNodeType();
			if (nodeType == Node.TEXT_NODE) {
				return possibleTextNode.getNodeValue();
			} else if (nodeType != Node.COMMENT_NODE
					   && nodeType != Node.PROCESSING_INSTRUCTION_NODE) {
				return null;
			}
			
			childNr++;
		}
		
		return "";
	}
	
	/**
	 * Convenience method for getting a node's (attr or elem) text value.
	 * 
	 * @param node The node whose text content is requested.
	 * @return	   Returns the text value of the given node. Null if is not allowed to have any.
	 */
	public static String getNodeValue(final Node node) {
		if (node == null) {
			return null;
		}
		
		short nodeKind = node.getNodeType();
		switch (nodeKind) {
		case Node.ATTRIBUTE_NODE:
		case Node.TEXT_NODE:
			return node.getNodeValue();
		case Node.ELEMENT_NODE:
			return XMLUtility.getTextFromElem((Element) node);
		default:
			return null;	
		}
	}
	
	/**
	 * Convenience method for setting a node's (attr or elem) text value.
	 * 
	 * @param node     The DOM node whose value needs to be changed.
	 * @param newValue The value to set for node.
	 */
	public static void setNodeValue(final Node node, 
									final String newValue) {
		short nodeType = node.getNodeType();
		if (nodeType == Node.ATTRIBUTE_NODE) {
			node.setNodeValue(newValue);
		} else if (nodeType == Node.ELEMENT_NODE) {
			node.setTextContent(newValue);
		}
	}
	
	/**
	 * Saves a DOM document to a given file.
	 * 
	 * @param doc  The document to save/write.
	 * @param file The file to write to.
	 */
	public static void saveDocument(final Document doc, final File file) {
		doc.setXmlStandalone(true);
		Source source = new DOMSource(doc);
		Result result = new StreamResult(file);
		
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
//			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
//			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			logger.error("Document saving failed with exception!", e);
		} catch (TransformerFactoryConfigurationError e) {
			logger.error("Document saving failed with exception!", e);
		} catch (TransformerException e) {
			logger.error("Document saving failed with exception!", e);
		}
	}
	
	/**
	 * Loads a XML document from a file. Optionally associates a XML schema with
	 * that loaded document.
	 * 
	 * @param documentFile The file containing the XML document to load.
	 * @param schema       Optional XML Schema containing the XML Schema Definition (XSD) for
	 * 					   documentFile. Pass null if not needed.
	 * @return			   The loaded XML document. Returns null if a fatal error occurred
	 * 					   during the parsing process.
	 */
	public static Document loadDocument(final File documentFile, final Schema schema) {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			builderFactory.setIgnoringElementContentWhitespace(true);
			if (schema != null) {
				builderFactory.setSchema(schema);
			}
			
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					logger.warn("Warning (line:" + exception.getLineNumber() 
										  + ", column:" + exception.getColumnNumber() +"): "
										  + exception.getMessage());
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					logger.error("Critical error (line:" + exception.getLineNumber() 
							  			  	  + ", column:" + exception.getColumnNumber() +"): "
							  			  	  + exception.getMessage());
				}
				@Override
				public void error(SAXParseException exception) throws SAXException {
					logger.warn("Non-critical error (line:" + exception.getLineNumber() 
										+ ", column:" + exception.getColumnNumber() +"): "
										+ exception.getMessage());
				}
			});
			return builder.parse(documentFile);
		} catch (SAXException e) {
			logger.error("Document loading failed with exception!", e);
		} catch (IOException e) {
			logger.error("Document loading failed with exception!", e);
		} catch (ParserConfigurationException e) {
			logger.error("Document loading failed with exception!", e);
		}

		return null;
	}
	
	/**
	 * Loads a XML Schema from a given file.
	 * 
	 * @param schemaFile The XML Schema file (.xsd) to load the schema from.
	 * @return		     Null if something went wrong.
	 */
	public static Schema loadSchema(final File schemaFile) {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return schemaFactory.newSchema(schemaFile);
		} catch (SAXException e) {
			logger.error("Could not load XML Schema: " + schemaFile.getAbsolutePath() + "!", e);
		}
		
		return null;
	}
	
//	public static List<String> validate(final Node node, final Schema schema) {
//		
//		final List<String> errorMessages = new LinkedList<String>();
//		
//		Validator validator = schema.newValidator();
//		validator.setErrorHandler(new ErrorHandler() {
//			@Override
//			public void warning(SAXParseException e) throws SAXException {
//				errorMessages.add("Warning: " + e.getMessage());
//			}
//			@Override
//			public void fatalError(SAXParseException e) throws SAXException {
//				errorMessages.add("Critical error: " + e.getMessage());
//			}
//			@Override
//			public void error(SAXParseException e) throws SAXException {
//				errorMessages.add("Non-critical error: " + e.getMessage());
//			}
//		});
//		
//		try {
//			validator.validate(new DOMSource(node));
//		} catch (SAXException e) {
//			logger.error("Node validation failed with exception!", e);
//		} catch (IOException e) {
//			logger.error("Node validation failed with exception!", e);
//		}
//		
//		return errorMessages;
//	}

}
