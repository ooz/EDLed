package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.mpg.cbs.edled.util.Configuration;


public class PresentationFrame extends JFrame implements Observer {
	
	private static final String FRAME_NAME = "Presentation";

	private static final long serialVersionUID = -1425556136329005294L;
	
	private final PresentationPanel presentationPane;
	private final Screen screen;
	
	PresentationFrame(final Screen screen) {
		this.presentationPane = new PresentationPanel();
		this.screen = screen;
		this.screen.addObserver(this);
		
		setLayout(null);
		setupWindow(screen.getSize());
	}
	
	private void setupWindow(final Dimension screenSize) {
		setTitle(PresentationFrame.FRAME_NAME);
		setSize(screenSize);
		setResizable(false);
		setIconImage(Configuration.getInstance().getAppIcon());
		this.setContentPane(this.presentationPane);
	}
	
	@Override
	public void finalize() {
		this.screen.deleteObserver(this);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o == this.screen) {
			setResizable(true);
			Dimension newSize = this.screen.getSize();
			setSize(newSize);
//			setMaximumSize(newSize);
//			setPreferredSize(newSize);
//			setMaximumSize(newSize);
			
			setResizable(false);
			
//			setVisible(true);
		}
	}
	
	void addMediaObject(final MediaObject mediaObj) {
		this.presentationPane.addMediaObject(mediaObj);
	}
	
	void removeMediaObject(final MediaObject mediaObj) {
		this.presentationPane.removeMediaObject(mediaObj);
	}
	
	void removeAllMediaObjects() {
		this.presentationPane.removeAllMediaObjects();
	}
	
	void pausePresentation() {
		this.presentationPane.pausePresentation();
	}
	
	void continuePresentation() {
		this.presentationPane.continuePresentation();
	}
	
	private class PresentationPanel extends JPanel implements MouseListener, MouseMotionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9192257302898401672L;
		
		/** All media objects that are currently presented. */
		private List<MediaObject> mediaObjects;
		private Map<String, Integer> displayCounts;
		
		private Point mouseDownAt = null;
		private Point draggedMouseAt = null;
		
		private MediaObject draggedMediaObject = null;
		private Rectangle draggedMediaObjectFrame = null;
		
		private final Color draggedFrameColor = new Color(0.0f, 0.0f, 0.8f, 0.5f);

		PresentationPanel() {
			setLayout(null);
			this.mediaObjects = new LinkedList<MediaObject>();
			this.displayCounts = new HashMap<String, Integer>();
			setBackground(Color.BLACK);
			this.setDoubleBuffered(true);
			
			addMouseListener(this);
			addMouseMotionListener(this);
			
			repaint();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			synchronized (this) {
				for (MediaObject mediaObj : this.mediaObjects) {
					mediaObj.present(this, g);
				}
			
				if (this.draggedMediaObject != null) {
					Graphics2D g2d = (Graphics2D) g;
					g2d.setColor(this.draggedFrameColor);
					g2d.setBackground(this.draggedFrameColor);
					
					Point movementVector = getMovementVectorFor(this.mouseDownAt, this.draggedMouseAt);
					// Invert y component because EDL uses different (upside down) coordinate space than Java
					movementVector.y = -1 * movementVector.y;
					
					Rectangle draggedFrameToDraw = new Rectangle(this.draggedMediaObjectFrame);
					draggedFrameToDraw.x += movementVector.x;
					draggedFrameToDraw.y += movementVector.y;
					draggedFrameToDraw.y = this.getHeight() - draggedFrameToDraw.y - draggedFrameToDraw.height;
					
					g2d.draw(draggedFrameToDraw);
					g2d.fill(draggedFrameToDraw);
				}
			}
		}
		
		synchronized void addMediaObject(final MediaObject mediaObj) {
			if (this.mediaObjects.contains(mediaObj)) {
				int displayCount = this.displayCounts.get(mediaObj.getID()).intValue();
				displayCount++;
				this.displayCounts.put(mediaObj.getID(), new Integer(displayCount));
			} else {
				this.mediaObjects.add(mediaObj);
				this.displayCounts.put(mediaObj.getID(), new Integer(1));
				
				repaint(0);
			}
		}
		
		synchronized void removeMediaObject(final MediaObject mediaObj) {
			if (this.mediaObjects.contains(mediaObj)) {
				int displayCount = this.displayCounts.get(mediaObj.getID()).intValue();
				displayCount--;
				if (displayCount == 0) {
					mediaObj.stopPresentation(this);
					this.mediaObjects.remove(mediaObj);
					this.displayCounts.remove(mediaObj.getID());
					
					repaint(0);
				} else {
					this.displayCounts.put(mediaObj.getID(), new Integer(displayCount));
				}
			}
		}
		
		synchronized void removeAllMediaObjects() {
			for (MediaObject mediaObj : this.mediaObjects) {
				mediaObj.stopPresentation(this);
			}
			
			this.mediaObjects.clear();
			this.displayCounts.clear();
			this.removeAll();
			
			repaint(0);
		}
		
		synchronized void pausePresentation() {
			for (MediaObject mediaObj : this.mediaObjects) {
				mediaObj.pausePresentation();
			}
		}
		
		synchronized void continuePresentation() {
			for (MediaObject mediaObj : this.mediaObjects) {
				mediaObj.continuePresentation();
			}
		}
		
		/** 
		 * Returns the top-most visual media object that contains point p.
		 * 
		 * @param p	The Point that should be within the visual image of the 
		 * 			requested media object (in SWING coordinate space).
		 * @return  A visual MediaObject (text/image/video) 
		 */
		private synchronized MediaObject mediaObjectForClickedPoint(final Point p) {
			Point transformedPoint = new Point(p.x, 
											   this.getHeight() - p.y);
			
			for (int i = this.mediaObjects.size() - 1;
				 i >= 0;
				 i--) {
				MediaObject mediaObj = this.mediaObjects.get(i);
				if (mediaObj.getVisualRect().contains(transformedPoint)
					&& mediaObj.isVisual()) {
					return mediaObj;
				}
			}
			
			return null;
		}
		
		private Point getMovementVectorFor(final Point from, 
										   final Point to) {
			return new Point(to.x - from.x, to.y - from.y);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.mouseDownAt = e.getPoint();
			this.draggedMouseAt = this.mouseDownAt;
			this.draggedMediaObject = mediaObjectForClickedPoint(this.mouseDownAt);
			if (this.draggedMediaObject != null) {
				this.draggedMediaObjectFrame = this.draggedMediaObject.getVisualRect();
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (this.draggedMediaObject != null) {
				Point movementVector = getMovementVectorFor(this.mouseDownAt, e.getPoint());
				// Invert y component because EDL uses different (upside down) coordinate space than Java
				movementVector.y = -1 * movementVector.y;
				
				Point mediaObjPosition = this.draggedMediaObject.getVisualPosition();
				mediaObjPosition.x += movementVector.x;
				mediaObjPosition.y += movementVector.y;
				
				this.draggedMediaObject.setVisualPosition(mediaObjPosition);
				
				this.mouseDownAt = null;
				this.draggedMouseAt = null;
				this.draggedMediaObject = null;
				this.draggedMediaObjectFrame = null;
				
				repaint();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			this.draggedMouseAt = e.getPoint();
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
	
}
