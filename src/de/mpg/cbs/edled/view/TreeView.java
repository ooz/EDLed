package de.mpg.cbs.edled.view;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.ManipulationOption;
import de.mpg.cbs.edled.core.ManipulationOption.ManipulationOptionKind;


/**
 * Custom JTree that displays the XML/EDL document structure in a foldable
 * tree. Various icons indicating manipulation options or can be displayed 
 * next to each tree node.
 * 
 * @author Oliver Z.
 */
public class TreeView extends JTree {

	private static final long serialVersionUID = 7984463671556427199L;
	
	/** Reference to the view fascade. */
	private View view = null;
	
	/**
	 * Alternative to "this"-keyword 
	 * (for use in anonymous and inner class definitions). 
	 */
	private TreeView self;
	/** 
	 * InspectorPanel displaying the details the currently selected node
	 * in this TreeView.
	 */
	private InspectorPanel inspector;
	
	/**
	 * Constructor.
	 * 
	 * @param edlTree           Underlying model for the TreeView/JTree.
	 * @param edlInspectorPanel InspectorPanel showing detailed information
	 * 							about the currently selected node.
	 * @param view				The view fascade.
	 */
	public TreeView(final TreeModel edlTree, 
					final InspectorPanel edlInspectorPanel, 
					final View view) {
		super(edlTree);
		
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setShowsRootHandles(false);
		this.view = view;
		
//		JComboBox comboBox = new JComboBox(new String[]{"A","B","C"});
//	    TreeCellEditor editor = new DefaultCellEditor(comboBox);
//
//	    this.setEditable(true);
//	    this.setCellEditor(editor);
		
		this.inspector = edlInspectorPanel;
		this.self = this;
		
		// React to node selection: show node properties in inspector panel
		this.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) self.getLastSelectedPathComponent();
				if (selectedNode == null) {
					return;
				}
				Node selectedXMLNode = (Node) selectedNode.getUserObject();
				
				inspector.showNodeInfo(selectedXMLNode, 
									   self.view.getMetaXMLNodeForNode(selectedXMLNode));
			}
		});
		
		// Listening for popup triggers in order to show the manipulation popup.
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					
					int selRow = self.getRowForLocation(e.getX(), e.getY());
			        if(selRow != -1) {
			        	self.setSelectionRow(selRow);
			        }
			        
			        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) self.getLastSelectedPathComponent();
					if (selectedNode == null) {
						return;
					}
					
					JPopupMenu popup = this.buildPopupMenuFor(selectedNode);
					
					Point clickedAt = e.getPoint();
					popup.show(e.getComponent(), clickedAt.x, clickedAt.y);
				}
			}

			private JPopupMenu buildPopupMenuFor(final DefaultMutableTreeNode selectedNode) {

				Node selectedXMLNode = (Node) selectedNode.getUserObject();
				List<ManipulationOption> options = self.view.getManipulationOptionsForNode(selectedXMLNode);
				
				JPopupMenu popup = new JPopupMenu();
				JMenuItem popupItem;
				
				for (ManipulationOption option : options) {
					if (option.getKind() != ManipulationOptionKind.ADD_ATTRIBUTE) {
						popupItem = new JMenuItem(option.getOptionDescription());
						popupItem.addActionListener(new MenuItemActionListener(option));
						popup.add(popupItem);
					}
				}
				
				popupItem = new JMenuItem("Close");
				popupItem.addActionListener(new MenuItemActionListener(null));
				popup.add(popupItem);
				
				return popup;
			}
		});

		this.setCellRenderer(new TreeRenderer(this.view));
	}

	/**
	 * ActionListener that executes the selected option from the node context
	 * menu (right clicking on a node in the TreeView shows the menu).
	 */
	private class MenuItemActionListener implements ActionListener {
		
		private ManipulationOption option;

		/**
		 * Constructs a new MenuItemActionListner object.
		 * 
		 * @param option The ManipulationOption that is executed when the
		 * 				 appropriate menu item was chosen. Nothing happens
		 * 				 if null is passed (safe).
		 */
		public MenuItemActionListener(final ManipulationOption option) {
			this.option = option;
		}

		/**
		 * Method to call when a popup menu item is selected.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (this.option != null) {
				this.option.execute();
//				self.repaint();
				if (this.option.getKind() == ManipulationOptionKind.REMOVE
					|| this.option.getKind() == ManipulationOptionKind.CHOICE) {
					self.inspector.showNodeInfo(null, null);
				} else {
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) self.getLastSelectedPathComponent();
					if (selectedNode == null) {
						self.inspector.showNodeInfo(null, null);
					}
					Node selectedXMLNode = (Node) selectedNode.getUserObject();
					
					inspector.showNodeInfo(selectedXMLNode, 
										   self.view.getMetaXMLNodeForNode(selectedXMLNode));
				}
			}
		}
	}
	
}
