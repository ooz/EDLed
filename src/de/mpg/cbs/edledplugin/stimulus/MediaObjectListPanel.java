package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class MediaObjectListPanel extends JPanel implements Observer, KeyListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4141103635719524006L;
	
	private final MediaObjectList mediaObjList;
	private final StimulusPlugin controller;
	
	private final List<MediaObject> selectedMediaObjs;
	private final Map<MediaObject, JPanel> mediaObjPanels;
	
	MediaObjectListPanel(final MediaObjectList mediaObjList,
						 final StimulusPlugin controller) {
		this.mediaObjList = mediaObjList;
		this.mediaObjList.addObserver(this);
		
		this.controller = controller;
		
		this.selectedMediaObjs = new LinkedList<MediaObject>();
		this.mediaObjPanels = new LinkedHashMap<MediaObject, JPanel>();
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		addKeyListener(this);
	}
	
	@Override
	public void finalize() {
		this.mediaObjList.deleteObserver(this);
	}
	
	@Override
	public void paint(Graphics g) {
		for (MediaObject mediaObj : this.mediaObjPanels.keySet()) {
			JPanel mediaObjPanel = this.mediaObjPanels.get(mediaObj);
			if (this.selectedMediaObjs.contains(mediaObj)) {
				mediaObjPanel.setBackground(new Color(200, 200, 255));
			} else {
				mediaObjPanel.setBackground(this.getBackground());
			}
		}
		
		super.paint(g);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.mediaObjList) {
			removeAll();
			
			this.mediaObjPanels.clear();
			
			for (MediaObject mediaObj : this.mediaObjList.getMediaObjects()) {
				JPanel mediaObjPanel = createPanelFor(mediaObj);
				this.mediaObjPanels.put(mediaObj, mediaObjPanel);
				add(mediaObjPanel);
			}
			
			repaint();
			revalidate();
		}
	}
	
	private JPanel createPanelFor(final MediaObject mediaObj) {
		final JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		JComponent icon = mediaObj.getPreviewIcon();
		icon.setAlignmentX(CENTER_ALIGNMENT);
		pane.add(icon);
		
		JLabel idLabel = new JLabel(mediaObj.getID());
		idLabel.setAlignmentX(CENTER_ALIGNMENT);
		idLabel.setForeground(Color.BLACK);
		
		pane.add(idLabel);
		
		JLabel nameLabel = new JLabel(mediaObj.getName());
		nameLabel.setAlignmentX(CENTER_ALIGNMENT);
		nameLabel.setForeground(Color.BLACK);
		pane.add(nameLabel);
		
		pane.setToolTipText("MediaObject named " + mediaObj.getName() + " with ID: " + mediaObj.getID());
		
		pane.setPreferredSize(new Dimension(80, 66));
		
		final Component self = this;
		pane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				requestFocusInWindow();
				
				selectedMediaObjs.clear();
				selectedMediaObjs.add(mediaObj);
				
				// Edit on double click
				if (e.getClickCount() == 2) {
					JDialog dialog = DialogFactory.getDefaultFactory().createMediaObjectDialog(self, controller, mediaObj);
					dialog.setVisible(true);
				}
				
				repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow();
				
				if (e.getButton() == MouseEvent.BUTTON3) {
					
					selectedMediaObjs.clear();
					selectedMediaObjs.add(mediaObj);
					
					JPopupMenu popup = this.buildPopupMenuFor(mediaObj);
					
					Point clickedAt = e.getPoint();
					popup.show(e.getComponent(), clickedAt.x, clickedAt.y);
					
					repaint();
				}
				
				
			}
			
			private JPopupMenu buildPopupMenuFor(final MediaObject mediaObj) {

				JPopupMenu popup = new JPopupMenu();
				
				JMenuItem deleteItem = new JMenuItem("Delete");
				deleteItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mediaObjList.removeMediaObject(mediaObj);
					}
				});
				popup.add(deleteItem);
				
				JMenuItem editItem = new JMenuItem("Edit");
				editItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JDialog dialog = DialogFactory.getDefaultFactory().createMediaObjectDialog(self, controller, mediaObj);
						dialog.setVisible(true);
					}
				});
				popup.add(editItem);
				
				popup.addSeparator();
				JMenuItem closeItem = new JMenuItem("Close");
				popup.add(closeItem);
				
				return popup;
			}
		});
		
//		pane.addKeyListener(new KeyListener() {
//			@Override
//			public void keyTyped(KeyEvent e) {
//			}
//			@Override
//			public void keyReleased(KeyEvent e) {
//				
//			}
//			@Override
//			public void keyPressed(KeyEvent e) {
//			}
//		});
		
		return pane;
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_DELETE) {
			for (MediaObject mediaObj : this.selectedMediaObjs) {
				this.mediaObjList.removeMediaObject(mediaObj);
			}
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
	}

}
