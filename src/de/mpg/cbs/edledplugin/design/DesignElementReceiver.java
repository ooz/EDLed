package de.mpg.cbs.edledplugin.design;

import de.mpg.cbs.edledplugin.design.bart.DesignElement;

/**
 * Interface that defines a method to receive a DesignElement (model object).
 * 
 * @author Oliver Z.
 */
public interface DesignElementReceiver {

	/**
	 * Register a new DesignElement (model object).
	 * Old objects should be discarded and observers reregistered. 
	 * 
	 * @param design DesignElement to register.
	 */
	public void register(final DesignElement design);

}
