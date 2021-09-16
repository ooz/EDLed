package de.mpg.cbs.edled;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Schema;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.Model;
import de.mpg.cbs.edled.core.XSOMMetaTreeBuilder;
import de.mpg.cbs.edled.core.metatree.MetaTreeBuilder;
import de.mpg.cbs.edled.core.validation.EDLRuleValidator;
import de.mpg.cbs.edled.plugin.Plugin;
import de.mpg.cbs.edled.plugin.PluginLoader;
import de.mpg.cbs.edled.plugin.ReplacementManager;
import de.mpg.cbs.edled.util.Configuration;
import de.mpg.cbs.edled.util.FileStatus;
import de.mpg.cbs.edled.util.FileUtility;
import de.mpg.cbs.edled.view.View;
import de.mpg.cbs.edled.xml.XMLUtility;


/**
 * Application entry point for EDLed. Main controller of the app.
 * 
 * @author Oliver Z.
 */
public class Application implements Runnable {
	
	private static final String APPLICATION_NAME = "EDLed";
	private static final String VERSION = "3.1.0";
	private static final String AUTHOR = "Oliver Z.";
	
	private static final String MSG_HOWTO_CORRECT_XSD = 
			"Please correct the \"XSD\" setting in Edit > Preferences and restart the application.";
	private static final String XSD_XSD_PATH = "$XSD_DIR$/XMLSchema.xsd";
	
	private static final Logger logger = Logger.getLogger(Application.class);
	
	/** Maximum number of recent files that are tracked in the history. */
	private static final int MAX_RECENTS = 10;
	
	/** The application configuration. */
	private Configuration config = null;
	
	/** All plugins identified by their qualified names. */
	private Map<String, Plugin> plugins = null;
	
	/** The application's model. */
	private Model model = null;
	/** The application's view. */
	private View view = null;
	
	/** The file containing the XSD. */
	private File xsdFile = null;
	/** The EDLRules file. */
	private File edlRulesFile = null;
	/** 
	 * A validator object for the purpose of validating the model against 
	 * the EDLRules.
	 */
	private EDLRuleValidator edlValidator = null;
	
	/** The XML (EDL) file that currently used by the app. */
	private File currentXML = null;
	
	/** History of recently opened files (as file paths). */
	private List<String> recentFiles = new LinkedList<String>();
	/** File where the history of recent files is saved. */
	private File historyFile = null;
	
	public Application(final String[] args) {
		init();
		setupPlugins();
		
		// If first argument is a file path: open that file
		if (args.length >= 1) {
			File initialFile = new File(args[0]);
			if (initialFile.exists()) {
				this.load(initialFile);
			}
		}
	}

	/**
	 * Initializes the app:
	 * - config setup
	 * - reading the XSD and EDLRules file
	 */
	private void init() {
		this.config = Configuration.getInstance();
		loadRecents(); // TODO: recents can be loaded in parallel
		logger.info("Application initialized.");
		
		this.view = new View(this);
		
		// TODO: exception in case the document element was not found!
		this.xsdFile = new File(this.config.getProp(Configuration.XSD));
		validateXSD();
		
		this.edlRulesFile = new File(this.config.getProp(Configuration.EDLRULES));
		this.edlValidator = new EDLRuleValidator(edlRulesFile);
	}
	
	/**
	 * Loads paths of recently opened files.
	 */
	private void loadRecents() {
		this.historyFile = new File(this.config.getProp(Configuration.RECENT_FILES));
		this.recentFiles = FileUtility.lines(this.historyFile); 
	}

	/**
	 * Checks whether the given XSD file is existing and a valid
	 * instance of the XML schema specification (XSD of XSDs).
	 * 
	 * The validation is performed in a different thread since it usually
	 * takes 40-50 seconds.
	 */
	private void validateXSD() {
		final Application self = this;
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (self.xsdFile.exists()) {
					logger.info("XSD file found.");
					Schema xsdSchema = XMLUtility.loadSchema(new File(
							self.config.resolveVariables(Application.XSD_XSD_PATH)));
					if (xsdSchema == null) {
						logger.warn("Could not find the XML schema for XSDs! No internet connection?");
						self.view.showWarnDialog("Could not find the XML schema for XSDs!\n" 
								+ "Please check your EDLed installation and internet connection!");
					}
					
					Document xsdDoc = XMLUtility.loadDocument(self.xsdFile, xsdSchema);
					if (xsdDoc == null) {
						logger.warn("EDL XSD is not a valid XSD!");
						self.view.showErrorDialog("The current XSD file is not valid!\n" 
								+ Application.MSG_HOWTO_CORRECT_XSD);
					} 
					
				} else {
					logger.warn("Could not find XSD file!");
					self.view.showWarnDialog("No XSD file was found!\n"
							+ Application.MSG_HOWTO_CORRECT_XSD);
				}
			}
		});
		
		t.start();
	}

	/**
	 * Adds all the plugins that should be loaded according the configuration.
	 */
	private void setupPlugins() {
		this.plugins = new LinkedHashMap<String, Plugin>();
		
		PluginLoader factory = PluginLoader.newInstance();
		for (String qualifiedPluginName : this.config.getUsedPlugins()) {
			Plugin plugin = factory.createPlugin(this, qualifiedPluginName);
			if (plugin != null) {
				logger.info("Using plugin " + plugin.getQualifiedName() + " (" + plugin.getName() + ").");
				this.plugins.put(qualifiedPluginName, plugin);
				this.view.addPlugin(plugin);
			}
		}
	}
	
	private void updateRecents(final File newFile) {
		String path = newFile.getPath();
		
		if (this.recentFiles.contains(path)) {
			this.recentFiles.remove(path);
		}
		this.recentFiles.add(0, path);
		
		while (this.recentFiles.size() > MAX_RECENTS) {
			this.recentFiles.remove(this.recentFiles.size() - 1);
		}
	}
	private void saveRecents() {
		if (this.historyFile != null) {
			FileUtility.writeLines(historyFile, this.recentFiles);
		}
	}

	/**
	 * Application entry point.
	 * 
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		(new Application(args)).run();
	}
	
	public String getName() {
		return Application.APPLICATION_NAME;
	}
	public String getVersion() {
		return Application.VERSION;
	}
	public String getAuthor() {
		return Application.AUTHOR;
	}
	
	@Override
	public void run() {
//		System.out.println("os.arch: " + System.getProperty("os.arch"));
//		System.out.println("user.dir: " + System.getProperty("user.dir"));
	}
	
	/**
	 * View getter.
	 * @return The application view.
	 */
	public View getView() {
		return this.view;
	}

	/** 
	 * Model getter.
	 * @return The application model.
	 */
	public Model getModel() {
		return this.model;
	}

	/**
	 * Creates a new XML (EDL) document to work on.
	 */
	public void newDocument() {
		if (xsdFile.exists()) {
//			Schema schema = XMLUtility.loadSchema(this.xsdFile);
			MetaTreeBuilder metaTreeBuilder = new XSOMMetaTreeBuilder(this.xsdFile);
			this.model = new Model(this.config.getProp(Configuration.DOCUMENTELEMENT), 
								   metaTreeBuilder,
								   this.edlValidator);
			
//			this.model.printToStdout();
//			this.model.printAllDistinctBaseTypes();
		}
		this.currentXML = null;
		if (this.model != null) {
//			this.plugins.put(StimulusPlugin.class.toString(), new StimulusPlugin(this.model.getDocument(), ""));
			this.view.setModel(this.model);
			logger.info("New document created.");
		} else {
			logger.error("Could not create new document!");
		}
	}
	
	/**
	 * Writes the current XML (EDL) document to a file.
	 * 
	 * @param to        The file the XML document should be written to.
	 * @param overwrite Flag indicating whether an existing file should be replaced
	 */
	// TODO: use overwrite flag!
	public void save(final File to, final boolean overwrite) {
		XMLUtility.saveDocument(this.model.getDocument(), to);
		XMLUtility.loadDocument(to, XMLUtility.loadSchema(this.xsdFile));
		
		setCurrentXMLFile(to);
		logger.info("Saved document to " + to.getPath());
	}
	
	/**
	 * Loads a XML (EDL) document from a given file.
	 * 
	 * @param from The file representing the XML (EDL) configuration.
	 * @return     FileStatus indicating whether the read/load process was 
	 * 			   successful and whether the document is an instance of the 
	 * 			   given XSD.
	 */
	public FileStatus load(final File from) {
		Schema schema = XMLUtility.loadSchema(this.xsdFile);
		Document document = XMLUtility.loadDocument(from, schema);
		MetaTreeBuilder metaTreeBuilder = new XSOMMetaTreeBuilder(this.xsdFile);
		
		Model newModel = null;
		
		if (document != null) {
			try {
				newModel = new Model(document, metaTreeBuilder, this.edlValidator);
			} catch (RuntimeException e) {
				logger.warn("XML/EDL file is not schema compliant!");
				newModel = null;
			}
		} else {
			return FileStatus.NOT_FOUND;
		}
		
		if (newModel != null) {
			setCurrentXMLFile(from);
			this.model = newModel;
			this.view.setModel(this.model);
			
			logger.info("Opened " + from.getPath());
			
			return FileStatus.SUCCESS;
		}
		
		return FileStatus.NOT_COMPLIANT;
	}
	
	/**
	 * Returns the file which is currently edited.
	 * 
	 * @return The file the app is currently working on. Can be null if
	 * 		   no file has been loaded/saved yet.
	 */
	public File getCurrentXMLFile() {
		return this.currentXML;
	}
	
	private void setCurrentXMLFile(final File file) {
		this.currentXML = file;
		updateRecents(file);
		saveRecents();
	}
	
	/**
	 * The most recently opened file path is the first one in the list.
	 * 
	 * @return List of recently opened files (as paths).
	 */
	public List<String> getRecentFiles() {
		return Collections.unmodifiableList(this.recentFiles);
	}
	
	/**
	 * Updates a plugin (model) specified by its qualified name with
	 * the information from the application's main model.
	 * 
	 * @param qualifiedName The qualified name of the plugin that needs an update.
	 */
	public void updatePlugin(final String qualifiedName) {
		Plugin plugin = this.plugins.get(qualifiedName);
		if (plugin != null) {
			logger.debug("Updating plugin: " + qualifiedName);
			if (this.model != null) {
				plugin.update(this.model);
			}
		}
	}
	
	/**
	 * Tries to incooperate information from a plugin's model to the main
	 * application model.
	 * 
	 * @param qualifiedName The qualified name of the plugin to update from.
	 */
	public void updateFromPlugin(final String qualifiedName) {
		if (this.model == null) {
			return;
		}
		
		Plugin plugin = this.plugins.get(qualifiedName);
		if (plugin != null) {
			logger.debug("Updating from plugin: " + qualifiedName);
			ReplacementManager mapper = plugin.getReplacementManager(true);
			if (mapper != null) {
				for (XPathExpression xpathOfNodeToReplace : mapper.getXPaths()) {
					try {
						Node toReplace = (Node) xpathOfNodeToReplace.evaluate(this.model.getDocument(), XPathConstants.NODE);
						logger.debug("Node to replace: " + toReplace);
						if (toReplace != null) {
							this.model.replace(toReplace, mapper.nodeFor(xpathOfNodeToReplace));
						}
					} catch (XPathExpressionException e) {
						logger.debug("XPathException while updating the application from a plugin.", e);
					}
				}
			}
		}
	}
	
	/**
	 * Getter for all the plugins.
	 * @return A new LinkedHashMap that contains all plugins identified by their
	 *         qualified names.
	 */
	public Map<String, Plugin> getPlugins() {
		return new LinkedHashMap<String, Plugin>(this.plugins);
	}
	
	/**
	 * Returns all the DOM nodes that are also modelled by plugins.
	 * 
	 * @return A map containing all DOM nodes of the current application model
	 * 		   that may be also created/modelled by a plugin.
	 */
	public Map<Node, Plugin> getNodesConfiguredByPlugins() {
		Map<Node, Plugin> nodes = new HashMap<Node, Plugin>();
		
		Document xmlDocument = this.model.getDocument();
		for (Plugin plugin : this.plugins.values()) {
			ReplacementManager mapper = plugin.getReplacementManager(false);
			if (mapper != null) {
				for (XPathExpression xpath : mapper.getXPaths()) {
					try {
						Node node = (Node) xpath.evaluate(xmlDocument, XPathConstants.NODE);
						if (node != null) {
							nodes.put(node, plugin);
						}
					} catch (XPathExpressionException e) {
						logger.debug("XPathException while querying for nodes which are configured by plugins", e);
					}
				}
			}
		}
		
		return nodes;
	}
	
//	private boolean loadDefaultProperties() {
//		return false;
//	}
//	
//	private boolean loadAppProperties() {
//		return false;
//	}
//	
//	private boolean saveAppProperties() {
//		return false;
//	}
}
