package de.mpg.cbs.edledplugin.stimulus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class TimetablePanel extends JPanel implements Observer, MouseListener, MouseMotionListener, KeyListener {
	
	private enum DragSpot {
		NOT_DRAGGED,
		LOWER_WEST,
		MID_CENTER,
		UPPER_EAST;
	}
	
	private enum TimeUnit {
		MILLISECONDS,
		SECONDS,
		MINUTES;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4421375481242162305L;
	
	/*
	 *  Paddings for the coordinate system.
	 *  Left padding is variable:
	 *  leftPadding = LEFT_CAPTION_PADDING * 2 + <max width of the rendered mediaObjIDs>.
	 */
	private static final int TOP_PADDING = 40;
	private static final int RIGHT_PADDING = 70;
	private static final int BOTTOM_PADDING = 90;
	
	private static final int LEFT_CAPTION_PADDING = 5;
	
	private static final int EVENT_BAR_HEIGHT = 30;
	private static final int DRAGGER_EDGE_LENGTH = (int) Math.floor((double) (EVENT_BAR_HEIGHT - 2) / 3.0);
	
	private static final String TIMETABLE_CAPTION = "Timetable";
	private static final int TIMETABLE_CAPTION_PADDING = 20;
	
	private static final int XAXSIS_DASH_LENGTH = 5;
	
	private final Timetable timetable;
	private long currentTime;
	
	private boolean handleMouseEvents = true;
	
	private StimEvent selectedEvent = null;
//	private Dragger clickedDragger = null;
	private Point mouseDownAt;
	private Point mouseUpAt;
	private DragSpot draggedAt = DragSpot.NOT_DRAGGED;
	private Rectangle lowerWestDragger = null;
	private Rectangle midCenterDragger = null;
	private Rectangle upperEastDragger = null;
	
	private double msPerPixel;
	private TimeUnit timeUnit;
	private double[] msPerPixelValues = {1.0, 10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0, 1200.0, 6000.0};
	private int leftPadding;
	private Font captionFont;
	
	TimetablePanel(final Timetable timetable) {
		this.timetable = timetable;
		this.timetable.addObserver(this);
		
		this.currentTime = 0;
		this.msPerPixel = 100.0;
		this.timeUnit = TimeUnit.SECONDS;
		
		this.captionFont = new Font("SansSerif", Font.PLAIN, 12);
		setBackground(Color.BLACK);
		
		setMillisecondsPerPixel(50.0);
		
		this.lowerWestDragger = new Rectangle(0, 0, DRAGGER_EDGE_LENGTH, DRAGGER_EDGE_LENGTH);
		this.midCenterDragger = new Rectangle(0, 0, DRAGGER_EDGE_LENGTH, DRAGGER_EDGE_LENGTH);
		this.upperEastDragger = new Rectangle(0, 0, DRAGGER_EDGE_LENGTH, DRAGGER_EDGE_LENGTH);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	@Override
	public void finalize() {
		this.timetable.deleteObserver(this);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o == this.timetable) {
			removeAll();
			paintComponent(getGraphics()); // paint(getGraphics()); caused a deadlock - don't know why yet
			revalidate();
		}
	}
	
	public void setCurrentTime(final long newCurrentTime) {
		this.currentTime = newCurrentTime;
		repaint();
		revalidate();
	}
	
	public void setMillisecondsPerPixel(final double msPerPixel) {
		this.msPerPixel = msPerPixel;
		
		if (msPerPixel < 100.0) {
			this.timeUnit = TimeUnit.MILLISECONDS;
		} else if (msPerPixel < 1200.0) {
			this.timeUnit = TimeUnit.SECONDS;
		} else {
			this.timeUnit = TimeUnit.MINUTES;
		}

		clearSelection();
		this.mouseDownAt = null;
		this.mouseUpAt = null;
		
		paint(this.getGraphics());
		revalidate();
	}
	
	public synchronized void setHandleMouseEvents(final boolean flag) {
		this.handleMouseEvents = flag;
		if (!flag) {
			this.mouseDownAt = null;
			this.mouseUpAt = null;
		}
		clearSelection();
	}
	public synchronized boolean isHandlingMouseEvents() {
		return this.handleMouseEvents;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Setting up graphics.
		Graphics2D g2D = (Graphics2D) g;
		g2D.setFont(this.captionFont);
		g2D.setStroke(new BasicStroke(1.0f));
		
		List<String> mediaObjIDs = this.timetable.getMediaObjectIDs();
		FontMetrics fontMetrics = g2D.getFontMetrics();
		
		drawCoordinateSystemAndResize(g2D, mediaObjIDs, fontMetrics);
		drawCaption(g2D);
		drawEventBars(g2D, mediaObjIDs);
		drawDraggers(g2D);
		drawCurrentTimeLine(g2D, fontMetrics);
		drawDraggedArea(g2D, fontMetrics, mediaObjIDs);
		
//		g2d.setBackground(Color.BLACK);
//		g2d.fillRect(getX(), getY(), getWidth(), getHeight());
	}
	
	private void drawCoordinateSystemAndResize(final Graphics2D g2D, 
											   final List<String> mediaObjIDs,
											   final FontMetrics fontMetrics) {

		// Resize based on timetable.
		int maxIDStringWidth = 0;
		for (String mediaObjID : mediaObjIDs) {
			int _IDStringWidth = fontMetrics.stringWidth(mediaObjID);
			if (_IDStringWidth > maxIDStringWidth) {
				maxIDStringWidth = _IDStringWidth;
			}
		}
		
		this.leftPadding = LEFT_CAPTION_PADDING * 2 + maxIDStringWidth;
		int xAxisLength = (int) (this.timetable.getDuration() / this.msPerPixel);
		int yAxisLength = mediaObjIDs.size() * EVENT_BAR_HEIGHT;
		
		int prefWidth = this.leftPadding
						+ xAxisLength
						+ RIGHT_PADDING;
		int prefHeight = TOP_PADDING 
						 + yAxisLength
						 + BOTTOM_PADDING;
		
		Dimension newPreferredSize = new Dimension(prefWidth, prefHeight);
		this.setPreferredSize(newPreferredSize);
		this.setMaximumSize(newPreferredSize);
		
		// Clear.
		g2D.setPaint(Color.BLACK);
		g2D.fillRect(0, 0, prefWidth, prefHeight);
		
		g2D.setPaint(Color.GRAY);
		// x-Axis and its captions.
		g2D.draw(new Line2D.Float(this.leftPadding, TOP_PADDING + yAxisLength,
								  this.leftPadding + xAxisLength, TOP_PADDING + yAxisLength));
		
		for (int curTime = 0; curTime <= xAxisLength; curTime += 10) {
			g2D.draw(new Line2D.Float(curTime + this.leftPadding, TOP_PADDING + yAxisLength,
									  curTime + this.leftPadding, TOP_PADDING + yAxisLength + XAXSIS_DASH_LENGTH));
			if (curTime % 50 == 0) {
				String curTimeString = String.format("%.0f", computeTimeForPixel(curTime, this.timeUnit));
				g2D.drawString(curTimeString, 
							   curTime + this.leftPadding - (fontMetrics.stringWidth(curTimeString) / 2), 
							   TOP_PADDING + yAxisLength + 2 * XAXSIS_DASH_LENGTH + fontMetrics.getHeight());
				g2D.draw(new Line2D.Float(curTime + this.leftPadding, TOP_PADDING + yAxisLength,
						  				  curTime + this.leftPadding, TOP_PADDING + yAxisLength + XAXSIS_DASH_LENGTH * 2));
			} else {
				g2D.draw(new Line2D.Float(curTime + this.leftPadding, TOP_PADDING + yAxisLength,
						  				  curTime + this.leftPadding, TOP_PADDING + yAxisLength + XAXSIS_DASH_LENGTH));
			}
		}
		
		g2D.drawString(generateXAxsisCaptionString(),
				       this.leftPadding + xAxisLength + LEFT_CAPTION_PADDING,
				       TOP_PADDING + yAxisLength);
		
		// y-Axis and its captions.
		g2D.draw(new Line2D.Float(this.leftPadding, TOP_PADDING, 
								  this.leftPadding, TOP_PADDING + yAxisLength));
		
		int captionSpacing = (EVENT_BAR_HEIGHT - fontMetrics.getHeight()) / 2;
		int mediaObjNr = 0;
		for (String mediaObjID : mediaObjIDs) {
			int yCoord = TOP_PADDING + yAxisLength - (mediaObjNr * EVENT_BAR_HEIGHT) - captionSpacing;
			g2D.drawString(mediaObjID, 
						   LEFT_CAPTION_PADDING, 
						   yCoord);
			mediaObjNr++;
		}
	}
	private String generateXAxsisCaptionString() {
		switch (this.timeUnit) {
		case MILLISECONDS:
			return "t in ms";
		case SECONDS:
			return "t in s";
		case MINUTES:
			return "t in min";
		default:
			return "";
		}
	}
	private double computeTimeForPixel(int pixelColumn, TimeUnit unit) {
		switch (unit) {
		case MILLISECONDS:
			return (pixelColumn * this.msPerPixel);
		case SECONDS:
			return pixelColumn * (this.msPerPixel / 1000.0);
		case MINUTES:
			return pixelColumn * (this.msPerPixel / 60000.0);
		default:
			return 0.0;
		}
	}
	
	private void drawCaption(final Graphics2D g2D) {
		g2D.drawString(TIMETABLE_CAPTION, TIMETABLE_CAPTION_PADDING, TIMETABLE_CAPTION_PADDING);
	}
	
	private void drawEventBars(final Graphics2D g2D, 
							   final List<String> mediaObjIDs) {
		
		int numberOfMediaObjs = mediaObjIDs.size();
		int remainingIDs = mediaObjIDs.size() - 1;
		for (String mediaObjID : mediaObjIDs) {
			List<StimEvent> happenedEvents = this.timetable.getHappenedEventsFor(mediaObjID);
			List<StimEvent> eventsToHappen = this.timetable.getEventsToHappenFor(mediaObjID);
			
			for (StimEvent event : happenedEvents) {
				float rbValue = ((float) numberOfMediaObjs - remainingIDs - 1.0f) / (float) numberOfMediaObjs;
				g2D.setColor(new Color(rbValue, 
									   1.0f, 
									   rbValue, 
									   1.0f));
				g2D.fill(new Rectangle((int) (event.time / this.msPerPixel) + this.leftPadding,
									   (remainingIDs * EVENT_BAR_HEIGHT + TOP_PADDING), 
									   (int) (event.duration / this.msPerPixel), 
									   EVENT_BAR_HEIGHT));
			}
			for (StimEvent event : eventsToHappen) {
				float rbValue = ((float) numberOfMediaObjs - remainingIDs - 1.0f) / (float) numberOfMediaObjs;
				g2D.setColor(new Color(rbValue, 
									   1.0f, 
									   rbValue, 
									   1.0f));
				g2D.fill(new Rectangle((int) (event.time / this.msPerPixel) + this.leftPadding,
									   (remainingIDs * EVENT_BAR_HEIGHT + TOP_PADDING), 
									   (int) (event.duration / this.msPerPixel), 
									   EVENT_BAR_HEIGHT));
			}
			
			remainingIDs--;
		}
	}
	
	private void drawDraggers(final Graphics2D g2D) {
		if (selectedEvent != null) {
			g2D.setColor(new Color(0.5f, 0.5f, 0.5f));
			
			g2D.fill(this.lowerWestDragger);
			g2D.fill(this.midCenterDragger);
			g2D.fill(this.upperEastDragger);
		}
	}
	
	private void drawDraggedArea(final Graphics2D g2D, 
								 final FontMetrics fontMetrics,
								 final List<String> mediaObjIDs) {
		
		g2D.setColor(new Color(0.0f, 0.8f, 1.0f, 1.0f));
		
		double yLineEnd = this.getPreferredSize().height - BOTTOM_PADDING + 2 * XAXSIS_DASH_LENGTH;
		if (this.mouseDownAt != null) {
			g2D.draw(new Line2D.Double(this.mouseDownAt.getX(), TOP_PADDING, 
									   this.mouseDownAt.getX(), yLineEnd));
		}
		if (this.mouseUpAt != null) {
			g2D.draw(new Line2D.Double(this.mouseUpAt.getX(), TOP_PADDING, 
									   this.mouseUpAt.getX(), yLineEnd));
		}
		
		if (this.selectedEvent != null
			&& this.draggedAt != DragSpot.NOT_DRAGGED) {
			g2D.setColor(new Color(0.0f, 0.8f, 1.0f, 0.7f));
			
			
			int yCoordDraggedArea = TOP_PADDING + (mediaObjIDs.size() - 1 - mediaObjIDs.indexOf(this.selectedEvent.mediaObject.getID())) * EVENT_BAR_HEIGHT;
			
			if (this.mouseUpAt.x >= this.mouseDownAt.x) {
				g2D.fill(new Rectangle(this.mouseDownAt.x, yCoordDraggedArea, this.mouseUpAt.x - this.mouseDownAt.x, EVENT_BAR_HEIGHT));
			} else {
				g2D.fill(new Rectangle(this.mouseUpAt.x, yCoordDraggedArea, this.mouseDownAt.x - this.mouseUpAt.x, EVENT_BAR_HEIGHT));
			}
		}
		
		// Time captions.
		if (this.mouseDownAt != null) {
			String currentTimeString = String.format("%.1f", computeTimeForPixel(mouseDownAt.x - this.leftPadding, this.timeUnit));
			g2D.drawString(currentTimeString,
						   mouseDownAt.x - (fontMetrics.stringWidth(currentTimeString) / 2), 
						   this.getPreferredSize().height - BOTTOM_PADDING + 2 * XAXSIS_DASH_LENGTH + 3 * fontMetrics.getHeight());
		}
		if (this.mouseUpAt != null) {
			String currentTimeString = String.format("%.1f", computeTimeForPixel(mouseUpAt.x - this.leftPadding, this.timeUnit));
			g2D.drawString(currentTimeString,
						   mouseUpAt.x - (fontMetrics.stringWidth(currentTimeString) / 2), 
						   this.getPreferredSize().height - BOTTOM_PADDING + 2 * XAXSIS_DASH_LENGTH + 4 * fontMetrics.getHeight());
		}
	}
	
	private void drawCurrentTimeLine(final Graphics2D g2D, 
									 final FontMetrics fontMetrics) {
		g2D.setColor(Color.RED);
		int curPixelTime = (int) (this.currentTime / this.msPerPixel);
		
		// Vertical red line.
		g2D.draw(new Line2D.Float(curPixelTime + this.leftPadding, 
								  TOP_PADDING, 
								  curPixelTime + this.leftPadding,
								  this.getPreferredSize().height - BOTTOM_PADDING + 2 * XAXSIS_DASH_LENGTH));
		
		// Caption.
		String currentTimeString = String.format("%.1f", computeTimeForPixel(curPixelTime, this.timeUnit));
		g2D.drawString(currentTimeString,
					   curPixelTime + this.leftPadding - (fontMetrics.stringWidth(currentTimeString) / 2), 
					   this.getPreferredSize().height - BOTTOM_PADDING + 2 * (XAXSIS_DASH_LENGTH + fontMetrics.getHeight()));
		
	}
	
	private StimEvent eventForClickedPoint(final Point p) {
		if (isPointInCoordinateSystem(p)) {
			
			List<String> mediaObjIDs = this.timetable.getMediaObjectIDs();
			int mediaObjNrForEvent = mediaObjIDs.size() - ((p.y - TOP_PADDING) / EVENT_BAR_HEIGHT + 1);
			String mediaObjIDForEvent = mediaObjIDs.get(mediaObjNrForEvent);
			long timeForEvent = (long) ((p.x - this.leftPadding) * this.msPerPixel);
			
			List<StimEvent> allEventsForMediaObj = this.timetable.getHappenedEventsFor(mediaObjIDForEvent);
			allEventsForMediaObj.addAll(this.timetable.getEventsToHappenFor(mediaObjIDForEvent));
			
			for (StimEvent event : allEventsForMediaObj) {
				if (event.time <= timeForEvent 
					&& (event.time + event.duration) >= timeForEvent) {
					
					this.lowerWestDragger.x = ((int) (event.time / this.msPerPixel)) + this.leftPadding;
					this.lowerWestDragger.y = (mediaObjIDs.size() - mediaObjNrForEvent) * EVENT_BAR_HEIGHT + TOP_PADDING - DRAGGER_EDGE_LENGTH;
					
					this.midCenterDragger.x = this.lowerWestDragger.x + (int) ((event.duration / this.msPerPixel) / 2.0) - (int) (DRAGGER_EDGE_LENGTH / 2.0);
					this.midCenterDragger.y = this.lowerWestDragger.y - DRAGGER_EDGE_LENGTH - 1;
					
					this.upperEastDragger.x = ((int) ((event.time + event.duration) / this.msPerPixel)) + this.leftPadding - DRAGGER_EDGE_LENGTH;
					this.upperEastDragger.y = this.lowerWestDragger.y + DRAGGER_EDGE_LENGTH - EVENT_BAR_HEIGHT;
					
					return event;
				}
			}
		}
		
		return null;
	}
	
	private boolean isPointInCoordinateSystem(final Point p) {
		int x = p.x;
		int y = p.y;
		int width = this.getPreferredSize().width;
		int height = this.getPreferredSize().height;
		
		if (x >= this.leftPadding && x < width - RIGHT_PADDING
			&& y > TOP_PADDING && y < height - BOTTOM_PADDING) {
			return true;
		}
		
		return false;
	}
	
	private void clearSelection() {
		this.selectedEvent = null;
		this.draggedAt = DragSpot.NOT_DRAGGED;
	}
	
	// Mouse event handlers.
	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
	@Override
	public void mousePressed(MouseEvent e) {
		requestFocusInWindow();
		
		if (isHandlingMouseEvents()) {	
			this.mouseDownAt = new Point(e.getPoint());
			this.mouseUpAt = new Point(e.getPoint());
			
			if (this.mouseDownAt.x < this.leftPadding) {
				this.mouseDownAt.x = leftPadding;
				this.mouseUpAt.x = this.mouseDownAt.x;
			}
			if (this.mouseDownAt.x > this.getPreferredSize().width - RIGHT_PADDING) {
				this.mouseDownAt.x = this.getPreferredSize().width - RIGHT_PADDING;
				this.mouseUpAt.x = this.mouseDownAt.x;
			}
			
			if (this.selectedEvent == null) {
				/* No event is selected: try to find one at the location
				 * pointed by the user. */
				this.selectedEvent = eventForClickedPoint(mouseDownAt);
				this.draggedAt = DragSpot.NOT_DRAGGED;
			} else {
				if (this.lowerWestDragger.contains(this.mouseDownAt)) {
					this.draggedAt = DragSpot.LOWER_WEST;
				} else if (this.midCenterDragger.contains(this.mouseDownAt)) {
					this.draggedAt = DragSpot.MID_CENTER;
				} else if (this.upperEastDragger.contains(this.mouseDownAt)) {
					this.draggedAt = DragSpot.UPPER_EAST;
				} else {
					clearSelection();
					mousePressed(e);
				}
			}

			repaint();
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isHandlingMouseEvents()) {
			this.mouseUpAt = new Point(e.getPoint());
			
			if (this.mouseUpAt.x < this.leftPadding) {
				this.mouseUpAt.x = leftPadding;
			}
			if (this.mouseUpAt.x > this.getPreferredSize().width - RIGHT_PADDING) {
				this.mouseUpAt.x = this.getPreferredSize().width - RIGHT_PADDING;
			}
			
			if (this.selectedEvent != null
				&& this.draggedAt != DragSpot.NOT_DRAGGED) {
				
				long newTime = -1;
				long newDuration = -1;
				
				if (this.draggedAt == DragSpot.LOWER_WEST) {
					newTime = (long) ((this.mouseUpAt.x - this.leftPadding) * this.msPerPixel);
					newDuration = this.selectedEvent.duration + (this.selectedEvent.time - newTime);
					
				} else if (this.draggedAt == DragSpot.UPPER_EAST) {
					newTime = this.selectedEvent.time;
					newDuration = ((long) ((this.mouseUpAt.x - this.leftPadding) * this.msPerPixel)) - newTime;
					
				} else if (this.draggedAt == DragSpot.MID_CENTER) {
					newTime = ((long) ((this.mouseUpAt.x - this.leftPadding) * this.msPerPixel)) - (this.selectedEvent.duration / 2);
					newDuration = this.selectedEvent.duration;
				}
				
				if (newTime < 0) {
					newTime = 0;
				}
				if (newDuration < 0) {
					newDuration = 0;
				}
				if (newTime + newDuration > this.timetable.getDuration()) {
					newTime = this.timetable.getDuration() - newDuration;
				}
				
				StimEvent newEvent = new StimEvent(newTime, newDuration, this.selectedEvent.mediaObject);
				this.timetable.replace(this.selectedEvent, newEvent);
				
				clearSelection();
			}
			
			repaint();
		}
	}
	
	// Mouse motion event handlers.
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isHandlingMouseEvents()) {
			this.mouseUpAt = e.getPoint();
			
			if (this.mouseUpAt.x < this.leftPadding) {
				this.mouseUpAt.x = leftPadding;
			}
			if (this.mouseUpAt.x > this.getPreferredSize().width - RIGHT_PADDING) {
				this.mouseUpAt.x = this.getPreferredSize().width - RIGHT_PADDING;
			}
			
			repaint();
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	
	// Key event handlers.
	@Override
	public void keyPressed(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_DELETE) {
			if (this.selectedEvent != null) {
				this.timetable.remove(this.selectedEvent);
				clearSelection();
				repaint();
			}
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		
		if (key == '-') {
			for (int i = 0; i < this.msPerPixelValues.length; i++) {
				if (this.msPerPixelValues[i] == this.msPerPixel
						&& (i + 1 < this.msPerPixelValues.length)) {
					setMillisecondsPerPixel(this.msPerPixelValues[i + 1]);
					return;
				}
			}
			
		} else if (key == '+') {
			for (int i = 0; i < this.msPerPixelValues.length; i++) {
				if (this.msPerPixelValues[i] == this.msPerPixel
						&& (i - 1 >= 0)) {
					setMillisecondsPerPixel(this.msPerPixelValues[i - 1]);
					return;
				}
			}
			
		}
	}

}
