package de.mpg.cbs.edled.core;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.metatree.MetaAttr;
import de.mpg.cbs.edled.core.metatree.MetaNode;


public class ManipulationAddOption implements ManipulationOption {
	
//	public static final String ADD_ADDITIONAL_MSG = "Add additional"; // + "\"<elemname>\""
//	public static final String ADD_CHILD_MSG = ""; // + "\"<elemname>\""
	
	private String description;
	private final String objectiveNodeName;
	private ManipulationOptionKind kind;
	
	private final Model model;
	
	private final MetaNode metaNode;
	private final Node xmlParent;
	private Node xmlPrevSibling;
	
	public ManipulationAddOption(final Model model,
								 final MetaAttr metaAttr,
								 final Element elem) {
		this.model = model;
		this.metaNode = metaAttr;
		this.xmlParent = elem;
		this.kind = ManipulationOptionKind.ADD_ATTRIBUTE;
		
		this.objectiveNodeName = metaAttr.getName();
		this.description = "Add attribute \"" + this.objectiveNodeName + "\"";
	}
	
	ManipulationAddOption(final Model model,
						  final MetaNode metaNode,
						  final Node xmlParent,
						  final Node xmlPrevSibling,
						  final ManipulationOptionKind kind) {
		
		this.model = model;
		this.metaNode = metaNode;
		this.xmlParent = xmlParent;
		this.xmlPrevSibling = xmlPrevSibling;
		
		this.objectiveNodeName = metaNode.getName();
		
		switch (kind) {
		case ADD_ADDITIONAL:
			this.kind = kind;
			this.description = "Add additional \"" + this.objectiveNodeName + "\"";
			break;
		case ADD_CHILD:
			this.kind = kind;
			this.description = "Add child node \"" + this.objectiveNodeName + "\"";
			break;
		default:
			throw new RuntimeException("ManipulationAddOption created with false kind: " + kind);
		}
	}

	@Override
	public void execute() {
		if (this.kind == ManipulationOptionKind.ADD_ATTRIBUTE) {
			this.model.addAttributeLike((MetaAttr) this.metaNode, 
										(Element) this.xmlParent);
		} else {
			this.model.addNodeLike(this.metaNode, 
					   			   this.xmlParent, 
					   			   this.xmlPrevSibling);
		}
	}
	
	@Override
	public String getObjectiveNodeName() {
		return this.objectiveNodeName;
	}

	@Override
	public String getOptionDescription() {
		return this.description;
	}

	@Override
	public ManipulationOptionKind getKind() {
		return this.kind;
	}

}
