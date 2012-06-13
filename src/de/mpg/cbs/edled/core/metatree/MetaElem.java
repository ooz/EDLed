package de.mpg.cbs.edled.core.metatree;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MetaElem implements MetaNode {
	
	private String name = null;
	private NodeConstraint constraint = null;
	private MetaNode parent = null;
	private List<MetaNode> children = null;
	private Map<String, MetaNode> attributes = null;
	
	public MetaElem(final String name, final NodeConstraint constraint) {
		this.name = name;
		this.constraint = constraint;
		this.children = new LinkedList<MetaNode>();
		this.attributes = new LinkedHashMap<String, MetaNode>();
	}

	@Override
	public List<MetaNode> getChildren() {
		return this.children;
	}

	@Override
	public NodeConstraint getConstraint() {
		return this.constraint;
	}

	@Override
	public MetaXMLNodeKind getKind() {
		return MetaXMLNodeKind.ELEMENT;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public MetaNode getParent() {
		return this.parent;
	}

	@Override
	public void add(final MetaNode node) {
		if (!this.children.contains(node)
			&& node.getKind() != MetaXMLNodeKind.ATTRIBUTE) {
			this.children.add(node);
		} else if (!this.attributes.containsKey(node.getName())
				   && node.getKind() == MetaXMLNodeKind.ATTRIBUTE) {
			this.attributes.put(node.getName(), node);
		}
		
		node.setParent(this);
	}

	@Override
	public void remove(final MetaNode node) {
		if (!this.children.contains(node)
			&& node.getKind() != MetaXMLNodeKind.ATTRIBUTE) {
			this.children.remove(node);
		} else if (!this.attributes.containsKey(node.getName())
				   && node.getKind() == MetaXMLNodeKind.ATTRIBUTE) {
			this.attributes.remove(node.getName());
		}
		
		node.setParent(null);
	}

	@Override
	public void setParent(MetaNode parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "MetaXMLElem{name=" + this.name + ", constraint=" + this.constraint +"}";		
	}

	@Override
	public boolean isCompositor() {
		return false;
	}
	
	@Override
	public int getIndex(MetaNode child) {
		return this.children.indexOf(child);
	}

	@Override
	public MetaNode getNextSibling() {
		if (this.parent != null) {
			List<MetaNode> siblings = this.parent.getChildren();
			int indexOfThis = siblings.indexOf(this);
			if (indexOfThis < siblings.size() - 1) {
				return siblings.get(indexOfThis + 1); 
			}
		}
		
		return null;
	}

	@Override
	public MetaNode getPrevSibling() {
		if (this.parent != null) {
			List<MetaNode> siblings = this.parent.getChildren();
			int indexOfThis = siblings.indexOf(this);
			if (indexOfThis > 0) {
				return siblings.get(indexOfThis - 1); 
			}
		}
		
		return null;
	}

	@Override
	public Map<String, MetaNode> getAttributes() {
		return this.attributes;
	}
	
}
