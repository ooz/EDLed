package de.mpg.cbs.edled.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;

/**
 * EDLed main and default panel/tab. Contains the hierarchical TreeView (EDL/XML
 * document structure) and the InspectorPanel (detailed information for one
 * selected node).
 * 
 * @author Oliver Z.
 */
public class GeneralPanel extends JPanel implements TreeReceiver {
	
	private static final long serialVersionUID = -7345437371067550354L;
	
	/** Default height of the general panel in pixel. */
	private static final int DEFAULT_HEIGHT = 420;
	
	JSplitPane splitPane = null;
	InspectorPanel inspectorPanel = null;
	
	/** Constructor */
	public GeneralPanel(final InspectorPanel xmlInspectorPanel) {
		super(new BorderLayout());
		
		this.inspectorPanel = xmlInspectorPanel;
		
//		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(), new JScrollPane(xmlInspectorPanel));
		
//		this.splitPane.setBottomComponent(new JScrollPane(xmlInspectorPanel));
//		splitPane.setOneTouchExpandable(true);
//		this.splitPane.setDividerLocation(0.5); // Divide in the middle.
//		this.add(this.splitPane);
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, GeneralPanel.DEFAULT_HEIGHT));
	}

	@Override
	public void setTree(final JTree tree) {
//		this.splitPane.setTopComponent(new JScrollPane(tree));
		this.removeAll();
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(this.inspectorPanel));
		split.setResizeWeight(0.25);
		this.add(split);
		
		this.inspectorPanel.setTree(tree);
		
		this.repaint();
	}
	
}
