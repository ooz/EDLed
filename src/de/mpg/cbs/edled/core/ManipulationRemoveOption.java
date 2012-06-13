package de.mpg.cbs.edled.core;

import org.w3c.dom.Node;

public class ManipulationRemoveOption implements ManipulationOption {
	
	private String description = "";
	private final String objectiveNodeName;
	private final ManipulationOptionKind kind;
	
	private Model model;
	private Node xmlNodeToRemove;

	ManipulationRemoveOption(Model model,
							 Node xmlNodeToRemove) {
		this.model = model;
		this.xmlNodeToRemove = xmlNodeToRemove;
		this.objectiveNodeName = xmlNodeToRemove.getNodeName();
		
		if (xmlNodeToRemove.getNodeType() == Node.ATTRIBUTE_NODE) {
			this.kind = ManipulationOptionKind.REMOVE_ATTRIBUTE;
			this.description = "Remove attribute \"" + this.objectiveNodeName + "\"";
		} else {
			this.kind = ManipulationOptionKind.REMOVE;
			this.description = "Remove this \"" + this.objectiveNodeName + "\"";
		}
	}
	
	@Override
	public void execute() {
		this.model.removeNode(this.xmlNodeToRemove);
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
