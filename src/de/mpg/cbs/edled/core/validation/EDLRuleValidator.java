package de.mpg.cbs.edled.core.validation;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.core.validation.EDLRuleParameter.ParameterPathKind;
import de.mpg.cbs.edled.xml.XMLUtility;





public class EDLRuleValidator {
	
	private final static Logger logger = Logger.getLogger(EDLRuleValidator.class);
	
//	private final File ruleFile;
	private final Document ruleDocument;
	private final List<EDLRule> rules = new LinkedList<EDLRule>();
	
	
	public EDLRuleValidator(final File ruleFile) {
//		this.ruleFile = ruleFile;
		this.ruleDocument = XMLUtility.loadDocument(ruleFile, null);
		if (this.ruleDocument != null) {
			buildRules();
		} else {
			logger.warn("Reading the rule XML file failed!");
		}
	}
	
	public Map<EDLRule, List<Node>> validate(final Document xmlDocument, final List<EDLRule> rules) {
		Map<EDLRule, List<Node>> validatedRules = new HashMap<EDLRule, List<Node>>();
		
		for (EDLRule rule : rules) {
			validatedRules.put(rule, rule.evaluate(xmlDocument));
		}
		
		return validatedRules;
	}
	
	public Map<EDLRule, List<Node>> validate(final Document xmlDocument) {
		Map<EDLRule, List<Node>> validatedRules = new HashMap<EDLRule, List<Node>>();
		
		for (EDLRule rule : this.rules) {
			validatedRules.put(rule, rule.evaluate(xmlDocument));
		}
		
		return validatedRules;
	}
	
	private void buildRules() {
		try {
			NodeList ruleNodes = (NodeList) XPathFactory.newInstance()
														.newXPath().evaluate("/edlRules/rule", 
														                     this.ruleDocument, 
																			 XPathConstants.NODESET);
			int ruleNr = 0;
			while (ruleNr < ruleNodes.getLength()) {
				Node ruleNode = ruleNodes.item(ruleNr);
				
				if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
					Element ruleElem = (Element) ruleNode;
					String ruleID = ruleElem.getAttribute("ruleID");
					Map<String, EDLRuleParameter> parameters = new HashMap<String, EDLRuleParameter>();
					List<EDLRuleLiteral> premise = new LinkedList<EDLRuleLiteral>();
					List<EDLRuleLiteral> conclusion = new LinkedList<EDLRuleLiteral>();
					String message = "";
					
					NodeList ruleChildren = ruleElem.getChildNodes();
					int childNr = 0;
					while (childNr < ruleChildren.getLength()) {
						Node ruleChild = ruleChildren.item(childNr);
						
						if (ruleChild.getNodeType() == Node.ELEMENT_NODE) {
							Element ruleChildElem = (Element) ruleChild;
							String childName = ruleChildElem.getNodeName();
							
							// Parameters.
							if (childName.compareTo("param") == 0) {
								String paramID = ruleChildElem.getAttribute("pID").trim();
								String paramName = ruleChildElem.getAttribute("pName").trim();
								// A parameter element has only 1 paramRef child element.
								String paramRef = ruleChildElem.getElementsByTagName("paramRef").item(0).getTextContent().trim();
								
								parameters.put(paramID, new EDLRuleParameter(paramID, paramName, paramRef, ParameterPathKind.MATLABPATH));
								
							// Premise (literals).
							} else if (childName.compareTo("premise") == 0) {
								NodeList premiseLiterals = ruleChildElem.getChildNodes();
								int literalNr = 0;
								while (literalNr < premiseLiterals.getLength()) {
									Node literal = premiseLiterals.item(literalNr);
									if (literal.getNodeType() == Node.ELEMENT_NODE) {
										String literalValue = literal.getTextContent().trim();
										premise.add(new EDLRuleLiteral(literalValue));
									}
									
									literalNr++;
								}
								
							// Conclusion (literals).
							} else if (childName.compareTo("conclusion") == 0) {
								// A conclusion must have 1 literal.
								Node literal = ruleChildElem.getElementsByTagName("literal").item(0); 
								String literalValue = literal.getTextContent().trim();
								conclusion.add(new EDLRuleLiteral(literalValue));
								
							// ErrorMessage.
							} else if (childName.compareTo("message") == 0) {
								message = ruleChildElem.getTextContent().trim();
							}
							
						}
						
						childNr++;
					}
					
					this.rules.add(new EDLRule(ruleID, parameters, premise, conclusion, message));
				}
				
				ruleNr++;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<EDLRule> getRules() {
		return this.rules;
	}
	
	public List<EDLRule> getRulesMissingParameters(Map<EDLRule, List<Node>> rulesUsingNodes) {
		List<EDLRule> rulesMissingParameters = new LinkedList<EDLRule>();
		
		for (EDLRule rule : this.rules) {
			List<Node> usedNodes = rulesUsingNodes.get(rule);
			if (usedNodes != null) {
				if (usedNodes.size() < rule.getParameterCount()) {
					rulesMissingParameters.add(rule);
				}
			}
		}
		
		return rulesMissingParameters;
	}
	
//	public List<EDLRule> getRulesRelevantFor(final Node node) {
//		List<EDLRule> relevantRules = new LinkedList<EDLRule>();
//		
//		for (EDLRule rule : this.rules) {
//			for (RuleParameter param : rule.getParameters().values()) {
//				if (param.refersTo(node)
//					&& !relevantRules.contains(rule)) {
//					relevantRules.add(rule);
//				}
//			}
//		}
//		
//		return relevantRules;
//	}
	
//	public List<String> getViolations() {
//		return null;
//	}

}
