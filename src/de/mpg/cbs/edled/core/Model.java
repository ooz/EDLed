package de.mpg.cbs.edled.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.core.metatree.MetaAttr;
import de.mpg.cbs.edled.core.metatree.MetaNode;
import de.mpg.cbs.edled.core.metatree.MetaTreeBuilder;
import de.mpg.cbs.edled.core.metatree.NodeConstraint;
import de.mpg.cbs.edled.core.metatree.MetaNode.MetaXMLNodeKind;
import de.mpg.cbs.edled.core.validation.EDLRule;
import de.mpg.cbs.edled.core.validation.EDLRuleValidator;
import de.mpg.cbs.edled.core.validation.SimpleTypeValidator;
import de.mpg.cbs.edled.core.validation.ValidationResult;
import de.mpg.cbs.edled.xml.XMLUtility;



/**
 * Root model object encapsulating all important model objects:
 * - the XML document
 * - the model used for the visual JTree (DefaultTreeModel)
 * - the meta tree
 * 
 * Uses a TreeManager to keep the models updated with one another.
 * 
 * @author Oliver Z.
 */
public class Model extends Observable implements Observer {
	
	private static final Logger logger = Logger.getLogger(Model.class);
	
	/** The XML document (EDL configuration) that is subject to editing. */
	private Document xmlDocument = null;
	/** Model for the JTree (visual representation of the XML document). */
	private DefaultTreeModel treeModel = null;
	/** The meta tree abstracting the XML schema. */
	private MetaNode xmlMetaTree = null;
	
	/** TreeManager to keep the three models above consistent with one another. */
	private TreeManager treeManager = null;
	
	/** EDL rule validator object. */
	private EDLRuleValidator edlValidator = null;
	
	/**
	 * Constructor for a empty model (XML document).
	 * 
	 * @param documentElementName The name of the document element.
	 * @param metaTreeBuilder	  MetaTreeBuilder that should be used to create the meta tree.
	 * @param edlValidator		  Optional EDLRuleValidator if EDL rules should be checked.
	 */
	public Model(final String documentElementName,
			     final MetaTreeBuilder metaTreeBuilder,
			     final EDLRuleValidator edlValidator) {
		try {
			this.xmlDocument = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			logger.error("Could not create new XML/EDL document.", e);
		}
		
		this.xmlMetaTree = metaTreeBuilder.buildMetaXMLTree(documentElementName);
		this.edlValidator = edlValidator;
		
		this.treeManager = new TreeManager(this.xmlDocument);
		this.xmlDocument.appendChild(this.treeManager.create(this.xmlMetaTree));
		
		// First time rule validation.
		checkRules(this.edlValidator.getRules());
	}
	
	/**
	 * Constructor for initialization with an existing XML document.
	 * 
	 * @param document 		  The XML document to be initialized with.
	 * @param metaTreeBuilder The MetaTreeBuilder needed for creation of
	 * 						  any additional nodes.
	 * 
	 * @throws IllegalArgumentException If any parameter is null.
	 * @throws RuntimeException If document is not valid.
	 */
	public Model(final Document document,
			     final MetaTreeBuilder metaTreeBuilder,
			     final EDLRuleValidator edlValidator) {
		
		if (document == null || metaTreeBuilder == null) {
			throw new IllegalArgumentException("Parameter document is null!");
		}
		
		
		this.xmlMetaTree = metaTreeBuilder.buildMetaXMLTree(document.getDocumentElement().getNodeName());
		
		this.treeManager = new TreeManager(document);
		// Init map between document and xmlMetaTree
		if (!this.treeManager.mapExisting(document.getDocumentElement(), this.xmlMetaTree)) {
			throw new RuntimeException("Parameter document is not valid!");
		}
		
		this.xmlDocument = document;
		this.edlValidator = edlValidator;
		
		// First time rule validation.
		checkRules(this.edlValidator.getRules());
	}
	
	public MetaNode getMetaXMLNodeForNode(final Node node) {
		return this.treeManager.getMetaNode(node);
	}
	
	/* OLD:
	 * 1. check whether additional nodes like node can be added as siblings
	 * 2. check whether node can be removed
	 * 3. check whether the entry set in which node is contained can be repeated (comp can be sequence or choice! recursion, check for all parent comps)
	 * 4. check whether entry set in which node is contained can be removed (recursion, check for all parent comps)
	 * 5. check for single node alternatives
	 * 6. check for entry set alternatives (check recursively)
	 * 7. check whether node has comps with 0 occurences
	 * 8. check whether node has children with 0 occurences
	 */
	/* NEW:
	 * After making restrictions to the use of compositors in the XSD:
	 * 1. check whether additional nodes like node can be added as siblings
	 * 2. check whether node can be removed
	 * 3. check for alternatives to node
	 * 4. check for currently not used optional child elements of node. those could be added
	 */
	public List<ManipulationOption> getManipulationOptionsFor(final Node node) {
	
		MetaNode metaNode = this.treeManager.getMetaNode(node);
		if (metaNode == null) {
			return new LinkedList<ManipulationOption>();
		}
		NodeConstraint constraint = metaNode.getConstraint();
		
		List<ManipulationOption> options = this.treeManager.getOptions(node);
		if (options != null) {
			// Skip option building process and return cached options.
			return options;
		} else {
			options = new LinkedList<ManipulationOption>();
		}
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			int occurs = XMLUtility.getOccurs(node);
			int minOccurs = constraint.getMinOccurs();
			int maxOccurs = constraint.getMaxOccurs();
			
			Node xmlParent = node.getParentNode();
			
			// 1. check whether additional nodes like node can be added as siblings.
			if ((occurs < maxOccurs
				|| maxOccurs == NodeConstraint.UNBOUNDED)
				&& node != this.xmlDocument.getDocumentElement()
				&& xmlParent != null) {
				
				options.add(new ManipulationAddOption(this, 
													  metaNode, 
													  xmlParent, 
													  node,
													  ManipulationOption.ManipulationOptionKind.ADD_ADDITIONAL));
			}
			
			// 2. check whether node can be removed.
			if (occurs > minOccurs
				&& node != this.xmlDocument.getDocumentElement()
				&& xmlParent != null) {
				
				options.add(new ManipulationRemoveOption(this, 
														 node));
			}
			
			// 3. check for alternatives to node.
			MetaNode metaParent = metaNode.getParent();
			if (metaParent != null) {
				if (metaParent.getKind() == MetaXMLNodeKind.CHOICE_COMPOSITOR) {
					List<MetaNode> choiceOptions = metaParent.getChildren();
					for (MetaNode choiceOption : choiceOptions) {
						if (choiceOption != metaNode) {
							options.add(new ManipulationChoiceOption(this,
									 								 choiceOption, 
									 								 xmlParent, 
									 								 node.getPreviousSibling(), 
									 								 node));
						}
					}
				}
			}
			
			// 4. check for currently not used optional child elements of node. those could be added
			NodeList children = node.getChildNodes();
			List<MetaNode> descendants = this.treeManager.getNonCompDirectDescendants(metaNode);
			for (MetaNode descendant : descendants) {
				NodeConstraint descConstraint = descendant.getConstraint();
				
				int descMaxOccurs = descConstraint.getMaxOccurs();
				int descOccurs = XMLUtility.getOccurs(descendant.getName(), children); //descConstraint.getOccurs();
				if (descOccurs == 0 
					&& (descMaxOccurs == NodeConstraint.UNBOUNDED || descOccurs < descMaxOccurs)) {
					
					List<MetaNode> choiceAlternatives;
					MetaNode descendantParent = descendant.getParent();
					if (descendantParent.getKind() == MetaXMLNodeKind.CHOICE_COMPOSITOR) {
						choiceAlternatives = descendantParent.getChildren();
					} else {
						choiceAlternatives = new LinkedList<MetaNode>();
					}
					
					// Find exact position where to add the child.
					// Also check whether descendant is part of a choice that is already represented.
					Node prevChild = null;
					boolean skipBecauseOfChoice = false;
//					Node nextChild = null;
					int childNr = 0;
					while (childNr < children.getLength()
						   /* && (nextChild == null) */
						   && !skipBecauseOfChoice) {
						Node child = children.item(childNr);
						MetaNode childMeta = this.treeManager.getMetaNode(child);
						
						if (choiceAlternatives.contains(childMeta)) {
							skipBecauseOfChoice = true;
						}
						
						if (!skipBecauseOfChoice
							&& descendants.contains(childMeta) // TODO: <-- not sure if this check/line is necessary
							&& descendants.indexOf(childMeta) < descendants.indexOf(descendant)) {
							prevChild = child;
						}
						
						childNr++;
					}
					if (!skipBecauseOfChoice) {
						options.add(new ManipulationAddOption(this, 
								  							  descendant, 
								  							  node, 
								  							  prevChild,
								  							  ManipulationOption.ManipulationOptionKind.ADD_CHILD));
					}
				}
			}
		} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
			ManipulationOption removeOption = getRemoveOptionFor((Attr) node);
			if (removeOption != null) {
				options.add(removeOption);
			}
		}
		
		options.addAll(getAttributeAddOptionsFor(node));
		
		this.treeManager.put(node, options);
		
		return options;
	}
	
	private ManipulationOption getRemoveOptionFor(Attr attr) {
		ManipulationOption option = null;
		
		if (attr != null) {
			NodeConstraint attrConstraint = this.treeManager.getMetaNode(attr).getConstraint();
			if (attrConstraint.hasAttributeUse()) {
				switch (attrConstraint.getAttributeUse()) {
				case OPTIONAL:
					if (!attrConstraint.hasDefaultValue()
							&& !attrConstraint.hasFixedValue()) {
						option = new ManipulationRemoveOption(this, attr);
					}
					break;
				case PROHIBITED:
					break;
				case REQUIRED:
					break;
				default:
					throw new RuntimeException("Undefined attrUse!");
				}
			}
		}
		
		return option;
	}
	
	private List<ManipulationOption> getAttributeAddOptionsFor(final Node node) {
		MetaNode metaNode = this.treeManager.getMetaNode(node);
		
		List<ManipulationOption> options = new LinkedList<ManipulationOption>();
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element) node;
			
			for (MetaNode metaChild : metaNode.getAttributes().values()) {
				MetaAttr metaAttr = (MetaAttr) metaChild;
				NodeConstraint attrConstraint = metaAttr.getConstraint();
				if (attrConstraint.hasAttributeUse()) {
					Attr attr = elem.getAttributeNode(metaAttr.getName());
					switch (attrConstraint.getAttributeUse()) {
					case OPTIONAL:
						if (attr == null
								&& !attrConstraint.hasDefaultValue()
								&& !attrConstraint.hasFixedValue()) {
							options.add(new ManipulationAddOption(this, metaAttr, elem));
						}
						break;
					case PROHIBITED:
						break;
					case REQUIRED:
						break;
					default:
						throw new RuntimeException("Undefined attrUse!");
					}
				}
			}
		}
		
		return options;
	}
	
	public ValidationResult getValidationResult(final Node node, final boolean deep) {
		return getValidationResult(node, deep, false);
	}
	
	private ValidationResult getValidationResult(final Node node, 
												 final boolean deep,
											 	 final boolean force) {
		if (node == null) {
			return new ValidationResult(false);
		}
		
		if (isWhitelisted(node)) {
			return new ValidationResult(true);
		}
		
		ValidationResult validationResult = this.treeManager.getValidationResult(node);
		// First time validation /* or revalidation */
		if (validationResult == null
				|| force
				// || !validationResult.isValid()
				) {
			validate(node);
			validationResult = this.treeManager.getValidationResult(node);
		}

		if (deep) {
			boolean isValid = validationResult.isValid();
			
			if (node.hasAttributes()) {
				NamedNodeMap attrs = node.getAttributes();
				for (int attrNr = 0; attrNr < attrs.getLength() 
									 && (force || isValid); attrNr++) {
					isValid = isValid & getValidationResult(attrs.item(attrNr), false, force).isValid();
				}
			}
			
			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				for (int childNr = 0; childNr < children.getLength() 
									  && (force || isValid); childNr++) {
					Node child = children.item(childNr);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						isValid = isValid & getValidationResult(child, true, force).isValid();
					}
				}
			}	
			
			return new ValidationResult(isValid);
		} else {
			return validationResult;
		}
	}
	
	private void validate(final Node node) {
		if (node == null) {
			return;
		}
		
		boolean validNodeValue = validateValue(node, false);

		if (validNodeValue) {
			validateRules(node, false);
		}
		
		this.treeManager.put(node, new ValidationResult(validNodeValue, this.treeManager.getRelevantRules(node)));
	}

	private List<EDLRule> validateRules(final Node node, final boolean deep) {
		
		List<EDLRule> violatedRules = new LinkedList<EDLRule>();
		
		List<EDLRule> relevantRules = this.treeManager.getRelevantRules(node);
		if (relevantRules == null) {
			return violatedRules;
		}
	
		for (EDLRule rule : relevantRules) {
			if (!rule.isSatisfied()) {
				violatedRules.add(rule);
			}
		}
		
		if (deep) {
			if (node.hasAttributes()) {
				NamedNodeMap attrs = node.getAttributes();
				for (int attrNr = 0; attrNr < attrs.getLength(); attrNr++) {
					violatedRules.addAll(validateRules(attrs.item(attrNr), false));
				}
			}
			
			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				for (int childNr = 0; childNr < children.getLength(); childNr++) {
					Node child = children.item(childNr);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						violatedRules.addAll(validateRules(child, true));
					}
				}
			}	
		}
		
		return violatedRules;
	}

	private boolean validateValue(final Node node, final boolean deep) {
		if (isWhitelisted(node)) {
			return true;
		}
		
		NodeConstraint constraint = this.treeManager.getMetaNode(node).getConstraint();
		
		boolean isValid = true;
		if (constraint.canHaveTextContent()) {
			isValid = SimpleTypeValidator.getSingleton().validate(node, constraint);
		}
		
		if (deep) {
			if (node.hasAttributes()) {
				NamedNodeMap attrs = node.getAttributes();
				for (int attrNr = 0; attrNr < attrs.getLength(); attrNr++) {
					isValid = isValid && validateValue(attrs.item(attrNr), false);
				}
			}
			
			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				for (int childNr = 0; childNr < children.getLength(); childNr++) {
					Node child = children.item(childNr);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						isValid = isValid && validateValue(child, true);
					}
				}
			}	
		}
		
		return isValid;
	}

	private void checkRules(final List<EDLRule> rules) {
		Map<EDLRule, List<Node>> rulesUsingNodes = this.edlValidator.validate(this.xmlDocument, rules);
		
		for (EDLRule rule : rulesUsingNodes.keySet()) {
			List<Node> usedNodes = rulesUsingNodes.get(rule);
			
			for (Node usedNode : usedNodes) {
				List<EDLRule> relevantRulesForNode = this.treeManager.getRelevantRules(usedNode);
				if (relevantRulesForNode == null) {
					relevantRulesForNode = new LinkedList<EDLRule>();
					relevantRulesForNode.add(rule);
					this.treeManager.putRelevantRules(usedNode, relevantRulesForNode);
				} else {
			 		if (!relevantRulesForNode.contains(rule)) {
			 			relevantRulesForNode.add(rule);
			 		}
			 	}
			}
			
			this.treeManager.put(rule, usedNodes);
		}
	}
	
	public boolean isWhitelisted(final Node node) {
		return this.treeManager.isWhitelisted(node.getNodeName());
	}
	
	public TreeModel treeModel() {
		if (this.treeModel == null) {
//			this.treeModel = new DefaultTreeModel(this.buildTree(this.xmlDocument.getDocumentElement()));
			this.treeModel = new DefaultTreeModel(this.treeManager.create(this.xmlDocument.getDocumentElement()));
		}
		
		return this.treeModel;
	}
	
	public Document getDocument() {
		return this.xmlDocument;
	}
	
	/**
	 * Revalidates (checking XSD types and EDL rules) the whole model. 
	 */
	public void revalidate() {
		getValidationResult(this.xmlDocument.getDocumentElement(), true, true);
	}
	
	/**
	 * Adds a node to the XML tree.
	 * 
	 * @param metaNode       A MetaXMLNode containing all information for the node to 
	 * 					     create and add.
	 * @param xmlParent      The DOM node that should be the parent of the node to add.
	 * @param xmlPrevSibling The DOM node that precedes the node to add. If null the
	 * 						 node to add is inserted at index 0.
	 */
	synchronized void addNodeLike(final MetaNode metaNode,
					 			  final Node xmlParent,
					 			  final Node xmlPrevSibling) {
		
//		Node newXMLNode = buildXMLTree(metaNode, true).get(0);
		Node newXMLNode = this.treeManager.create(metaNode);
		DefaultMutableTreeNode newTreeNode = this.treeManager.create(newXMLNode);
		DefaultMutableTreeNode treeParent = this.treeManager.getTreeNode(xmlParent);
		DefaultMutableTreeNode treePrevSibling = null; 
		
		int insertionIndex = 0;
		if (xmlPrevSibling != null) {
			treePrevSibling = this.treeManager.getTreeNode(xmlPrevSibling);
			xmlParent.insertBefore(newXMLNode, 
					   			   xmlPrevSibling.getNextSibling());
			
			insertionIndex = treeParent.getIndex(treePrevSibling) + 1;
		} else {
//			xmlParent.insertBefore(newXMLNode, 
//								   null);
//			
//			insertionIndex = treeParent.getChildCount();
//			// If there are no children yet, insert at index 0.
//			// Else append at the end of child list, hence childCount - 1.
//			if (insertionIndex > 0) {
//				insertionIndex--;
//			}
			xmlParent.insertBefore(newXMLNode, xmlParent.getFirstChild());
		}
		
		this.treeModel.insertNodeInto(newTreeNode, 
						  			  treeParent, 
						  			  insertionIndex);
		
		this.treeManager.invalidateOptions(xmlParent);
		
		checkRules(this.edlValidator.getRulesMissingParameters(this.treeManager.getRulesUsingNodes()));
		validate(newXMLNode);
		
		logger.info("Added " + newXMLNode.getNodeName() + " to " + xmlParent.getNodeName() + ".");
	}
	
	synchronized void removeNode(final Node xmlNode) {

		Node parent = null;
		if (xmlNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			Attr attr = (Attr) xmlNode;
			Element ownerElem = attr.getOwnerElement();
			ownerElem.removeAttributeNode(attr);
			parent = ownerElem;
		} else {
			parent = xmlNode.getParentNode();
			parent.removeChild(xmlNode);
			this.treeModel.removeNodeFromParent(this.treeManager.getTreeNode(xmlNode));
		}
		
		List<EDLRule> relevantRules = this.treeManager.getRelevantRules(xmlNode);
		if (relevantRules != null) {
			checkRules(relevantRules);
		}
		
		this.treeManager.invalidateOptions(parent);
		this.treeManager.destroy(xmlNode);
		
		logger.info("Removed " + xmlNode.getNodeName() + ".");
	}
	
	synchronized void addAttributeLike(final MetaAttr metaAttr,
									   final Element elem) {
		Attr attr = (Attr) this.treeManager.create(metaAttr);
		elem.setAttributeNode(attr);
		
		this.treeManager.invalidateOptions(elem);
		
		checkRules(this.edlValidator.getRulesMissingParameters(this.treeManager.getRulesUsingNodes()));
		validate(attr);
		
		logger.info("Added attribute " + attr.getNodeName() + " to " + elem.getNodeName() + ".");
	}
	
	synchronized void chooseAlternative(MetaNode metaReplacement,
									    Node xmlParent,
									    Node xmlPrevSibling,
									    Node xmlToReplace) {
//		removeNode(xmlToReplace);
		addNodeLike(metaReplacement, xmlParent, xmlPrevSibling);
		removeNode(xmlToReplace);
		
		this.treeManager.invalidateOptions(xmlParent);
	}
	
	
	public synchronized void setNodeValue(final Node node, final String newValue) {
		// TODO: use nodevalue-changer-objects!
		NodeConstraint constraint = this.treeManager.getMetaNode(node).getConstraint();
		if (constraint.canHaveTextContent()) {
			// Remove old value from IDs.
			if (constraint.hasTypeName() 
					&& constraint.getTypeName().equals("ID")) {
				String oldID = XMLUtility.getNodeValue(node);
				TreeManager.removeID(oldID, node);
				validate(TreeManager.getNodeForID(oldID));
			}
			
			// Change value.
			XMLUtility.setNodeValue(node, newValue);
			
			// Add new value to IDs.
			if (constraint.hasTypeName() 
					&& constraint.getTypeName().equals("ID")) {
				TreeManager.putID(node);
			}
			
			List<EDLRule> relevantRules = this.treeManager.getRelevantRules(node);
			if (relevantRules != null) {
				checkRules(relevantRules);
			}
			validate(node);
			
			logger.info("Changed value of " + node.getNodeName() + " to " + newValue);
		}
	}
	
	public synchronized void replace(final Node xmlToReplace, 
							  		 final Node xmlReplacement) {
		if (xmlToReplace == null
			|| xmlReplacement == null) {
			return;
		}
		
		MetaNode metaNode = this.treeManager.getMetaNode(xmlToReplace);
		Node parent = xmlToReplace.getParentNode();
		Node importedReplacement = this.xmlDocument.importNode(xmlReplacement, true);
		parent.replaceChild(importedReplacement, xmlToReplace);
		
		if (this.treeManager.mapExisting(importedReplacement, metaNode)) {
			DefaultMutableTreeNode treeReplacement = this.treeManager.create(importedReplacement);
			DefaultMutableTreeNode treeParent = this.treeManager.getTreeNode(parent);
			
			DefaultMutableTreeNode treeToReplace = this.treeManager.getTreeNode(xmlToReplace);
			int insertionIndex = treeParent.getIndex(treeToReplace);
			this.treeModel.removeNodeFromParent(treeToReplace);
			
			List<EDLRule> relevantRules = this.treeManager.getRelevantRules(xmlToReplace);
			if (relevantRules != null) {
				checkRules(relevantRules);
			}
			
			this.treeManager.invalidateOptions(parent);
			this.treeManager.destroy(xmlToReplace);
			
			checkRules(this.edlValidator.getRulesMissingParameters(this.treeManager.getRulesUsingNodes()));
			validate(importedReplacement);
			
			this.treeModel.insertNodeInto(treeReplacement, 
  					  					  treeParent, 
  					  					  insertionIndex);
			
		} else {
			// Revert replacing process.
			parent.replaceChild(xmlToReplace, importedReplacement);
		}
	}
	
	void printToStdout() {
		printMetaNode(this.xmlMetaTree, 0);
		printXMLNode(this.xmlDocument.getDocumentElement(), 0);
	}
	
	void printMetaNode(final MetaNode node, final int depth) {
		
		String spaces = "";
		int spaceCounter = 0;
		while (spaceCounter < depth) {
			spaces = spaces + "    ";
			spaceCounter++;
		}
		
		System.out.println(spaces + node);
		
		for (MetaNode child : node.getChildren()) {
			this.printMetaNode(child, depth + 1);
		}
	}
	
	void printXMLNode(final Node node, final int depth) {
		
		short nodeType = node.getNodeType();
		if (nodeType != Node.ATTRIBUTE_NODE
			&& nodeType != Node.ELEMENT_NODE) {
			return;
		}
		
		String spaces = "";
		int spaceCounter = 0;
		while (spaceCounter < depth) {
			spaces = spaces + "    ";
			spaceCounter++;
		}
		
		System.out.println(spaces + node.getNodeName());
		
		NamedNodeMap attributes = node.getAttributes();
		int attrNr = 0;
		while (attrNr < attributes.getLength()) {
			Node attribute = attributes.item(attrNr);
			System.out.println(spaces + "@" + attribute.getNodeName());
			attrNr++;
		}
		
		NodeList children = node.getChildNodes();
		int childNr = 0;
		while (childNr < children.getLength()) {
			this.printXMLNode(children.item(childNr), depth + 1);
			childNr++;
		}
	}
	
	void printAllDistinctBaseTypes() {
		Map<String, String> distinctBaseTypes = new HashMap<String, String>();
		_printAllDistinctBaseTypes(this.xmlMetaTree, distinctBaseTypes);
		
		System.out.println("Distinct types in current EDL spec:");
		for (String type : distinctBaseTypes.values()) {
			System.out.println(" " + type);
		}
	}
	private void _printAllDistinctBaseTypes(final MetaNode metaNode, 
											final Map<String, String> distinctBaseTypes) {
		NodeConstraint constraint = metaNode.getConstraint();
		if (constraint != null
			&& constraint.hasTypeName()) {
			distinctBaseTypes.put(constraint.getTypeName(), constraint.getTypeName());
		}
		
		for (MetaNode attr : metaNode.getAttributes().values()) {
			_printAllDistinctBaseTypes(attr, distinctBaseTypes);
		}
		
		for (MetaNode child : metaNode.getChildren()) {
			_printAllDistinctBaseTypes(child, distinctBaseTypes);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(arg);
	}
}
