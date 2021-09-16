package de.mpg.cbs.edled.core.metatree;

import java.util.List;
import java.util.Map;


/**
 * Interface for all nodes in the meta tree.
 * 
 * @author Oliver Z.
 */
public interface MetaNode {
	
	/** Enum signaling the kind of the node. */
	public enum MetaXMLNodeKind {
		ELEMENT,
		ATTRIBUTE,
		GROUP_COMPOSITOR,
		SEQUENCE_COMPOSITOR,
		CHOICE_COMPOSITOR;
//		ANY_COMPOSITOR;
	}
	
	/**
	 * Returns the specific node kind.
	 * 
	 * @return Kind of the node object.
	 */
	public MetaXMLNodeKind getKind();
	/**
	 * Indicates whether the node is a compositor (sequence, choice, group).
	 * 
	 * @return True if the node represents a compositor. False otherwise.
	 */
	public boolean isCompositor();
	
	/**
	 * Returns the node name.
	 * 
	 * @return The node name. Should return null if no node name is supported.
	 */
	public String getName();
	
	/**
	 * Returns the associated NodeConstraint.
	 * 
	 * @return The associated NodeConstraint.
	 */
	public NodeConstraint getConstraint();
	
	/**
	 * Returns the parent node of the meta node.
	 * 
	 * @return The node parent.
	 */
	public MetaNode getParent();
	/**
	 * Parent node setter.
	 * 
	 * @param parent The new parent MetaNode to set.
	 */
	public void setParent(final MetaNode parent);
	
	/**
	 * Returns the next sibling of this MetaXMLNode.
	 * 
	 * @return The next sibling of this MetaXMLNode. Returns null 
	 * 		   if it does not exist.
	 */
	public MetaNode getNextSibling();
	/**
	 * Returns the previous sibling of this MetaXMLNode.
	 * 
	 * @return The previous sibling of this MetaXMLNode. Returns null 
	 * 		   if it does not exist.
	 */
	public MetaNode getPrevSibling();
	
	/**
	 * Returns all children of the node.
	 * 
	 * @return The node children in a list.
	 * 		   Should return an empty list if no children are supported (not null!)
	 */
	public List<MetaNode> getChildren();
	/**
	 * Returns the index of child in the children list of this
	 * MetaXMLNode.
	 * 
	 * @param child The MetaXMLNode whose index in the child list
	 * 			    is requested.
	 * @return		The index of child in the children list. The value 
	 * 				has to be between (including) 0 and (n - 1) where n is the
	 * 				number of children. Has to be -1 if child is not a children
	 * 				of this MetaXMLNode.
	 */
	public int getIndex(final MetaNode child);
	
	/**
	 * Addition of child nodes AND attributes.
	 * 
	 * @param node
	 */
	public void add(final MetaNode node);
	
	/**
	 * Removal of child nodes AND attributes.
	 * @param node
	 */
	public void remove(final MetaNode node);
	
	/**
	 * Must return an empty map if the MetaXMLNode implementation does not 
	 * support attributes.
	 * 
	 * The iteration ordering of the map entries has to be constant (insertion ordering).
	 * 
	 * @return A map containing all attribute meta nodes keyed with their node names.
	 * 		   Should return an empty map if no attributes are supported (not null!)
	 */
	public Map<String, MetaNode> getAttributes();
	
//	/**
//	 * Returns all leaves (MetaXMLAttr and MetaXMLElement objects) of this
//	 * MetaXMLNode and all of its children.
//	 * 
//	 * @return
//	 */
//	public List<MetaXMLNode> getLeaves();

}
