package de.mpg.cbs.edled.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.core.metatree.MetaNode;
import de.mpg.cbs.edled.core.metatree.NodeConstraint;
import de.mpg.cbs.edled.core.metatree.MetaNode.MetaXMLNodeKind;
import de.mpg.cbs.edled.core.metatree.NodeConstraint.AttributeUse;
import de.mpg.cbs.edled.core.validation.EDLRule;
import de.mpg.cbs.edled.core.validation.ValidationResult;
import de.mpg.cbs.edled.xml.XMLUtility;



/**
 * A TreeManager manages different tree representations of the same XML document 
 * (like the meta tree, the XML document itself or the model needed for a JTree).
 * 
 * @author Oliver Z.
 */
public class TreeManager {
	
	private static final Logger LOG = Logger.getLogger(TreeManager.class);
	
	/** Document used for node creation. */
	private Document xmlDocument = null;
	
	/** Mapping DOM nodes to the meta tree. */
	private Map<Node, MetaNode> xmlNodeToMetaXMLNode = null;
	/** Mapping DOM nodes to the model for the JTree. */
	private Map<Node, DefaultMutableTreeNode> xmlNodeToTreeNode = null;
	
	/** Map for caching manipulation options for each node. */
	private Map<Node, List<ManipulationOption>> manipulationOptions = null;
	
	/** Map for caching validation results for each node. */
	private Map<Node, ValidationResult> validationResults = null;
	/** Map for storing the relevant rules for each node. */
	private Map<Node, List<EDLRule>> relevantRules = null;
	/** Map for storing the relevant nodes for each rule. */
	private Map<EDLRule, List<Node>> rulesUsingNodes = null;
	
	/** 
	 * Map for storing used IDs.
	 * The first node in the list is the node which holds the ID.
	 * The following nodes are candidates for the ID if the first node gets a different ID
	 * or gets deleted.
	 */
	private static final Map<String, List<Node>> nodeIDs = new HashMap<String, List<Node>>();
	
	/** Attributes that are whitelisted (meaning they will be just taken "as is" without validation). */
	private List<String> attributeWhitelist = new LinkedList<String>();
	
	/**
	 * Constructor.
	 * 
	 * @param xmlDocument The XML document that should be managed.
	 */
	TreeManager(final Document xmlDocument) {
		this.xmlDocument = xmlDocument;
		
		this.xmlNodeToMetaXMLNode = new HashMap<Node, MetaNode>();
		this.xmlNodeToTreeNode = new HashMap<Node, DefaultMutableTreeNode>();
		
		this.manipulationOptions = new HashMap<Node, List<ManipulationOption>>();
		
		this.validationResults = new HashMap<Node, ValidationResult>();
		this.relevantRules = new HashMap<Node, List<EDLRule>>();
		this.rulesUsingNodes = new HashMap<EDLRule, List<Node>>();
		
		this.attributeWhitelist.add("xmlns:xsi");
		this.attributeWhitelist.add("xsi:noNamespaceSchemaLocation");
		
		TreeManager.nodeIDs.clear();
	}
	
	/**
	 * Returns the MetaNode object for a given DOM node.
	 * 
	 * @param node A Node object.
	 * @return	   MetaNode for the given node. Null if it does not exist.
	 */
	public MetaNode getMetaNode(final Node node) {
		return this.xmlNodeToMetaXMLNode.get(node);
	}
	/**
	 * Returns the DefaultMutableTreeNode (part of the JTree model) for a given DOM node.
	 * 
	 * @param node A Node object.
	 * @return	   DefaultMutableTreeNode representing the given node in the JTree model.
	 */
	public DefaultMutableTreeNode getTreeNode(final Node node) {
		return this.xmlNodeToTreeNode.get(node);
	}
	/**
	 * Returns all cached manipulation options for a DOM node.
	 * 
	 * @param node A Node object.
	 * @return	   All cached manipulation options for the given node. 
	 * 			   Null if no options are cached.
	 */
	synchronized public List<ManipulationOption> getOptions(final Node node) {
//		if (this.manipulationOptions.containsKey(node)) {
			return this.manipulationOptions.get(node);
//		} else {
//			return null;
//		}
	}
	/**
	 * Returns the cached ValidationResult for a DOM node.
	 * 
	 * @param node A Node object.
	 * @return	   The cached ValidationResult for the given node. 
	 * 			   Null if no ValidationResult is cached. 
	 */
	synchronized public ValidationResult getValidationResult(final Node node) {
		return this.validationResults.get(node);
	}
	/**
	 * Returns all EDL rules that a relevant for a given node (use the node
	 * as a parameter).
	 * 
	 * @param node A Node object.
	 * @return	   All relevant rules for the given node. Null if none is relevant.
	 */
	synchronized public List<EDLRule> getRelevantRules(final Node node) {
		return this.relevantRules.get(node);
	}
	synchronized public Map<EDLRule, List<Node>> getRulesUsingNodes() {
		return new HashMap<EDLRule, List<Node>>(this.rulesUsingNodes);
	}

	public List<MetaNode> getNonCompDirectDescendants(final MetaNode metaNode) {
		List<MetaNode> nonCompDirectDescendants = new LinkedList<MetaNode>();
		
		for (MetaNode child : metaNode.getChildren()) {
			switch(child.getKind()) {
			case ATTRIBUTE:
				break;
			case ELEMENT:
				nonCompDirectDescendants.add(child);
				break;
			case CHOICE_COMPOSITOR:
			case GROUP_COMPOSITOR:
			case SEQUENCE_COMPOSITOR:
				nonCompDirectDescendants.addAll(getNonCompDirectDescendants(child));
				break;
			default:
				throw new RuntimeException("getNonCompLeaves: child has unspecified node kind!");
			}
		}
		
		return nonCompDirectDescendants;
	}

	/**
	 * Puts a Node-MetaXMLNode association into the manager.
	 * ATTENTION: You need to make sure, that all child elements and 
	 * attributes of node are also mapped by this method! This method
	 * DOES NOT map child elements or attributes of node automatically! 
	 * 
	 * @param node	   Node that acts as the mapping key.
	 * @param metaNode MetaXMLNode that represents the mapped value.
	 * @return		   The previous MetaXMLNode that was mapped to node,
	 * 				   or null if there was no such mapped value.
	 */
	private MetaNode put(final Node node, final MetaNode metaNode) {
		NodeConstraint constraint = metaNode.getConstraint();
		if (constraint != null) {
			if (constraint.hasTypeName() && constraint.getTypeName().equals("ID")) {
				TreeManager.putID(node);
			}
		}
		
		return this.xmlNodeToMetaXMLNode.put(node, metaNode);
	}
	private DefaultMutableTreeNode put(final Node node, final DefaultMutableTreeNode treeNode) {
		return this.xmlNodeToTreeNode.put(node, treeNode);
	}
	
	synchronized List<ManipulationOption> put(final Node node, final List<ManipulationOption> options) {
		return this.manipulationOptions.put(node, options);
	}
	synchronized ValidationResult put(final Node node, final ValidationResult validationResult) {
		return this.validationResults.put(node, validationResult);
	}
	synchronized List<EDLRule> putRelevantRules(final Node node, final List<EDLRule> relevantRules) {
		return this.relevantRules.put(node, relevantRules);
	}
	synchronized List<Node> put(final EDLRule rule, final List<Node> usedNodes) {
		return this.rulesUsingNodes.put(rule, usedNodes);
	}
	public static void putID(final Node node) {
//		short nodeType = node.getNodeType();
		String id = XMLUtility.getNodeValue(node);
		if (id == null 
				|| id.isEmpty()
				//|| (nodeType != Node.ATTRIBUTE_NODE && nodeType != Node.ELEMENT_NODE)
				) {
			return;
		}
		
		if (TreeManager.nodeIDs.containsKey(id)) {
			List<Node> idApplicants = TreeManager.nodeIDs.get(id);
			if (!idApplicants.contains(node)) {
				LOG.trace("Put ID applicant: " + id + " node: " + node.hashCode());
				idApplicants.add(node);
				LOG.trace(TreeManager.nodeIDs);
			}
		} else {
			LOG.trace("Put ID: " + id + " node: " + node.hashCode());
			List<Node> idQueue = new LinkedList<Node>();
			idQueue.add(node);
			TreeManager.nodeIDs.put(id, idQueue);
			LOG.trace(TreeManager.nodeIDs);
		}
	}
	public static void removeID(final Node node) {
		TreeManager.removeID(XMLUtility.getNodeValue(node), node);
	}
	public static void removeID(final String id, final Node node) {
//		short nodeType = node.getNodeType();
//		if (nodeType != Node.ATTRIBUTE_NODE && nodeType != Node.ELEMENT_NODE) {
//			return;
//		}
		
		if (TreeManager.nodeIDs.containsKey(id)) {
			List<Node> nodesWithID = TreeManager.nodeIDs.get(id);
			nodesWithID.remove(node);
//			System.out.println("Remove ID: " + id + " node: " + node.hashCode());
//			System.out.println(TreeManager.nodeIDs);
			if (nodesWithID.isEmpty()) {
				TreeManager.nodeIDs.remove(id);
			}
		}
	}
//	public static boolean isIDinUse(final String id) {
//		if (TreeManager.nodeIDs.containsKey(id)) {
//			return true;
//		}
//		
//		return false;
//	}
	public static Node getNodeForID(final String id) {
		if (TreeManager.nodeIDs.containsKey(id)) {
			return TreeManager.nodeIDs.get(id).get(0);
		}
		
		return null;
	}
	
	public boolean mapExisting(final Node xmlNode, final MetaNode metaNode) {
		if (xmlNode == null
			|| metaNode == null) {
			LOG.trace("couldn't map, because at least one parameter null");
			return false;
		}
		
		if (metaNode.getName().compareTo(xmlNode.getNodeName()) != 0) {
			LOG.trace("couldn't map, because names dont match (xml!=meta): " + xmlNode.getNodeName() + "!=" + metaNode.getName());
			return false;
		}
		
		if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {
			Element xmlElem = (Element) xmlNode;
			NodeConstraint constraint = metaNode.getConstraint();
			if (metaNode.getKind() == MetaXMLNodeKind.ELEMENT) {
				int occurs = XMLUtility.getOccurs(xmlElem);
				int minOccurs = constraint.getMinOccurs();
				int maxOccurs = constraint.getMaxOccurs();
				if (occurs < minOccurs
					|| (occurs > maxOccurs && maxOccurs != NodeConstraint.UNBOUNDED)) {
					LOG.trace("couldn't, because occurrences are unvalid");
					return false;
				}
				
				// Check whether attributes confirm.
				// TODO: ignore following attributes/namespaces:
				NamedNodeMap attributes = xmlElem.getAttributes();
				Map<String, MetaNode> metaAttributes = metaNode.getAttributes();
				int attrNr = 0;
				while (attrNr < attributes.getLength()) {
					Attr attr = (Attr) attributes.item(attrNr);
					MetaNode metaAttr = metaAttributes.get(attr.getNodeName());
					if (metaAttr != null) {
						put(attr, metaAttr);
						// Add attribute value to IDs.
						NodeConstraint attrConstraint = metaAttr.getConstraint();
						if (attrConstraint != null 
							&& attrConstraint.hasTypeName() 
							&& attrConstraint.getTypeName().equals("ID")) {
							TreeManager.putID(attr);
						}
					} else if (!this.attributeWhitelist.contains(attr.getNodeName())) {
//						xmlElem.removeAttributeNode(attr);
						destroy(xmlNode);
						LOG.trace("Couldn't map, because unexspected attr " + attr.getNodeName() + " found");
						return false;
					}
					attrNr++;
				}
				
				// Check whether children of xmlNode confirm.
				NodeList children = xmlElem.getChildNodes();
				int childNr = 0;
				Node child = children.item(childNr);
				List<MetaNode> descendants = getNonCompDirectDescendants(metaNode);
				int descendantNr = 0;
				// Working around the IndexOutOfBoundsException: descendant is null when descendantNr is out of bounds.
				MetaNode descendant = (descendantNr < descendants.size()) ? descendants.get(descendantNr) : null;
				
				while (child != null
					   && descendant != null) {
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						
						if (mapExisting(child, descendant)) {
							// Map additional occurrences of element child.
							do {
								childNr++;
								child = children.item(childNr);
								// Skip whitespace.
								while (child != null
									   && child.getNodeType() != Node.ELEMENT_NODE) {
									childNr++;
									child = children.item(childNr);
								}
							
							} while (mapExisting(child, descendant));
							
							// skip additional choice options
							MetaNode descendantParent = descendant.getParent();
							if (descendantParent.getKind() == MetaXMLNodeKind.CHOICE_COMPOSITOR) {
								List<MetaNode> choiceAlternatives = descendantParent.getChildren();
								while (descendant != null
									   && choiceAlternatives.contains(descendant)) {
									descendantNr++;
									descendant = (descendantNr < descendants.size()) ? descendants.get(descendantNr) : null;
								}
							} else {
								descendantNr++;
								descendant = (descendantNr < descendants.size()) ? descendants.get(descendantNr) : null;
							}
						} else {
							NodeConstraint descConstraint = descendant.getConstraint();
							if (descConstraint.getMinOccurs() != 0
								&& descendant.getParent().getKind() != MetaXMLNodeKind.CHOICE_COMPOSITOR) {
								destroy(xmlElem);
								LOG.trace("couldn't map, because desc is not optional");
								return false;
							}
							descendantNr++;
							descendant = (descendantNr < descendants.size()) ? descendants.get(descendantNr) : null;
						}
					} else {
						childNr++;
						child = children.item(childNr);
					}
				}
				
				// Evaluate
				if (child == null
					&& descendant != null) {
					// Check whether remaining descendants are optional (read: either unused choice options or have minOccurs == 0).
					while (descendant != null) {
						NodeConstraint descConstraint = descendant.getConstraint();
						if (descConstraint.getMinOccurs() != 0) {
							destroy(xmlElem);
							LOG.trace("couldn't map, because remaining descs are not optional");
							return false;
						}
						descendantNr++;
						descendant = (descendantNr < descendants.size()) ? descendants.get(descendantNr) : null;
					}
					
				} else if (child != null
						   && descendant == null) {
					// If there are unmapped children left: return false!
					while (child != null) {
						if (child.getNodeType() == Node.ELEMENT_NODE) {
							destroy(xmlElem);
							LOG.trace("couldn't map, because unmapped children is left: " + child.getNodeName());
							return false;
						}
						
						childNr++;
						child = children.item(childNr);
					}
				}
			}
			
			put(xmlElem, metaNode);
			// Add value to IDs.
			if (constraint.hasTypeName() 
					&& constraint.getTypeName().equals("ID")) {
				TreeManager.putID(xmlElem);
			}
			
			LOG.trace("mapped " + xmlElem.getNodeName() + " to " + metaNode.getName());
			return true;
		} else {
			destroy(xmlNode);
			LOG.trace("couldn't map, because tried to map something other than a element: " + xmlNode.getNodeName());
			return false;
		}
	}
	
	public boolean isWhitelisted(final String nodename) {
		return this.attributeWhitelist.contains(nodename);
	}
	
	/**
	 * 
	 * @param metaNode
	 * @return
	 */
	public Node create(final MetaNode metaNode) {
		switch (metaNode.getKind()) {
		case ATTRIBUTE:
		case ELEMENT:
			return createXMLTree(metaNode, true).get(0);
		default:
			return null;
		}
	}
	
	/**
	 * Constructs a tree of DOM Node objects from a tree of MetaXMLNode objects.
	 * 
	 * @param metaNode         The MetaXMLNode which should be represented as a DOM tree.
	 * @param isTopNode 	   Flag indicating whether metaNode represents the top node (root node of the 
	 * 						   (sub-)tree that should be created, that needn't be the root node of a XML document).
	 * @return				   A list containing all the DOM Node objects that could be constructed based on
	 * 						   the given metaNode. Contains only 1 Node if metaNode represented an attribute or
	 * 						   element. May contain zero or more elements if metaNode represents a compositor
	 * 						   (sequence, choice, group, all).
	 */
	private List<Node> createXMLTree(final MetaNode metaNode, 
									 final boolean isTopNode) {
		
		List<Node> xmlNodes = new LinkedList<Node>();
		
		NodeConstraint nodeConstraint = metaNode.getConstraint();
		
//		if (nodeConstraint.getMinOccurs() > 0
//			|| isTopNode) {
			
		MetaXMLNodeKind metaChildKind = metaNode.getKind();
		switch (metaChildKind) {
		case ATTRIBUTE:
			if ((nodeConstraint.hasAttributeUse() 
					&& nodeConstraint.getAttributeUse() == AttributeUse.REQUIRED)
				|| isTopNode
				|| nodeConstraint.hasDefaultValue()
				|| nodeConstraint.hasFixedValue()) {
				Attr attr = this.xmlDocument.createAttribute(metaNode.getName());
				if (nodeConstraint.hasDefaultValue()) {
					attr.setNodeValue(nodeConstraint.getDefaultValue());
				} else if (nodeConstraint.hasFixedValue()) {
					attr.setNodeValue(nodeConstraint.getFixedValue());
				}
				this.put(attr, metaNode);
				xmlNodes.add(attr);
			}
			break;
		case ELEMENT:
			int occurs = 0;
			boolean doAtLeastOnce = isTopNode;
			while (occurs < nodeConstraint.getMinOccurs()
				   || doAtLeastOnce) {
				Element elem = this.xmlDocument.createElement(metaNode.getName());
				if (nodeConstraint.hasDefaultValue()) {
					XMLUtility.setNodeValue(elem, nodeConstraint.getDefaultValue());
				} 
				if (nodeConstraint.hasFixedValue()) {
					XMLUtility.setNodeValue(elem, nodeConstraint.getFixedValue());
				}
				this.put(elem, metaNode);
				xmlNodes.add(elem);
				
				List<Node> tmpXMLChildren = new LinkedList<Node>();
				
				for (MetaNode metaChild : metaNode.getChildren()) {
					tmpXMLChildren.addAll(createXMLTree(metaChild, false));
				}
				for (MetaNode metaAttr : metaNode.getAttributes().values()) {
					tmpXMLChildren.addAll(createXMLTree(metaAttr, false));
				}
				
				for (Node xmlChild : tmpXMLChildren) {
					short xmlChildKind = xmlChild.getNodeType();
					if (xmlChildKind == Node.ATTRIBUTE_NODE) {
						elem.setAttributeNode((Attr) xmlChild);
					} else if (xmlChildKind == Node.ELEMENT_NODE) {
						elem.appendChild(xmlChild);
					}
				}

				occurs++;
				doAtLeastOnce = false;
			}
			break;
		case CHOICE_COMPOSITOR:
			xmlNodes.addAll(createXMLTree(metaNode.getChildren().get(0), false));
			break;
		case GROUP_COMPOSITOR:
			break;
		case SEQUENCE_COMPOSITOR:
			for (MetaNode metaChild : metaNode.getChildren()) {
				xmlNodes.addAll((createXMLTree(metaChild, false)));
			}
			break;
		default:
			// TODO: implement ALL_COMPOSITOR
			// error&explosions
			break;
		}
//		}
		
		return xmlNodes;
	}
	
	public DefaultMutableTreeNode create(final Node xmlNode) {
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(xmlNode);
		this.put(xmlNode, treeNode);
		
		NodeList children = xmlNode.getChildNodes();
		int childNr = 0;
		while (childNr < children.getLength()) {
			
			Node childXMLNode = children.item(childNr);
			
			short childNodeKind = childXMLNode.getNodeType();
			if (childNodeKind == Node.ELEMENT_NODE) {
			
//				List<Node> nodeChoices = this.nodeConstraints.get(childXMLNode).getNodesInChoicePool();
//				if (nodeChoices == null) {
//					treeNode.add(this.buildTree(childXMLNode));
//				} else {
//					if (nodeChoices.get(0).getNodeName().compareTo(childXMLNode.getNodeName()) == 0) {
//						treeNode.add(this.buildTree(childXMLNode));
//					}
//				}
				treeNode.add(create(childXMLNode));
			}
			
			childNr++;
		}
		return treeNode;
	}
	
	/**
	 * Removes a XML node as well as all of its child nodes and attributes
	 * for the manager.
	 * 
	 * @param node The XML node which should be removed recursively from the mapper.
	 */
	public void destroy(final Node node) {
		_destroy(node, false);
	}
	public void invalidateOptions(final Node node) {
		_destroy(node, true);
	}
	private void _destroy(final Node node, boolean optionsOnly) {
		
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			int childNr = 0;
			while (childNr < children.getLength()) {
				_destroy(children.item(childNr), optionsOnly);
				childNr++;
			}
		}
		
		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			int attrNr = 0;
			while (attrNr < attrs.getLength()) {
				_destroy(attrs.item(attrNr), optionsOnly);
				attrNr++;
			}
		}
		
		if (!optionsOnly) {
			this.xmlNodeToMetaXMLNode.remove(node);
			this.xmlNodeToTreeNode.remove(node);
			
			this.validationResults.remove(node);
			removeRuleEntries(node);
			
			TreeManager.removeID(node);
		}
		
		this.manipulationOptions.remove(node);
	}
	
	private void removeRuleEntries(final Node node) {
		List<EDLRule> relevantRulesForNode = this.relevantRules.get(node);
		if (relevantRulesForNode != null) {
			for (EDLRule rule : relevantRulesForNode) {
				this.rulesUsingNodes.get(rule).remove(node);
			}
		}
		this.relevantRules.remove(node);
	}
}
