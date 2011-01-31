package edled.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edled.core.ManipulationOption;
import edled.util.Configuration;

/**
 * Custom TreeCellRenderer rendering nodes in the TreeView (hierarchical view
 * of the XML/EDL document structure). Displays icons indicating validation
 * status and manipulations options next to each node.
 * 
 * @author Oliver Zscheyge
 */
public class TreeRenderer implements TreeCellRenderer {
	/** */
	private static final Logger logger = Logger.getLogger(TreeRenderer.class);
	
	/** Default padding width (space reserved for icons) for rendered nodes. */
	private static final int RENDERED_COMP_EXTRA_WIDTH = 50;
	/** Default height of a rendered node. */
	private static final int RENDERED_COMP_HEIGHT = 20;
	
	/* 
	 * All following unqualified file names will be preceeded either by
	 * ICON_SIZE_MODIFIER_12 or ICON_SIZE_MODIFIER_16. 
	 */
	/** Unqualified file name of the "plugin"-icon. */
	private static final String PLUGIN_FILENAME = "plugin.png";
	/** Alternative text that will be rendered if no "plugin"-icon is available. */
	private static final String PLUGIN_ALT_TEXT = "[P]";
	
	/** Unqualified file name of the "add additional node"-icon.  */
	private static final String ADD_ADDITIONAL_FILENAME = "add.png";
	/** Alternative text that will be rendered if no "add additional node"-icon is available. */
	private static final String ADD_ADDITIONAL_ALT_TEXT = "[+]";
	
	/** Unqualified file name of the "add child node"-icon. */
	private static final String ADD_CHILD_FILENAME = "add_child.png";
	/** Alternative text that will be rendered if no "add child node"-icon is available. */
	private static final String ADD_CHILD_ALT_TEXT = "[#]";
	
	/** Unqualified file name of the "add attribute"-icon. */
	private static final String ADD_ATTRIBUTE_FILENAME = "add_attr.png";
	/** Alternative text that will be rendered if no "add attribute"-icon is available. */
	private static final String ADD_ATTRIBUTE_ALT_TEXT = "[A+]";
	
	/** Unqualified file name of the "XSD/EDLRule validation error"-icon. */
	private static final String ERROR_FILENAME = "error.png";
	/** Alternative text that will be rendered if no "XSD/EDLRule validation error"-icon is available. */
	private static final String ERROR_ALT_TEXT = "[!]";
	
	/** Unqualified file name of the "remove this node"-icon. */
	private static final String REMOVE_FILENAME = "remove.png";
	/** Alternative text that will be rendered if no "remove this node"-icon is available. */
	private static final String REMOVE_ALT_TEXT = "[-]";
	
	/** Unqualified file name of the "node has removable attribute(s)"-icon. */
	private static final String REMOVE_ATTRIBUTE_FILENAME = "rm_attr.png";
	/** Alternative text that will be rendered if no "node has removable attribute(s)"-icon is available. */
	private static final String REMOVE_ATTRIBUTE_ALT_TEXT = "[A-]";
	
	/** Unqualified file name of the "alternative node available"-icon. */
	private static final String CHOICE_FILENAME = "alternative.png";
	/** Alternative text that will be rendered if no "alternative node available"-icon is available. */
	private static final String CHOICE_ALT_TEXT = "[||]";
	
	/** Reference to the view fascade. */
	private View view = null;
	/** Using the DefaultTreeCellRenderer to render the textual part of the node. */
	private DefaultTreeCellRenderer defaultRenderer = null;
	
	/** Icon indicating that the node can be alternatively configured by a plugin. */
	private Icon pluginIcon = null;
	
	/** Icon indicating that additional nodes of the same type can be added. */
	private Icon addIcon = null;
	/** Icon indicating that an optional child node can be added. */
	private Icon addChildIcon = null;
	/** Icon indicating that an optional attribute node can be added. */
	private Icon addAttributeIcon = null;
	/** Icon indicating an XSD or EDLRule violation. */
	private Icon errorIcon = null;
	/** Icon indicating that the node can be deleted. */
	private Icon removeIcon = null;
	/** Icon indicating that the node has attribute nodes that can be deleted. */
	private Icon removeAttributeIcon = null;
	/** Icon indicating that an alternative node can be chosen. */
	private Icon choiceIcon = null;
	
	/** 
	 * Constructor.
	 * 
	 * @param view The view fascade.
	 */
	public TreeRenderer(final View view) {
		this.view = view;
		this.defaultRenderer = new DefaultTreeCellRenderer();
		Configuration config = Configuration.getInstance();
		String imgPath = config.resolveVariables("$IMG_DIR");
		String iconSizeModifier = view.getIconSizeModifier();
		
		// Load plugin icon.
		File iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + PLUGIN_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.pluginIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Plugin icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find plugin icon.");
		}
		
		// Load add icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ADD_ADDITIONAL_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.addIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Add icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find add icon.");
		}
		// Load add child icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ADD_CHILD_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.addChildIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Add child icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find add child icon.");
		}
		// Load add attribute icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ADD_ATTRIBUTE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.addAttributeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Add attribute icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find add attribute icon.");
		}
		// Load error icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ERROR_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.errorIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Error icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find error icon.");
		}
		// Load remove icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + REMOVE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.removeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Remove icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find remove icon.");
		}
		// Load remove attribute icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + REMOVE_ATTRIBUTE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.removeAttributeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Remove attribute icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find remove attribute icon.");
		}
		// Load choice icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + CHOICE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.choiceIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Choice icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find choice icon.");
		}
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
			if (this.errorIcon == null) {
				label = new JLabel(ERROR_ALT_TEXT);
			} else {
				label = new JLabel(this.errorIcon);
			}
			label.setMaximumSize(label.getPreferredSize());
			indicatorPane.add(label);
		}
		
		if (this.view.getNodesConfiguredByPlugins().containsKey(currentXMLNode)) {
			if (this.pluginIcon == null) {
				label = new JLabel(PLUGIN_ALT_TEXT);
			} else {
				label = new JLabel(this.pluginIcon);
			}
			label.setMaximumSize(label.getPreferredSize());
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
					
					if (this.addIcon == null) {
						label = new JLabel(ADD_ADDITIONAL_ALT_TEXT);
					} else {
						label = new JLabel(this.addIcon);
					}
					label.setMaximumSize(label.getPreferredSize());
					indicatorPane.add(label);
					hasAddOption = true;
				}
				break;
			case ADD_ATTRIBUTE:
				if (!hasAddAttrOption) {
					if (this.addAttributeIcon == null) {
						label = new JLabel(ADD_ATTRIBUTE_ALT_TEXT);
					} else {
						label = new JLabel(this.addAttributeIcon);
					}
					label.setMaximumSize(label.getPreferredSize());
					indicatorPane.add(label);
					hasAddAttrOption = true;
				}
				break;
			case ADD_CHILD:
				if (!hasAddChildOption) {
					if (this.addChildIcon == null) {
						label = new JLabel(ADD_CHILD_ALT_TEXT);
					} else {
						label = new JLabel(this.addChildIcon);
					}
					label.setMaximumSize(label.getPreferredSize());
					indicatorPane.add(label);
					hasAddChildOption = true;
				}
				break;
			case CHOICE:
				if (!hasChoiceOption) {
					if (this.choiceIcon == null) {
						label = new JLabel(CHOICE_ALT_TEXT);
					} else {
						label = new JLabel(this.choiceIcon);
					}
					label.setMaximumSize(label.getPreferredSize());
					indicatorPane.add(label);
					hasChoiceOption = true;
				}
				break;
			case REMOVE:
				if (!hasRemoveOption) {
					if (this.removeIcon == null) {
						label = new JLabel(REMOVE_ALT_TEXT);
					} else {
						label = new JLabel(this.removeIcon);
					}
					label.setMaximumSize(label.getPreferredSize());
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
					if (this.removeAttributeIcon == null) {
						label = new JLabel(REMOVE_ATTRIBUTE_ALT_TEXT);
					} else {
						label = new JLabel(this.removeAttributeIcon);
					}
					label.setMaximumSize(label.getPreferredSize());
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
