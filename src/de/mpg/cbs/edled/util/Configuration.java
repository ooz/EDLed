package de.mpg.cbs.edled.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Utility class for the global application configuration.
 * 
 * @author Oliver Z.
 */
public class Configuration {
	
	private static final Logger logger = Logger.getLogger(Configuration.class);
	
	/** Stored for convenient access to the system property file.separator. */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/* All configuration keys: */
	/** Configuration key for the logger configuration file. */
	public static final String LOGGER_CONFIGURATION = "LOGGER_CONFIGURATION";
	/** Configuration key for the name of the document element. */ 
	public static final String DOCUMENTELEMENT = "DOCUMENTELEMENT";
	/** Configuration key for the XSD file path. */
	public static final String XSD = "XSD";
	/** Configuration key for the EDL rules file path. */
	public static final String EDLRULES = "EDLRULES";
	/** Configuration key for the recent files list (file path). */
	public static final String RECENT_FILES = "RECENT_FILES";
	
	/** Configuration key for the list of plugins to use (qualified names seperated by commas). */
	private static final String USE_PLUGINS = "USE_PLUGINS";
	
	/** Name of the XML schema specification (XML schema XSD). */
	public static final String XSD_XSD_FILE = "XMLSchema.xsd"; 
	
	/** Name of the logger configuration file. */ 
	private static final String LOGGER_CONFIG_FILE = "logger.conf";
	/** File name for the default configuration. */
	private static final String DEFAULTS_CONFIG_FILE = "defaults.conf";
	/** File name for the user manipulated configuration. */
	private static final String APPLICATION_CONFIG_FILE = "application.conf";
	/** File name for the file containing the recently opened file paths. */
	private static final String RECENTS_FILE = "recents.txt";
	
	/** File name of the file specifying alternative input methods for 
	 *  some EDL configuration entries. */
	private static final String INPUT_METHODS_MAP_FILE = "inputmethods.map";
	
	/** File name of the application icon. */
	private static final String APP_ICON_FILE = "edled.png";
	
	/** Singleton Configuration instance. */
	private static Configuration config = null;
	
	/** Application properties: a key-value map. */
	private Properties applicationProps = null;
	/** Map containing all variable names (keys) and their resolved values. */
	private Map<String, String> variables = null;
	
	/** Current log file. */
	private File logFile = null;
	/** Application icon. */
	private Image appIcon = null;
	/** Map specifying alternative input methods for some EDL entries. */
	private InputMethodMap inputMethods = new InputMethodMap();
	
	/**
	 * Private singleton constructor.
	 * Loads the configuration and sets up the logger.
	 */
	private Configuration() {
		try {
			setupVariables();
			buildFileHierarchy();
			load();
			setupLogger();
			this.appIcon = Toolkit.getDefaultToolkit().getImage(resolveVariables("$IMG_DIR") 
			 		  	   + Configuration.FILE_SEPARATOR + APP_ICON_FILE);
		} catch (IOException e) {
			logger.fatal("Could not initialize application (Configuration, Logger)!", e);
		}
	}

	/**
	 * Returns the singleton Configuration instance.
	 * 
	 * @return The singleton Configuration instance.
	 */
	public static Configuration getInstance() {
		if (Configuration.config == null) {
			Configuration.config = new Configuration();
		}
		return Configuration.config;
	}
	
	/**
	 * Returns all the keys for the configuration entries.
	 * 
	 * @return All configuration keys.
	 */
	public Set<String> getKeys() {
		return this.applicationProps.stringPropertyNames();
	}
	
	/**
	 * Returns the configuration entry for a given key.
	 * 
	 * @param key The key of the requested config entry.
	 * @return    The configuration value for the given key.
	 */
	public String getProp(final String key) {
		return resolveVariables(this.applicationProps.getProperty(key));
	}
	/**
	 * Returns the configuration entry for a given key without resolving
	 * contained variable names in the configuration entry.
	 * 
	 * @param key The key of the requested config entry.
	 * @return    The configuration value for the given key (without substituting
	 * 			  the variable names).
	 */
	public String getPropWithoutResolvedVariables(final String key) {
		return this.applicationProps.getProperty(key);
	}

	/**
	 * Recursively replaces all variable names with their respective values
	 * in a given string.
	 * 
	 * @param prop The String that might contain variable names that need to
	 * 			   be resolved. 
	 * @return     The given String prop with all variable names resolved.
	 */
	public String resolveVariables(final String prop) {
		if (prop == null) {
			return null;
		}
		
		String resolvedProp = new String(prop);
		for (String variable : this.variables.keySet()) {
			if (resolvedProp.indexOf(variable) != -1) {
				resolvedProp = resolvedProp.replace(variable, this.variables.get(variable));
				resolvedProp = resolveVariables(resolvedProp);
			}
		}
		
		return resolvedProp;
	}

	/**
	 * Changes a property value.
	 * 
	 * @param key   Name of the configuration entry that needs to be changed.
	 * @param value The new value of key.
	 */
	public void setProp(final String key, final String value) {
		this.applicationProps.setProperty(key, value);
	}
	
//	public void removeProp(final String key) {
//		this.applicationProps.remove(key);
//	}
	
	/**
	 * Saves the current configuration state.
	 */
	public void save() {
		write(this.applicationProps, 
			  resolveVariables("$CONFIG_DIR$/") + Configuration.APPLICATION_CONFIG_FILE);
	}
	/**
	 * Does the actual storing of the Property information in a file.
	 * 
	 * @param props	   The Property object to write.
	 * @param filepath The file to write to.
	 */
	private void write(final Properties props, final String filepath) {
		FileOutputStream outstream;
		try {
			outstream = new FileOutputStream(filepath);
			props.store(outstream, "");
			outstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Wrapper for System.getProperty(String key).
	 * 
	 * @param key The system property key.
	 * @return	  The associated value for key.
	 */
	public String getSysProp(final String key) {
		return System.getProperty(key);
	}
	
	/**
	 * Initializes the this.variables map.
	 */
	private void setupVariables() {
		this.variables = new HashMap<String, String>();
		this.variables.put("$APP_DIR", this.getSysProp("user.dir"));
		this.variables.put("$/", this.getSysProp("file.separator"));
		this.variables.put("$CONFIG_DIR", "$APP_DIR$/res$/config");
		this.variables.put("$IMG_DIR", "$APP_DIR$/res$/img");
		this.variables.put("$XSD_DIR", "$APP_DIR$/res$/xsd");
		this.variables.put("$LOG_DIR", "$APP_DIR$/log");
		this.variables.put("$PLUGIN_DIR", "$APP_DIR$/plugin");
//		this.variables.put("$EDL_DIR", "$APP_DIR$/edl");
		this.variables.put("$EDLRULES_DIR", "$APP_DIR$/res$/rules");
		this.variables.put("$DOCUMENTATION_DIR", "$APP_DIR$/documentation");
	}
	
	/**
	 * Builds the necessary file structure for this application.
	 * 
	 * @throws IOException IOException during file creation or file writing.
	 */
	private void buildFileHierarchy() throws IOException {
		File configDir = new File(resolveVariables("$CONFIG_DIR$/"));
		if (!configDir.isDirectory()) {
			configDir.mkdirs();
		}
		File imgDir = new File(resolveVariables("$IMG_DIR$/"));
		if (!imgDir.isDirectory()) {
			imgDir.mkdirs();
		}
		File logDir = new File(resolveVariables("$LOG_DIR$/"));
		if (!logDir.isDirectory()) {
			logDir.mkdirs();
		}
		File pluginDir = new File(resolveVariables("$PLUGIN_DIR$/"));
		if (!pluginDir.isDirectory()) {
			pluginDir.mkdirs();
		}
		File xsdDir = new File(resolveVariables("$XSD_DIR$/"));
		if (!xsdDir.isDirectory()) {
			xsdDir.mkdirs();
		}
//		File edlDir = new File(resolveVariables("$EDL_DIR$/"));
//		if (!edlDir.isDirectory()) {
//			edlDir.mkdirs();
//		}
		File edlRulesDir = new File(resolveVariables("$EDLRULES_DIR$/"));
		if (!edlRulesDir.isDirectory()) {
			edlRulesDir.mkdirs();
		}
		
		File defaultPropertiesFile = new File(resolveVariables("$CONFIG_DIR$/") + Configuration.DEFAULTS_CONFIG_FILE);
		if (!defaultPropertiesFile.exists()) {
			defaultPropertiesFile.createNewFile();
			writeDefaultProperties();
		}
		
		File applicationPropertiesFile = new File(resolveVariables("$CONFIG_DIR$/") + Configuration.APPLICATION_CONFIG_FILE);
		if (!applicationPropertiesFile.exists()) {
			applicationPropertiesFile.createNewFile();
		}
	}
	
	/**
	 * Generates the default properties and writes them to the
	 * defaults configuration file.
	 */
	private void writeDefaultProperties() {
		Properties defaultProps = new Properties();
		defaultProps.setProperty(Configuration.LOGGER_CONFIGURATION, "$CONFIG_DIR$/" + Configuration.LOGGER_CONFIG_FILE);
		defaultProps.setProperty(Configuration.RECENT_FILES, "$CONFIG_DIR$/" + Configuration.RECENTS_FILE);
		defaultProps.setProperty(Configuration.DOCUMENTELEMENT, "rtExperiment");
		defaultProps.setProperty(Configuration.XSD, "$XSD_DIR$/rtExperiment_v15.xsd");
		defaultProps.setProperty(Configuration.EDLRULES, "$EDLRULES_DIR$/edlValidation_rules.xml");
		defaultProps.setProperty(Configuration.USE_PLUGINS, "");
		
		write(defaultProps, 
			  resolveVariables("$CONFIG_DIR$/") + Configuration.DEFAULTS_CONFIG_FILE);
	}
	
	/**
	 * Loads the default and application properties from their respective files
	 * into the configuration object.
	 * 
	 * @throws IOException IOException during file reading.
	 */
	private void load() throws IOException {
		loadInputMethodsMap();
		
		Properties defaults = new Properties();
		FileInputStream instream = new FileInputStream(resolveVariables("$CONFIG_DIR$/") + Configuration.DEFAULTS_CONFIG_FILE);
		defaults.load(instream);
		instream.close();
		
		this.applicationProps = new Properties(defaults);
		instream = new FileInputStream(resolveVariables("$CONFIG_DIR$/") + Configuration.APPLICATION_CONFIG_FILE);
		this.applicationProps.load(instream);
		instream.close();
	}
	private void loadInputMethodsMap() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (inputMethods) {
					inputMethods = new InputMethodMap(resolveVariables("$CONFIG_DIR$/") + Configuration.INPUT_METHODS_MAP_FILE);
				}
			}
		});
		thread.start();
	}
	
	/**
	 * Sets up the log4j environment.
	 */
	private void setupLogger() {
//		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
//		Layout layout = new SimpleLayout();
		Layout layout = new PatternLayout("[%d{HH:mm:ss,SSS} %-5p]: %m%n");
		Logger.getRootLogger().addAppender(new ConsoleAppender(layout, 
															   ConsoleAppender.SYSTEM_ERR));
		Appender fileAppender = null;
		try {
			// Delete oldest log if more than 20 logs are available
			// TODO: make log file number variable
			File logDir = new File(resolveVariables("$LOG_DIR$/"));
			File[] logFiles = logDir.listFiles();
			int logFilesToDelete = logFiles.length - 20;
			while (logFilesToDelete >= 0) {
				File oldest = getOldestFile(logDir);
				oldest.delete();
				logFilesToDelete--;
			}
			
			this.logFile = new File(resolveVariables("$LOG_DIR$/") + generateLogFileName());
			if (!this.logFile.exists()) {
				if (!this.logFile.createNewFile()) {
					this.logFile = null;
				}
			}
			
			if (this.logFile != null) {
				fileAppender = new FileAppender(layout, this.logFile.getPath());
				Logger.getRootLogger().addAppender(fileAppender);
			} /* else {
				BasicConfigurator.configure();
			} */
		} catch (IOException e) {
		}
	}
	/**
	 * Returns the oldest (based on last modification date) file in a given directory.
	 * 
	 * @param directory The directory that needs to be searched for the oldest file. 
	 * @return 			The oldest file in directory. Null if there is no file or 
	 * 		   			if directory is no directory.
	 */
	private File getOldestFile(final File directory) {
		if (!directory.isDirectory()) {
			return null;
		}
		
		File oldest = null;
		long age = Long.MAX_VALUE;
		for (File file : directory.listFiles()) {
			if (file.lastModified() < age
				&& file.isFile()
				&& !file.isHidden()) {
				oldest = file;
				age = file.lastModified();
			}
		}
		
		return oldest;
	}
	/**
	 * Generates an appropriate filename (with timestamp) for a log file.
	 * 
	 * @return The generated log file name (with timestamp).
	 */
	private String generateLogFileName() {
		// Create log file named "log_yyyy_MM_dd_HH_mm.txt"
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		Date current = new Date();
		String name = formatter.format(current);
		
		name += ".log";
		return name;
	}
	
	/**
	 * Returns the current log file.
	 * 
	 * @return The current log file.
	 */
	public File getLogFile() {
		return this.logFile;
	}
	
	/**
	 * Returns the application icon.
	 * 
	 * @return The application icon.
	 */
	public Image getAppIcon() {
		return this.appIcon;
	}
	
	/**
	 * Returns a list of qualified plugin names that indicate which
	 * plugins should be used by the application.
	 * 
	 * @return A list of qualified plugin names.
	 */
	public List<String> getUsedPlugins() {
		String pluginArray = this.getProp(Configuration.USE_PLUGINS);
		
		List<String> pluginNames = new LinkedList<String>();
		if (pluginArray != null) {
			for (String pluginName : pluginArray.split(",")) {
				
				String trimmedName = pluginName.trim();
				if (!trimmedName.isEmpty()) {
					pluginNames.add(trimmedName);
				}
			}	
		}
		return pluginNames;
	}
	
	public InputMethodMap getInputMethodMap() {
		synchronized (this.inputMethods) {
			return this.inputMethods;
		}
	}
	
}
