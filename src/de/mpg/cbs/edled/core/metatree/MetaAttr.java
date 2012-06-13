package de.mpg.cbs.edled.core.metatree;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MetaAttr implements MetaNode {
	
	private String name = null;
	private NodeConstraint constraint = null;
	private MetaNode parent = null;
	
	public MetaAttr(final String name, final NodeConstraint constraint) {
		this.name = name;
		this.constraint = constraint;
	}

	@Override
	public List<MetaNode> getChildren() {
		return new LinkedList<MetaNode>();
	}

	@Override
	public NodeConstraint getConstraint() {
		return this.constraint;
	}

	@Override
	public MetaXMLNodeKind getKind() {
		return MetaXMLNodeKind.ATTRIBUTE;
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
		return;
	}

	@Override
	public void remove(final MetaNode node) {
		return;
	}

	@Override
	public void setParent(MetaNode parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return "MetaXMLAttr{name=" + this.name + ", constraint=" + this.constraint +"}";		
	}

	@Override
	public boolean isCompositor() {
		return false;
	}
	
	@Override
	public int getIndex(MetaNode child) {
		return -1;
	}

	@Override
	public MetaNode getNextSibling() {
		return null;
	}

	@Override
	public MetaNode getPrevSibling() {
		return null;
	}

	@Override
	public Map<String, MetaNode> getAttributes() {
		return new LinkedHashMap<String, MetaNode>();
	}

}
