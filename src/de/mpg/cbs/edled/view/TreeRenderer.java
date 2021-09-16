package de.mpg.cbs.edled.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.ManipulationOption;


/**
 * Custom TreeCellRenderer rendering nodes in the TreeView (hierarchical view
 * of the XML/EDL document structure). Displays icons indicating validation
 * status and manipulations options next to each node.
 * 
 * @author Oliver Z.
 */
public class TreeRenderer implements TreeCellRenderer {

	/** Default padding width (space reserved for icons) for rendered nodes. */
	private static final int RENDERED_COMP_EXTRA_WIDTH = 50;
	/** Default height of a rendered node. */
	private static final int RENDERED_COMP_HEIGHT = 20;
	
	/** Reference to the view fascade. */
	private View view = null;
	/** Using the DefaultTreeCellRenderer to render the textual part of the node. */
	private DefaultTreeCellRenderer defaultRenderer = null;
	
	/** Object that provides all needed application icons. */
	private final IconProvider iconProvider;
	
	/** 
	 * Constructor.
	 * 
	 * @param view The view fascade.
	 */
	public TreeRenderer(final View view) {
		this.view = view;
		this.defaultRenderer = new DefaultTreeCellRenderer();
		
		this.iconProvider = IconProvider.getInstance();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree,
			  									  Object value,
			  									  boolean sel,
			  									  boolean expanded,
			  									  boolean leaf,
			  									  int row,
			  									  boolean hasFocus) {
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) value;
		Node currentXMLNode = (Node) currentNode.getUserObject();
		
		JPanel renderedComp = new JPanel();
//		renderedComp.setLayout(new BorderLayout());
		renderedComp.setLayout(new BoxLayout(renderedComp, BoxLayout.X_AXIS));
		renderedComp.setBackground(Color.WHITE);
		renderedComp.setBorder(null);
		
		Component defaultComp = this.defaultRenderer.getTreeCellRendererComponent(tree, 
				   																  currentXMLNode.getNodeName(), 
				   																  sel, 
				   																  expanded, 
				   																  leaf, 
				   																  row, 
				   																  hasFocus);
		defaultComp.setMaximumSize(defaultComp.getPreferredSize());
		
		renderedComp.add(defaultComp);
		
		JPanel indicatorPane = new JPanel();
		indicatorPane.setBackground(Color.WHITE);
		indicatorPane.setBorder(null);
		
		JLabel label = null;
		if (!this.view.getValidationResultForNode(currentXMLNode, true).isValid()) {
			label = this.iconProvider.makeErrorLabel();
			indicatorPane.add(label);
		}
		
		if (this.view.getNodesConfiguredByPlugins().containsKey(currentXMLNode)) {
			label = this.iconProvider.makePluginLabel();
			indicatorPane.add(label);
		}
		
		boolean hasAddOption = false;
		boolean hasAddAttrOption = false;
		boolean hasAddChildOption = false;
		boolean hasChoiceOption = false;
		boolean hasRemoveOption = false;
		boolean hasAttrRemoveOption = false;
		
		for (ManipulationOption option : this.view.getManipulationOptionsForNode(currentXMLNode)) {
			switch (option.getKind()) {
			case ADD_ADDITIONAL:
				if (!hasAddOption) {
					label = this.iconProvider.makeAddLabel();
					indicatorPane.add(label);
					hasAddOption = true;
				}
				break;
			case ADD_ATTRIBUTE:
				if (!hasAddAttrOption) {
					label = this.iconProvider.makeAddAttributeLabel();
					indicatorPane.add(label);
					hasAddAttrOption = true;
				}
				break;
			case ADD_CHILD:
				if (!hasAddChildOption) {
					label = this.iconProvider.makeAddChildLabel();
					indicatorPane.add(label);
					hasAddChildOption = true;
				}
				break;
			case CHOICE:
				if (!hasChoiceOption) {
					label = this.iconProvider.makeChoiceLabel();
					indicatorPane.add(label);
					hasChoiceOption = true;
				}
				break;
			case REMOVE:
				if (!hasRemoveOption) {
					label = this.iconProvider.makeRemoveLabel();
					indicatorPane.add(label);
					hasRemoveOption = true;
				}
				break;
			
			/* Commented out because this case only occurs, when the manipulations options for 
			 * an optional attribute are requested - not its owner element's options!          
			 */
//			case REMOVE_ATTRIBUTE:
//				if (optionIndicators.indexOf(XMLTreeRenderer.REMOVE_ATTRIBUTE_INDICATOR) == -1) {
//					optionIndicators.append(XMLTreeRenderer.REMOVE_ATTRIBUTE_INDICATOR);
//				}
//				break;
			default:
				throw new RuntimeException("Undefined ManipulationOptionKind occured!");
			}
		}
		
		if (currentXMLNode.hasAttributes()) {
			NamedNodeMap attrs = currentXMLNode.getAttributes();
			for (int attrNr = 0; attrNr < attrs.getLength(); attrNr++) {
				Attr attr = (Attr) attrs.item(attrNr);
				List<ManipulationOption> options = this.view.getManipulationOptionsForNode(attr);
				if (options != null
					&& options.size() > 0
					&& !hasAttrRemoveOption) {
					
//					ManipulationOption removeAttrOption = options.get(0);
					label = this.iconProvider.makeRemoveAttributeLabel();
					indicatorPane.add(label);
					hasAttrRemoveOption = true;
				}
			}
		}
		
		indicatorPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		indicatorPane.setMaximumSize(indicatorPane.getPreferredSize());
		renderedComp.add(indicatorPane);
		
		Dimension size = new Dimension(renderedComp.getPreferredSize());
		size.width += TreeRenderer.RENDERED_COMP_EXTRA_WIDTH;
		size.height = RENDERED_COMP_HEIGHT;
		renderedComp.setPreferredSize(size);
		renderedComp.setMaximumSize(size);
		
		return renderedComp;
	}

}
