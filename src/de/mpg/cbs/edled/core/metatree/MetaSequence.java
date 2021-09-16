package de.mpg.cbs.edled.core.metatree;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MetaSequence implements MetaNode {
	
	private NodeConstraint constraint = null;
	private MetaNode parent = null;
	private List<MetaNode> children = null;
	
	public MetaSequence(final NodeConstraint constraint) {
		this.constraint = constraint;
		this.children = new LinkedList<MetaNode>();
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
		return MetaXMLNodeKind.SEQUENCE_COMPOSITOR;
	}

	@Override
	public boolean isCompositor() {
		return true;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public MetaNode getParent() {
		return this.parent;
	}
	
	@Override
	public void setParent(final MetaNode parent) {
		this.parent = parent;
	}

	@Override
	public void add(final MetaNode node) {
		if (!this.children.contains(node)
			&& node.getKind() != MetaXMLNodeKind.ATTRIBUTE) {
			this.children.add(node);
			node.setParent(this);
		}
	}

	@Override
	public void remove(final MetaNode node) {
		if (node.getKind() != MetaXMLNodeKind.ATTRIBUTE) {
			this.children.remove(node);
			node.setParent(null);
		}
	}
	
	@Override
	public String toString() {
		return "MetaXMLSequence{constraint=" + this.constraint +"}";		
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
		return new LinkedHashMap<String, MetaNode>();
	}
	
}
