package edled.view;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import edled.Application;
import edled.core.ManipulationOption;
import edled.core.MetaNode;
import edled.core.Model;
import edled.core.validation.ValidationResult;
import edled.plugin.Plugin;

/**
 * Acts as a fascade for all related view actions.
 * 
 * @author Oliver Zscheyge
 */
public class View {
	/** */
	private static final Logger logger = Logger.getLogger(View.class);
	
	/** File name segment qualifying 12x12-pixel icons. */
	public static final String ICON_SIZE_MODIFIER_12 = "12_";
	/** File name segment qualifying 16x16-pixel icons. */
	public static final String ICON_SIZE_MODIFIER_16 = "16_";
	
	/** Main frame/window of the application. */
	private MainFrame mainWindow;
	/** Application controller. */
	private Application controller;
	/** The area in which messages for the user a displayed. */
	private JTextArea messageArea;
	/** The panel showing detailed information about a XML (EDL) element. */
	private InspectorPanel edlInspectorPanel;
	
	/** DateFormatter for the message time stamps. */
	private final DateFormat dateFormatter;
	
	/**
	 * Creates the view (all graphical user interfaces).
	 * 
	 * @param controller The application controller.
	 */
	public View(final Application controller) {
		try {
//			UIManager.setLookAndFeel("javax.swing.plaf.basic.BasicLookAndFeel");
//			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
		}
		
		this.dateFormatter = new SimpleDateFormat("HH:mm:ss");
		
		this.controller = controller;
		this.edlInspectorPanel = new InspectorPanel(this);
		this.messageArea = new JTextArea();
		this.messageArea.setEditable(false);
		this.mainWindow = new MainFrame(this, this.edlInspectorPanel, this.messageArea);
		
		this.mainWindow.setVisible(true);
	}
	
	/**
	 * Sets the model for the tree view.
	 * 
	 * @param model The model object encapsulating the tree model.
	 */
	public void setModel(final Model model) {
		this.mainWindow.setTree(new TreeView(model.treeModel(), this.edlInspectorPanel, this));
	}
	/**
	 * Acknowledges a plugin to the view. This results in a creation
	 * of a tab for the plugin view.
	 * 
	 * @param plugin The Plugin object to add.
	 */
	public void addPlugin(final Plugin plugin) {
		this.mainWindow.addPlugin(plugin);
	}
	
	/* Model methods that are provided to the views by this fascade. */
	MetaNode getMetaXMLNodeForNode(final Node node) {
		return this.controller.getModel().getMetaXMLNodeForNode(node);
	}
	
	List<ManipulationOption> getManipulationOptionsForNode(final Node node) {
		return this.controller.getModel().getManipulationOptionsFor(node);
	}
	
	ValidationResult getValidationResultForNode(final Node node, final boolean deep) {
		return this.controller.getModel().getValidationResult(node, deep);
	}

	/** 
	 * Requests the creation of a new XML (EDL) document from the app controller.
	 */
	void newDocument() {
		this.controller.newDocument();
	}
	
	/**
	 * Asks the controller to load a given XML (EDL) file.
	 * 
	 * @param file The file to load.
	 * @return     Boolean indicating whether the load process was successful.
	 */
	boolean loadXMLFile(final File file) {
		this.edlInspectorPanel.showNodeInfo(null, null); // Clear inspector.
		
		return this.controller.load(file);
	}
	
	/**
	 * Asks the controller to save the current model in a given file.
	 * 
	 * @param file The file to write the model / document to.
	 */
	void save(final File file) {
		this.controller.save(file, true);
		this.controller.getModel().revalidate();
	}
	
	/**
	 * Querys the controller for the file which is currently edited.
	 * 
	 * @return The file the app is currently working on. Can be null if
	 * 		   no file has been loaded/saved yet.
	 */
	File getCurrentFile() {
		return this.controller.getCurrentXMLFile();
	}
	
	/** 
	 * Querys the controller for the history of recently opened files.
	 * 
	 * @return Recently opened files as a list of paths.
	 */
	List<String> getRecentFiles() {
		return this.controller.getRecentFiles();
	}
	
	/**
	 * Sets the value of a node.
	 * 
	 * @param node     Node whose value needs to be changed.
	 * @param newValue The new string value.
	 */
	void setNodeValue(final Node node, final String newValue) {
		this.controller.getModel().setNodeValue(node, newValue);
	}
	
	/**
	 * Asks the model whether a certain node is whitelisted meaning
	 * it is not subject to validation.
	 * 
	 * @param node The node to check.
	 * @return     True if the node is whitelisted. False otherwise.
	 */
	boolean isWhitelisted(final Node node) {
		return this.controller.getModel().isWhitelisted(node);
	}
	
	/**
	 * Updates a plugin (model) specified by its qualified name with
	 * the information from the application's main model.
	 * Fascade method for all view classes. Decouples from the app controller.
	 * 
	 * @param qualifiedName The qualified name of the plugin that needs an update.
	 */
	void updatePlugin(final String qualifiedName) {
		this.controller.updatePlugin(qualifiedName);
	}
	/**
	 * Tries to incooperate information from a plugin's model to the main
	 * application model.
	 * Fascade method for all view classes. Decouples from the app controller.
	 * 
	 * @param qualifiedName The qualified name of the plugin to update from.
	 */
	void updateFromPlugin(final String qualifiedName) {
		this.controller.updateFromPlugin(qualifiedName);
	}
	/**
	 * Returns all the DOM nodes that are also modelled by plugins.
	 * Fascade method for all view classes. Decouples from the app controller.
	 * 
	 * @return A map containing all DOM nodes of the current application model
	 * 		   that may be also created/modelled by a plugin.
	 */
	Map<Node, Plugin> getNodesConfiguredByPlugins() {
		 return this.controller.getNodesConfiguredByPlugins();
	}
	
//	Plugin getPlugin(final String qualifiedName) {
//		return this.controller.getPlugin(qualifiedName);
//	}
	
	/**
	 * Writes a message to the main frames message area.
	 * 
	 * @param msg     The message string to write.
	 * @param msgType Type of the message. Uses the same types as JOptionPane.
	 */
	public void showMessage(final String msg, 
							final int msgType) {
		synchronized (this.messageArea) {
			this.messageArea.append(this.dateFormatter.format(new Date()) + " " + msg);
		}
		logger.info(msg);
	}

	/**
	 * Convenience method for showing an error dialog.
	 * 
	 * @param msg The string message to show in the dialog.
	 */
	public void showErrorDialog(final String msg) {
		JOptionPane.showMessageDialog(this.mainWindow, 
				  					  msg, 
				  					  "Error", 
				  					  JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Convenience method for showing an information dialog.
	 * 
	 * @param msg The string message to show in the dialog.
	 */
	public void showInfoDialog(final String msg) {
		JOptionPane.showMessageDialog(this.mainWindow, 
									  msg, 
									  "Info", 
									  JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Convenience method for showing a warning dialog.
	 * 
	 * @param msg The string message to show in the dialog.
	 */
	public void showWarnDialog(final String msg) {
		JOptionPane.showMessageDialog(this.mainWindow,
			    					  msg,
			    					  "Warning",
			    					  JOptionPane.WARNING_MESSAGE);
	}
	
	public String getAppName() {
		return this.controller.getName();
	}
	public String getVersion() {
		return this.controller.getVersion();
	}
	public String getAuthor() {
		return this.controller.getAuthor();
	}
	
	public String getIconSizeModifier() {
		if (System.getProperty("os.name").contains("Linux")) {
			return View.ICON_SIZE_MODIFIER_16;
		} else {
			return View.ICON_SIZE_MODIFIER_12;
		}
	}

}
