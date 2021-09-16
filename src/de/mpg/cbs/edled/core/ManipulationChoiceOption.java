package de.mpg.cbs.edled.core;

import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.metatree.MetaNode;


public class ManipulationChoiceOption implements ManipulationOption {
	
	private String description = "";
	private final String objectiveNodeName;
	
	private Model model;
	
	private MetaNode metaReplacement;
	private Node xmlParent;
	private Node xmlPrevSibling;
	private Node xmlToReplace;
	
	public ManipulationChoiceOption(Model model,
									MetaNode metaReplacement,
									Node xmlParent,
									Node xmlPrevSibling,
									Node xmlToReplace) {
		this.model = model;
		this.metaReplacement = metaReplacement;
		this.xmlParent = xmlParent;
		this.xmlPrevSibling = xmlPrevSibling;
		this.xmlToReplace = xmlToReplace;
		
		this.objectiveNodeName = metaReplacement.getName();
		
		this.description = "Choose alternative \"" + this.objectiveNodeName + "\"";
	}

	@Override
	public void execute() {
		this.model.chooseAlternative(this.metaReplacement, 
									 this.xmlParent, 
									 this.xmlPrevSibling, 
									 this.xmlToReplace);
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
		return ManipulationOptionKind.CHOICE;
	}

}
