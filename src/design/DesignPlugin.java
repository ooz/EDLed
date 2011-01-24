package design;

import javax.swing.JPanel;

import edled.Application;
import edled.core.Model;
import edled.plugin.Plugin;
import edled.plugin.ReplacementManager;

/**
 * Plugin controller for the design plugin.
 * 
 * @author Oliver Zscheyge
 */
public class DesignPlugin implements Plugin {
	
	private static final String NAME = "Design";
	private static final String AUTHOR = "Oliver Zscheyge";
	private static final String DESCRIPTION = "Plugin for graphical display of fMRI design data like regressors, HRF, etc.";
	private static final String VERSION = "0.1";
	
//	private Application appController = null;
//	private final DesignView pluginView;
//	private final DesignData pluginModel;
	
	public DesignPlugin() {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initAppController(Application appController) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Model model) {
		// TODO Auto-generated method stub
		
	}
	
}
