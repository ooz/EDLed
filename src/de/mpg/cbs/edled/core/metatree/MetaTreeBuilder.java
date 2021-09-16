package de.mpg.cbs.edled.core.metatree;



/**
 * The MetaTreeBuilder interface is used for abstraction of a XML schema.
 * It generates a meta tree (MetaNode objects) that allows efficient retrieval
 * of manipulation options.
 * 
 * @author Oliver Z.
 */
public interface MetaTreeBuilder {
	
	/**
	 * Recursively builds the meta tree for a given element.
	 * The concrete builder must be initialized with a XML schema beforehand.
	 * 
	 * @param elemName Name of the element to create the meta tree for.
	 * @return		   Meta tree representing the element.
	 */
	public MetaNode buildMetaXMLTree(final String elemName);

}
