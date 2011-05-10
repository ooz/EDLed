package design;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import design.bart.DesignElement;

/**
 * Root view for the design plugin.
 * Contains the tab view for additional views on the design (convolution,
 * orthogonality etc.)
 * 
 * @author Oliver Zscheyge
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
//	private final HRFView hrfView;
	
	/**
	 * Constructor.
	 * 
	 * @param controller Main design plugin controller.
	 */
	DesignView(final DesignPlugin controller) {
		super(new BorderLayout());
		
//		this.controller = controller;
		
		this.tabs = new JTabbedPane();
		this.convolutionView = new ConvolutionView(controller.getModel().getDesign());
		this.orthogonalityView = new OrthogonalityView(controller.getModel().getDesign());
		this.tabs.addTab(ConvolutionView.DISPLAY_NAME, this.convolutionView);
		this.tabs.addTab(OrthogonalityView.DISPLAY_NAME, this.orthogonalityView);
		
		add(this.tabs, BorderLayout.CENTER);
	}
	
	@Override
	public void register(final DesignElement design) {
		this.convolutionView.register(design);
		this.orthogonalityView.register(design);
	}

}
