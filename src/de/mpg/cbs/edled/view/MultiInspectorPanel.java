package de.mpg.cbs.edled.view;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.core.metatree.MetaNode;


public class MultiInspectorPanel extends InspectorPanel {
	
	/** */
	private static final long serialVersionUID = 7545277348134530653L;
	
	private static final int TOP_PADDING  = 25;
	private static final int LEFT_PADDING = 10;

	public MultiInspectorPanel(final View view) {
		super(view);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	
	@Override
	public void showNodeInfo(final Node xmlNode, final MetaNode metaNode) {
		removeAll();
		repaint();
		
		// Keep showing nothing if null parameters were passed.
		if (xmlNode == null || metaNode == null) {
			this.revalidate();
			return;
		}
		
		if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {
		
			InspectorPanel nodeInspectorPanel = new InspectorPanel(this.view);
			add(nodeInspectorPanel);
			nodeInspectorPanel.showNodeInfo(xmlNode, metaNode);
			nodeInspectorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
															nodeInspectorPanel.getPreferredSize().height));
			
			NodeList children = xmlNode.getChildNodes();
			int elemNodeCount = 0;
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					elemNodeCount++;
				}
			}
			
			if (elemNodeCount > 0) {
				JPanel childPane = new JPanel();
				childPane.setBorder(new EmptyBorder(TOP_PADDING, LEFT_PADDING, 0, 0));
				childPane.setLayout(new BoxLayout(childPane, BoxLayout.PAGE_AXIS));
				
				String childrenLabelStr = "<html><b>Child ";
				if (elemNodeCount > 1) {
					childrenLabelStr += "entries";
				} else {
					childrenLabelStr += "entry";
				}
				childrenLabelStr += ":</b></html>";
				JLabel childrenLabel = new JLabel(childrenLabelStr); 
				
				childPane.add(childrenLabel);
				
				for (int i = 0; i < children.getLength(); i++) {
					Node childNode = children.item(i);
					MetaNode childMetaNode = this.view.getMetaXMLNodeForNode(childNode);
					
					InspectorPanel childInspectorPanel = new InspectorPanel(this.view);
					
					childPane.add(childInspectorPanel);
					childInspectorPanel.showNodeInfo(childNode, childMetaNode);
					childInspectorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
																	 childInspectorPanel.getPreferredSize().height));
				}
			
//				childPane.setMaximumSize(childPane.getPreferredSize());
				add(childPane);
				
				add(Box.createVerticalGlue());
			}
			
		}
	
		revalidate();
		repaint();
	}

}
