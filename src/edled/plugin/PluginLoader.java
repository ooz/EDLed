package edled.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import edled.Application;
import edled.util.Configuration;



public class PluginLoader {
	
	private PluginLoader() {
		
	}
	
	public static PluginLoader newInstance() {
		return new PluginLoader();
	}
	
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

}
