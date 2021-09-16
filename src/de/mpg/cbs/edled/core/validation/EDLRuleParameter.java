package de.mpg.cbs.edled.core.validation;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class EDLRuleParameter {
	
	public enum ParameterPathKind {
		XPATH,
		MATLABPATH;
	}
	
	private final String id;
	private final String name;
	private final String xpathString;
	private XPathExpression compiledXPath = null;
	private QName xpathReturnType = null;
	
	public EDLRuleParameter(final String id,
					 	 final String name,
					 	 final String path,
					 	 final ParameterPathKind pathKind) {
		this.id = id;
		this.name = name;
		
		if (pathKind == ParameterPathKind.MATLABPATH) {
			this.xpathString = convertMatlabTreePathToXPath(path);
		} else {
			this.xpathString = path;
		}
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			this.compiledXPath = xpath.compile(this.xpathString);
		} catch (XPathExpressionException e) {
//			System.out.println();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String convertMatlabTreePathToXPath(final String matlabTreePath) {
		
		String converted = matlabTreePath;
		
		if (converted.contains("{")) {
			converted = converted.replace("{", "[");
			converted = converted.replace("}", "]");
		}
		
		if (converted.contains(".ATTRIBUTE.")) {
			converted = converted.replace(".ATTRIBUTE.", "/@");
			this.xpathReturnType = XPathConstants.STRING;
		} else if (converted.contains(".CONTENT")) {
			converted = converted.replace(".CONTENT", "");
			this.xpathReturnType = XPathConstants.STRING;
		} else {
			this.xpathReturnType = XPathConstants.NODESET;
		}
		
		if (converted.contains(".")) {
			converted = converted.replace(".", "/");
		}
		
		return converted;
	}
	
	public String getID() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public String getPath() {
		return this.xpathString;
	}
	
	public ParameterValue evaluate(final Document xmlDocument) {
		ParameterValue value = null;
		
		try {
			NodeList result = (NodeList) this.compiledXPath.evaluate(xmlDocument, XPathConstants.NODESET);
			if (result.getLength() == 0) {
				value = new ParameterValue();
			} else {
				value = new ParameterValue(result, this.xpathReturnType);
			}
			
//			if (this.xpathReturnType == XPathConstants.STRING) {
//				String result = (String) this.compiledXPath.evaluate(xmlDocument, this.xpathReturnType);
//				value = new ParameterValue(result);
//			} else if (this.xpathReturnType == XPathConstants.NODESET) {
//				NodeList result = (NodeList) this.compiledXPath.evaluate(xmlDocument, this.xpathReturnType);
//				if (result.getLength() == 0) {
//					value = new ParameterValue();
//				} else {
//					value = new ParameterValue(result);
//				}
//			}	
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (value == null) {
			value = new ParameterValue();
		}
		
		return value;
	}
	
//	public boolean refersTo(final Node node) {
//		try {
//			NodeList result = (NodeList) this.compiledXPath.evaluate(node.getOwnerDocument(), XPathConstants.NODESET);
//			if (result.getLength() > 0) {
//				if (result.item(0) == node) {
//					return true;
//				}
//			}
//		} catch (XPathExpressionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return false;
//	}
	
	public String toString() {
		return "RuleParameter{id=" + this.id + ", name=" + this.name + ", path=" + this.xpathString + "}";
	}
	
	public class ParameterValue {
		
		private final NodeList nodes;
		private final QName valueKind;
		
		public ParameterValue() {
			this.nodes = null;
			this.valueKind = null;
		}
		
		public ParameterValue(final NodeList nodes, final QName valueKind) {
			this.nodes = nodes;
			this.valueKind = valueKind;
		}
		
		public boolean isNodeList() {
			if (this.valueKind == XPathConstants.NODESET) {
				return true;
			}
			
			return false;
		}
		public boolean isNodeValue() {
			if (this.valueKind == XPathConstants.STRING) {
				return true;
			}
				
			return false;
		}
		public boolean isNull() {
			if (this.nodes == null) {
				return true;
			}
			
			return false;
		}
		
		public NodeList getNodeList() {
			return this.nodes;
		}
	}

}
