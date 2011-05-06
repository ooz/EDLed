package design;

import java.io.File;

import javax.swing.JPanel;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import design.bart.DesignElement;
import edled.Application;
import edled.core.Model;
import edled.plugin.Plugin;
import edled.plugin.ReplacementManager;
import edled.util.Configuration;

/**
 * Plugin controller for the design plugin.
 * 
 * @author Oliver Zscheyge
 */
public class DesignPlugin implements Plugin {
	
	/** */
	private static final Logger LOGGER = Logger.getLogger(DesignPlugin.class);
	
	private static final String PARADIGM_KEY = "PARADIGM";
	private static final String TR_KEY = "TR";
	private static final String MEASUREMENTS_KEY = "MEASUREMENTS";
	private static final String REFERENCE_FUNCTIONS_KEY = "REFERENCE_FUNCTIONS";
	
	private static final String NAME = "Design";
	private static final String AUTHOR = "Oliver Zscheyge";
	private static final String DESCRIPTION = "Plugin for graphical display of fMRI design data like regressors, HRF, etc.";
	private static final String VERSION = "0.1.0";
	
	private Application appController = null;
	private final DesignView pluginView;
	private final DesignData pluginModel;
	
	private final ReplacementManager nodeMapper;
	
	public DesignPlugin() {
		this.pluginModel = new DesignData();
		this.pluginView  = new DesignView(this);
		
		Configuration config = Configuration.getInstance();
		File mapFile = new File(config.resolveVariables("$PLUGIN_DIR") + Configuration.FILE_SEPARATOR + getQualifiedName() + ".map");
		this.nodeMapper = ReplacementManager.createFrom(mapFile);
	}

	@Override
	public String getName() { return NAME; }
	@Override
	public String getQualifiedName() { return DesignPlugin.class.getName(); }
	@Override
	public String getAuthor() { return AUTHOR; }
	@Override
	public String getDescription() { return DESCRIPTION; }
	@Override
	public String getVersion() { return VERSION; }
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
				DesignElement newDesign = new DesignElement(paradigmNode, trNode, measurementsNode, refFctsNode);
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
	
	public DesignData getModel() {
		return this.pluginModel;
	}
	
}
