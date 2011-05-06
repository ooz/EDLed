package design;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import design.bart.DesignElement;

public class DesignView extends JPanel implements DesignElementReceiver {
	/** */
	private static final long serialVersionUID = -1410868044151014221L;
	
//	private final DesignPlugin controller;
	
	private final JTabbedPane tabs;
	
	private final ConvolutionView convolutionView;
	private final OrthogonalityView orthogonalityView;
//	private final HRFView hrfView;
	
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
