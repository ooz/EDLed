package de.mpg.cbs.edledplugin.design;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.mpg.cbs.edledplugin.design.bart.DesignElement;

/**
 * Root view for the design plugin.
 * Contains the tab view for additional views on the design (convolution,
 * orthogonality etc.)
 * 
 * @author Oliver Z.
 */
public class DesignView extends JPanel implements DesignElementReceiver {
	/** */
	private static final long serialVersionUID = -1410868044151014221L;
	
//	private final DesignPlugin controller;
	
	/** Tabs for selecting the appropriate design view  (convolution, orthogonality etc.)*/
	private final JTabbedPane tabs;
	
	/** Convolution view. */
	private final ConvolutionView convolutionView;
	/** Orthogonality view. */
	private final OrthogonalityView orthogonalityView;
	/** HRF view. */
	private final HRFView hrfView;
	
	/**
	 * Constructor.
	 * 
	 * @param controller Main design plugin controller.
	 */
	DesignView(final DesignPlugin controller) {
		super(new BorderLayout());
		
//		this.controller = controller;
		
		this.tabs = new JTabbedPane();
		DesignElement design = controller.getModel().getDesign();
		this.convolutionView = new ConvolutionView(design);
		this.orthogonalityView = new OrthogonalityView(design);
		this.hrfView = new HRFView(design);
		this.tabs.addTab(DesignViewConstants.CONVOLUTION_VIEW_NAME, this.convolutionView);
		this.tabs.addTab(DesignViewConstants.ORTHOGONALITY_VIEW_NAME, this.orthogonalityView);
		this.tabs.addTab(DesignViewConstants.HRF_VIEW_NAME, this.hrfView);
		
		add(this.tabs, BorderLayout.CENTER);
	}
	
	@Override
	public void register(final DesignElement design) {
		this.convolutionView.register(design);
		this.orthogonalityView.register(design);
		this.hrfView.register(design);
	}

}
