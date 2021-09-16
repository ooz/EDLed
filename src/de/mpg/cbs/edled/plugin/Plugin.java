package de.mpg.cbs.edled.plugin;

import javax.swing.JPanel;

import de.mpg.cbs.edled.Application;
import de.mpg.cbs.edled.core.Model;



/**
 * Interface that needs to be implemented by EDLed plugins.
 * Each plugin is granted a tab in the main application window. This tab displays
 * the plugin view.
 * 
 * A plugin gets its data via the update method and provides its data to the application
 * in form of a ReplacementManager.
 * 
 * @author Oliver Z.
 */
public interface Plugin {
	
	/**
	 * Initializes the plugin with the main application controller.
	 * 
	 * @param appController The application controller that the plugin should
	 * 						communicate with.
	 */
	public void initAppController(final Application appController);
	
	/**
	 * Returns the unqualified name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName();
	/**
	 * Returns the qualified name of the plugin.
	 * 
	 * @return The qualified name of the class implementing this interface.
	 */
	public String getQualifiedName();
	/**
	 * Returns the plugin author's name.
	 * 
	 * @return The plugin author's name.
	 */
	public String getAuthor();
	/**
	 * Returns a short description of the plugin functionality/purpose.
	 * 
	 * @return A short plugin discription.
	 */
	public String getDescription();
	/**
	 * Returns the version number string of the plugin (i.e. 1.0; 2.3.1b).
	 * 
	 * @return The version number of the plugin in string form.
	 */
	public String getVersion();
	
	/**
	 * Returns the plugin view.
	 * 
	 * @return The plugin view.
	 */
	public JPanel getView();
	
	/**
	 * Updates the plugin model with data from a DOMTree representing 
	 * the main application model.
	 * 
	 * @param model The application's main data structure.
	 */
	public void update(final Model model);
	
	/**
	 * Returns the ReplacementManager used by the plugin.
	 * 
	 * @param updateManager Flag that indicates whether the plugin has to
	 * 					    update the manager from its internal model before
	 * 					    returning the manager.
	 * @return              The plugin's ReplacementManager.
	 */
	public ReplacementManager getReplacementManager(final boolean updateManager);

}
