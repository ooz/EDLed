package de.mpg.cbs.edledplugin.design;

import java.io.File;

import javax.swing.JPanel;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.Application;
import de.mpg.cbs.edled.core.Model;
import de.mpg.cbs.edled.plugin.Plugin;
import de.mpg.cbs.edled.plugin.ReplacementManager;
import de.mpg.cbs.edled.util.Configuration;
import de.mpg.cbs.edledplugin.design.bart.DesignElement;

/**
 * Plugin controller for the design plugin.
 * 
 * @author Oliver Z.
 */
public class DesignPlugin implements Plugin {
	
	/** */
	private static final Logger LOGGER = Logger.getLogger(DesignPlugin.class);
	
	/* Keys for model node mapping. */
	/** Specifies the paradigm node. */
	private static final String PARADIGM_KEY = "PARADIGM";
	/** Specifies the TR (repetition time) node. */
	private static final String TR_KEY = "TR";
	/** Specifies the measurements node. */
	private static final String MEASUREMENTS_KEY = "MEASUREMENTS";
	/** Specifies the reference functions node. */
	private static final String REFERENCE_FUNCTIONS_KEY = "REFERENCE_FUNCTIONS";
	
	/** Name of the plugin. */
	private static final String NAME = "Design";
	/** Author of the plugin. */
	private static final String AUTHOR = "Oliver Z.";
	/** Short description of the plugin. */
	private static final String DESCRIPTION = "Plugin for graphical display of fMRI design data like regressors, HRF, etc.";
	/** Version of the plugin. */
	private static final String VERSION = "0.2.0";
	
	/** Root controller of the main application. */
	private Application appController = null;
	/** Root view of the plugin. */
	private final DesignView pluginView;
	/** Root model of the plugin. */
	private final DesignData pluginModel;
	
	/** ReplacementManager that keeps track of nodes that need to be
	 *  updated in the main application model. */
	private final ReplacementManager nodeMapper;
	
	/**
	 * Constructor.
	 */
	public DesignPlugin() {
		this.pluginModel = new DesignData();
		this.pluginView  = new DesignView(this);
		
		Configuration config = Configuration.getInstance();
		File mapFile = new File(config.resolveVariables("$PLUGIN_DIR") + Configuration.FILE_SEPARATOR + getQualifiedName() + ".map");
		this.nodeMapper = ReplacementManager.createFrom(mapFile);
	}

	@Override
	public String getName()		     { return NAME; }
	@Override
	public String getQualifiedName() { return DesignPlugin.class.getName(); }
	@Override
	public String getAuthor() 		 { return AUTHOR; }
	@Override
	public String getDescription()	 { return DESCRIPTION; }
	@Override
	public String getVersion() 		 { return VERSION; }
	@Override
	public ReplacementManager getReplacementManager(boolean updateManager) {
		// The design plugin won't update the main model - it's only for
		// visualizing the design data.
		return null;
	}
	@Override
	public JPanel getView() {
		return this.pluginView;
	}
	@Override
	public void initAppController(Application appController) {
		if (this.appController == null) {
			this.appController = appController;
		}
	}

	@Override
	public void update(Model model) {
		Document doc = model.getDocument();
		try {
			Node paradigmNode = (Node) this.nodeMapper.xpathFor(PARADIGM_KEY).evaluate(doc, XPathConstants.NODE);
			Node trNode       = (Node) this.nodeMapper.xpathFor(TR_KEY).evaluate(doc, XPathConstants.NODE);
			Node measurementsNode = (Node) this.nodeMapper.xpathFor(MEASUREMENTS_KEY).evaluate(doc, XPathConstants.NODE);
			Node refFctsNode  = (Node) this.nodeMapper.xpathFor(REFERENCE_FUNCTIONS_KEY).evaluate(doc, XPathConstants.NODE);
			if (model.getValidationResult(paradigmNode, true).isValid()) {
				DesignElement newDesign = null;
				try {
					newDesign = new DesignElement(paradigmNode, trNode, measurementsNode, refFctsNode);
				} catch(IllegalArgumentException e) {
					LOGGER.warn("IllegalArgumentException while initializing DesignElement from DOM nodes.", e);
				} catch(IndexOutOfBoundsException e) {
					LOGGER.warn("Index out of bounds while creating design element.", e);
				}
				this.pluginModel.setDesign(newDesign);
				this.pluginView.register(newDesign);
			}
			if (model.getValidationResult(refFctsNode, true).isValid()) {
				// TODO
//				MediaObjectList mediaObjList = this.pluginModel.getMediaObjectList();
//				formatter.fill(mediaObjList, mediaObjListNode, appController.getCurrentXMLFile());
//				if (model.getValidationResult(timetableNode, true).isValid()) {
//					formatter.fill(this.pluginModel.getTimetable(), timetableNode, mediaObjList);
//				}
			}
		} catch (XPathExpressionException e) {
			LOGGER.debug("XPathExpressionException while fetching nodes from the main model "
					     + "(check map file for design plugin!)", e);
		}
	}
	
	/**
	 * Returns the plugin root model object.
	 * 
	 * @return The root model object of the plugin.
	 */
	public DesignData getModel() {
		return this.pluginModel;
	}
	
}
