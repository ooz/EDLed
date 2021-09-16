package de.mpg.cbs.edled.core;

/**
 * Interface for all manipulation options that can be executed on
 * the model.
 * 
 * @author Oliver Z.
 */
public interface ManipulationOption {
	
	/**
	 * Enum indicating the type of the option without the need to
	 * use reflections.
	 */
	public enum ManipulationOptionKind {
		ADD_ADDITIONAL,
		ADD_CHILD,
		ADD_ATTRIBUTE,
		REMOVE,
		REMOVE_ATTRIBUTE,
		CHOICE;
	}
	
	/**
	 * Returns the name of the node being subject to manipulation,
	 * e.g. the node that is about to be deleted/added.
	 * 
	 * @return The node name.
	 */
	public String getObjectiveNodeName();
	
	/**
	 * Returns a short text of what the option does when executed.
	 * May be used for display in GUIs.
	 * 
	 * @return The description of the option.
	 */
	public String getOptionDescription();
	
	/**
	 * Returns the ManipulationOptionKind.
	 * 
	 * @return The kind of the option.
	 */
	public ManipulationOptionKind getKind();
	
	/**
	 * Executes the option on the model.
	 */
	public void execute();

}
