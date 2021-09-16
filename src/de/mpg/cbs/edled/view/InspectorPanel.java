package de.mpg.cbs.edled.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpringLayout;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.ManipulationOption;
import de.mpg.cbs.edled.core.RuleViolationNotification;
import de.mpg.cbs.edled.core.ManipulationOption.ManipulationOptionKind;
import de.mpg.cbs.edled.core.metatree.MetaNode;
import de.mpg.cbs.edled.core.metatree.NodeConstraint;
import de.mpg.cbs.edled.core.validation.EDLRule;
import de.mpg.cbs.edled.core.validation.ValidationResult;
import de.mpg.cbs.edled.util.Configuration;
import de.mpg.cbs.edled.util.FileUtility;
import de.mpg.cbs.edled.util.InputMethodMap;
import de.mpg.cbs.edled.util.InputMethodMap.InputMethod;
import de.mpg.cbs.edled.xml.XMLUtility;


/**
 * Panel that is shown on the right hand side (general view).
 * Shows detailed information (value, attributes, 
 * 
 * @author Oliver Z.
 */
public class InspectorPanel extends JPanel implements TreeReceiver {

	private static final long serialVersionUID = 1L;

	/** Width for all textfields showing node values. */
	private static final int TEXTFIELD_WIDTH = 20;

	private static final int ATTR_FIELD_PADDING = 10;
	
	/** Default file name shown in the file chooser caller button. */
	private static final String DEFAULT_FILE_NAME = "...";

	/** Reference to the view fascade. */
	protected final View view;

	/** 
	 * Reference to the JTree displaying the EDL/XML document structure 
	 * (located left of the InspectorPanel).
	 */
	private JTree tree = null;
	private SpringLayout layout;
	
	/** Hand (link) cursor. */
	private final static Cursor HAND = new Cursor(Cursor.HAND_CURSOR);
	/** Normal mouse pointer. */
	private final static Cursor NORMAL = new Cursor(Cursor.DEFAULT_CURSOR);
	
	/** Icon indicating that mouseovering the icon or associated node name label
	 *  will show the node description/annotation. */
	private Icon infoIcon = null;

	/** Constructor. */
	public InspectorPanel(final View view) {
		this.view = view;
		this.layout = new SpringLayout();
		this.setLayout(this.layout);
		
		
		// Load plugin icon.
		IconProvider ip = IconProvider.getInstance();
		this.infoIcon = ip.getInfoIcon();
	}

	@Override
	public void setTree(JTree tree) {
		this.tree = tree;
	}

	/** 
	 * Displays detailed information (value, type, attributes, etc.) about
	 * one node.
	 * 
	 * @param xmlNode  The node to display in detail.
	 * @param metaNode MetaNode of parameter xmlNode.
	 */
	public void showNodeInfo(final Node xmlNode, final MetaNode metaNode) {
		// Clear view.
		this.removeAll();
		this.repaint();

		// Keep showing nothing if null parameters were passed.
		if (xmlNode == null || metaNode == null) {
			this.revalidate();
			return;
		}

		if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {

			NodeConstraint edlNodeConstraint = metaNode.getConstraint();

			// Build node name (heading).
			JPanel nodeNamePane = new JPanel();
			Component prefComp = nodeNamePane;
			final JLabel nodeNameLabel;
			String labelText = "<html><b>" + xmlNode.getNodeName() + "</b></html>";
			if (edlNodeConstraint.hasAppInfo()) {
				if (this.infoIcon == null) {
					labelText = "<html><b>" + IconProvider.INFO_ALT_TEXT + " " + xmlNode.getNodeName() + "</b></html>";
					nodeNameLabel = new JLabel(labelText);
				} else {
					nodeNameLabel = new JLabel(labelText, this.infoIcon, JLabel.TRAILING);
				}
				
				// Add node description tooltip to nodeNameLabel.
				String nodeDescription = edlNodeConstraint.getAppInfo();
				String tooltipText = nodeDescription.replace("\n", "<br>");
				tooltipText = "<html>" + tooltipText + "</html>";
				nodeNameLabel.setToolTipText(tooltipText);
				nodeNameLabel.addMouseListener(buildTooltipMouseListener(nodeDescription));
			} else {
				nodeNameLabel = new JLabel(labelText);
			}
			
			ValidationResult validationResult = view
					.getValidationResultForNode(xmlNode, false);
			if (!validationResult.isValid()) {
				nodeNameLabel.setForeground(Color.RED);
				printViolatedRules(validationResult);
			}
			nodeNamePane.add(nodeNameLabel);
			this.add(nodeNamePane);
			this.layout.putConstraint(SpringLayout.NORTH, nodeNamePane, 0,
					SpringLayout.NORTH, this);
			this.layout.putConstraint(SpringLayout.WEST, nodeNamePane, 0,
					SpringLayout.WEST, this);

			// Build text field for text value of node (also showing type).
			if (edlNodeConstraint.canHaveTextContent()) {
				nodeNamePane.add(new JLabel("("
						+ edlNodeConstraint.getTypeName() + ")"));

				JPanel nodeValuePane = new JPanel();

				Component nodeValueField = buildCompForNode(xmlNode, metaNode,
														    nodeNameLabel);

				this.layout.putConstraint(SpringLayout.NORTH, nodeValuePane, 0,
						SpringLayout.SOUTH, prefComp);
				this.layout.putConstraint(SpringLayout.WEST, nodeValuePane, 0,
						SpringLayout.WEST, this);
				nodeValuePane.add(nodeValueField);
				this.add(nodeValuePane);
				prefComp = nodeValuePane;
			}

			// Show node attributes and buttons for optional removal of those.
			// TODO: only show attributes, when not only whitelisted attributes
			// are availiable
			Map<String, MetaNode> metaAttributes = metaNode.getAttributes();
			NamedNodeMap nodeAttributes = xmlNode.getAttributes();
			int attrCount = nodeAttributes.getLength();
			if (attrCount > 0) {

				JLabel attrHeader = new JLabel("<html><b>Attributes:</b></html>");
				this.add(attrHeader);
				this.layout.putConstraint(SpringLayout.NORTH, attrHeader,
						ATTR_FIELD_PADDING, SpringLayout.SOUTH, prefComp);
				this.layout.putConstraint(SpringLayout.WEST, attrHeader,
						ATTR_FIELD_PADDING, SpringLayout.WEST, this);
				prefComp = attrHeader;

				int attrNr = 0;
				while (attrNr < attrCount) {
					final Node currentAttribute = nodeAttributes.item(attrNr);

					if (this.view.isWhitelisted(currentAttribute)) {
						// TODO: DONT USE CONTINUE!!!
						attrNr++;
						continue;
					}

					MetaNode attrMetaNode = metaAttributes.get(currentAttribute
							.getNodeName());
					NodeConstraint attrConstraint = attrMetaNode
							.getConstraint();

					// Attribute name.
					JPanel attrNamePane = new JPanel();
					final JLabel attrNameLabel = new JLabel(currentAttribute
							.getNodeName());
					ValidationResult attrValidationResult = view
							.getValidationResultForNode(currentAttribute, false);
					if (!attrValidationResult.isValid()) {
						attrNameLabel.setForeground(Color.RED);
						printViolatedRules(attrValidationResult);
					}
					attrNamePane.add(attrNameLabel);

					if (attrConstraint.hasTypeName()) {
						attrNamePane.add(new JLabel("("
								+ attrConstraint.getTypeName() + ")"));
					}
					this.add(attrNamePane);
					this.layout.putConstraint(SpringLayout.NORTH, attrNamePane,
							0, SpringLayout.SOUTH, prefComp);
					this.layout.putConstraint(SpringLayout.WEST, attrNamePane,
							ATTR_FIELD_PADDING, SpringLayout.WEST, this);
					prefComp = attrNamePane;

					// Pane for attribute value field + optional remove button.
					JPanel attrValuePane = new JPanel();

					Component attrValueComp = buildCompForNode(
							currentAttribute, attrMetaNode, attrNameLabel);
					attrValuePane.add(attrValueComp);

					// Show removal button after attribute value field.
					final List<ManipulationOption> attrOptions = this.view
							.getManipulationOptionsForNode(currentAttribute);
					if (!attrOptions.isEmpty()) {
						JButton removeButton = new JButton("remove");
						attrValuePane.add(removeButton);
						removeButton.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								attrOptions.get(0).execute();
								showNodeInfo(xmlNode, metaNode);

								if (tree != null) {
									tree.repaint();
								}
							}
						});
					}

					this.add(attrValuePane);
					this.layout.putConstraint(SpringLayout.NORTH,
							attrValuePane, 0, SpringLayout.SOUTH, prefComp);
					this.layout.putConstraint(SpringLayout.WEST, attrValuePane,
							ATTR_FIELD_PADDING, SpringLayout.WEST, this);
					prefComp = attrValuePane;

					attrNr++;
				}
			}

			// Show buttons for addition of optional attributes.
			for (ManipulationOption _option : this.view
					.getManipulationOptionsForNode(xmlNode)) {
				final ManipulationOption option = _option;
				if (option.getKind() == ManipulationOptionKind.ADD_ATTRIBUTE) {
					JPanel attrAddPane = new JPanel();
					JButton addButton = new JButton(option
							.getOptionDescription());
					attrAddPane.add(addButton);
					addButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							option.execute();
							showNodeInfo(xmlNode, metaNode);

							if (tree != null) {
								tree.repaint();
							}
						}
					});

					this.add(attrAddPane);
					this.layout.putConstraint(SpringLayout.NORTH, attrAddPane,
							0, SpringLayout.SOUTH, prefComp);
					this.layout.putConstraint(SpringLayout.WEST, attrAddPane,
							ATTR_FIELD_PADDING, SpringLayout.WEST, this);
					prefComp = attrAddPane;
				}
			}
		}

		// Recalculate the preferred size of this JPanel since SpringLayout
		// won't do this
		// automatically based on the given constraints. -_-
		int width = 0;
		int height = 0;
		for (Component comp : this.getComponents()) {
			SpringLayout.Constraints constraints = layout.getConstraints(comp);
			int westPadding = constraints.getConstraint(SpringLayout.WEST)
					.getPreferredValue();
			int compWidth = comp.getPreferredSize().width;
			int compHeight = comp.getMinimumSize().height;

			if (westPadding + compWidth > width) {
				width = westPadding + compWidth;
			}
			height += compHeight;
		}
		height += InspectorPanel.ATTR_FIELD_PADDING;
		this.setPreferredSize(new Dimension(width, height));

		this.revalidate();
	}

	/**
	 * Builds the value input component (e.g. textfield, drop down, file chooser
	 * button, etc.) for one node.
	 * 
	 * @param node
	 * @param metaNode
	 * @param nodeLabel
	 * @return
	 */
	private Component buildCompForNode(final Node node,
									   final MetaNode metaNode,
									   final JLabel nodeLabel) {
		InputMethodMap imp = Configuration.getInstance().getInputMethodMap();
		NodeConstraint constraint = metaNode.getConstraint();
		String nodeText = XMLUtility.getNodeValue(node);
		
		InputMethod method = imp.getMethodFor(node, constraint);
		
		Component comp = null;
		boolean directoriesOnly = false;
		switch (method) {
		case DIRCHOOSER:
			directoriesOnly = true;
			// Fall through because a directory chooser is basically a file chooser
		case FILECHOOSER:
			JPanel filePanel = new JPanel();
			JTextField filePathField = new JTextField(nodeText, 
													  InspectorPanel.TEXTFIELD_WIDTH);
			filePathField.setForeground(nodeLabel.getForeground());
			filePathField.addKeyListener(buildTextFieldKeyListener(node, nodeLabel, filePathField));
			JButton fileChooserCaller = new JButton(DEFAULT_FILE_NAME);
			fileChooserCaller.addActionListener(buildFileChooserActionListener(
													node, 
													filePathField, 
													fileChooserCaller, 
													directoriesOnly));
			
			filePanel.add(filePathField);
			filePanel.add(fileChooserCaller);
			comp = filePanel;//fileChooserCaller;
			break;
		case COLORCHOOSER:
			// TODO: implement and insert break when finished! ;)
		default:
			// NOT_SPECIFIED and TEXTFIELD result in a textfield
			// (or drop down menu if an enumeration type is used)
			List<String> valueEnum = null;
			if (constraint.hasTypeRestriction()) {
				valueEnum = constraint.getTypeRestriction().getEnumeration();
			}

			if (valueEnum == null) {
				// textfield
				final JTextField textfield = new JTextField(nodeText,
						InspectorPanel.TEXTFIELD_WIDTH);
				textfield.setForeground(nodeLabel.getForeground());

				if (constraint.hasDefaultValue()
						&& textfield.getText().trim().compareTo("") == 0) {
					textfield.setText(constraint.getDefaultValue());
				}
				if (constraint.hasFixedValue()) {
					textfield.setText(constraint.getFixedValue());
					textfield.setEditable(false);
				} else {
					textfield.addKeyListener(buildTextFieldKeyListener(node,
							nodeLabel, textfield));
				}
				// textfield.addActionListener(new ActionListener() {
				// @Override
				// public void actionPerformed(ActionEvent e) {
				// // Check type restriction
				// node.setTextContent(textFieldTextToNodeText(textfield.getText()));
				// }
				// });

				comp = textfield;
			} else {
				// drop down menu
				List<String> valueEnumCopy = new LinkedList<String>(valueEnum);
				valueEnumCopy.add(0, "");
				JComboBox combobox = new JComboBox(valueEnumCopy.toArray());
				combobox.setSelectedIndex(0);
				for (String possibleValue : valueEnumCopy) {
					if (nodeText.compareTo(possibleValue) == 0) {
						combobox.setSelectedIndex(valueEnumCopy
								.indexOf(possibleValue));
					}
				}

				combobox.addActionListener(buildComboBoxActionListener(node,
						nodeLabel));
				comp = combobox;
			}
		}

		return comp;
	}

	/**
	 * Builds the KeyListener for JTextField inputs.
	 * 
	 * @param node		The DOM node whose value will be changed according to
	 * 					the text field input.
	 * @param label		JLabel accompanying the text field input (usually 
	 * 					displaying the name of node).
	 * @param textfield The text field input.
	 * @return			A KeyListener updating and revalidating the value of 
	 * 					node each time the value of textfield is changed.
	 * 					Side effect: the text color of label and textfield is
	 * 					changed to black (red) when the node value is valid
	 * 					(invalid).
	 */
	private KeyListener buildTextFieldKeyListener(final Node node,
			final JLabel label, 
			final JTextField textfield) {
		
		return new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_SHIFT
						|| keyCode == KeyEvent.VK_CONTROL
						|| keyCode == KeyEvent.VK_ALT
						|| keyCode == KeyEvent.VK_ALT_GRAPH) {
					return;
				}
				
				view.setNodeValue(node, textfield.getText());
				ValidationResult validationResult = view
						.getValidationResultForNode(node, false);
				if (validationResult.isValid()) {
					label.setForeground(Color.BLACK);
					textfield.setForeground(Color.BLACK);
				} else {
					label.setForeground(Color.RED);
					textfield.setForeground(Color.RED);
					printViolatedRules(validationResult);
				}

				if (tree != null) {
					tree.repaint();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) { }
			@Override
			public void keyTyped(KeyEvent e)   { }
		};
	}

	/**
	 * Builds the ActionListener for combo box inputs.
	 * 
	 * @param node  The DOM node whose value will be changed according to the
	 * 				combo box selection.
	 * @param label JLabel accompanying the combo box (usually displaying the
	 * 				name of node).
	 * @return		A ActionListener that should be assigned to a combo box.
	 *              The action listener will be updating and revalidating the 
	 *              value of node each time the value of the combo box is 
	 *              changed.
	 * 				Side effect: the text color of label is changed to black 
	 * 			    (red) when the node value is valid (invalid).
	 */
	private ActionListener buildComboBoxActionListener(final Node node,
													   final JLabel label) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox comboBox = (JComboBox) e.getSource();
				view.setNodeValue(node, (String) comboBox.getSelectedItem());

				ValidationResult validationResult = view
						.getValidationResultForNode(node, false);
				if (validationResult.isValid()) {
					label.setForeground(Color.BLACK);
				} else {
					label.setForeground(Color.RED);
					printViolatedRules(validationResult);
				}

				if (tree != null) {
					tree.repaint();
				}
			}
		};
	}

	/**
	 * Builds the ActionListener for buttons that should launch file chooser
	 * inputs.
	 * 
	 * @param node	          The DOM node whose value will be changed according 
	 * 						  to the selected file (path).
	 * @param pathField       JTextField that displays the (editable) file path.
	 * @param button 		  JButton that pops up the file chooser dialog when
	 * 						  pressed (the returned action listener should be 
	 * 						  assigned to this button). The button also shows
	 * 						  the current value of node as its test.
	 * @param directoriesOnly Flag indicating whether the file chooser should
	 * 						  select directories (true)
	 * 						  or   regular files (false).
	 * @return				  A ActionListener launching a file chooser. Should
	 * 						  be assigned to argument button. The generated
	 * 						  file chooser changes the value of node to the 
	 * 						  selected file.
	 */
	private ActionListener buildFileChooserActionListener(final Node node,
														  final JTextField pathField,
														  final JButton button,
														  final boolean directoriesOnly) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File currentFile = view.getCurrentFile();
				JFileChooser fileChooser;
				if (currentFile == null) {
					// Open file chooser in current working directory.
					fileChooser = new JFileChooser(Configuration.getInstance()
							.getSysProp("user.dir"));
				} else {
					// Same directory as currentFile.
					fileChooser = new JFileChooser(currentFile.getParentFile());
				}
				
				if (directoriesOnly) {
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}

				if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
					String choosenPath = fileChooser.getSelectedFile()
							.getPath();
					String relativizedPath;
					if (currentFile == null) {
						relativizedPath = choosenPath;
					} else {
						relativizedPath = FileUtility.relativize(choosenPath,
								currentFile.getAbsolutePath(),
								Configuration.FILE_SEPARATOR);
					}

					if (directoriesOnly 
						&& !relativizedPath.endsWith(Configuration.FILE_SEPARATOR)) {
						relativizedPath += Configuration.FILE_SEPARATOR;
					}
					
					pathField.setText(relativizedPath);
					view.setNodeValue(node, relativizedPath);
				}
			}
		};
	}
	
	private MouseListener buildTooltipMouseListener(final String text) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				view.showNodeDescription(text);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(HAND);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(NORMAL);
			}
		};
	}

	/**
	 * Prints the message part of all violated EDL rules to the message area.
	 * 
	 * @param validationResult ValidationResult of a DOM node.
	 */
	private void printViolatedRules(final ValidationResult validationResult) {
		for (EDLRule violatedRule : validationResult.getViolatedRules()) {
			this.view.show(new RuleViolationNotification(violatedRule));
		}
	}

}
