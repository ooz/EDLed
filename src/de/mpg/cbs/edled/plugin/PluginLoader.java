package de.mpg.cbs.edled.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import de.mpg.cbs.edled.Application;
import de.mpg.cbs.edled.util.Configuration;


/**
 * Factory class for plugins.
 * 
 * @author Oliver Z.
 */
public class PluginLoader {
	
	/**  */
	private static final Logger LOGGER = Logger.getLogger(PluginLoader.class);

	/** Private default constructor. */
	private PluginLoader() {
	}
	
	/**
	 * Static instantiation of a PluginLoader object.
	 * 
	 * @return A new PluginLoader.
	 */
	public static PluginLoader newInstance() {
		return new PluginLoader();
	}
	
	/**
	 * Dynamically loads a plugin from the plugin directory (specified in the
	 * app config).
	 * 
	 * @param appController The application root controller that is passed to
	 * 						the plugin inorder to get access to the application's
	 * 						data and operations.
	 * @param qualifiedName Qualified name of the plugin class to load.
	 * @return				Plugin object of class qualifiedName. Null if no
	 * 						plugin named qualifiedName could be found or other
	 * 						errors occured. 
	 */
	public Plugin createPlugin(final Application appController,
							   final String qualifiedName) {
		
		Configuration config = Configuration.getInstance();
		String path = config.resolveVariables("$PLUGIN_DIR") 
					  + Configuration.FILE_SEPARATOR 
					  + qualifiedName + ".jar";
		File pluginFile = new File(path);
		if (pluginFile.isFile()) {
			URL[] jarURLs = new URL[1];
			try {
				jarURLs[0] = pluginFile.toURI().toURL();
				ClassLoader loader = new URLClassLoader(jarURLs, this.getClass().getClassLoader());
				Class<?> pluginClass = Class.forName(qualifiedName, true, loader);
				Plugin plugin = (Plugin) pluginClass.newInstance();
				plugin.initAppController(appController);
				return plugin;
			} catch (MalformedURLException e) {
				LOGGER.debug("MalformedURLException while loading plugin: " + pluginFile.getPath(), e);
			} catch (ClassNotFoundException e) {
				LOGGER.debug("ClassNotFoundException while loading plugin: " + pluginFile.getPath(), e);
			} catch (InstantiationException e) {
				LOGGER.debug("InstantiationException while loading plugin: " + pluginFile.getPath(), e);
			} catch (IllegalAccessException e) {
				LOGGER.debug("IllegalAccessException while loading plugin: " + pluginFile.getPath(), e);
			}
		}
		
		return null;
	}

}
