package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.mpg.cbs.edled.util.Configuration;



public class MediaText implements MediaObject {
	
	public final static int DEFAULT_TEXTSIZE = 60;
	
	private static final Logger logger = Logger.getLogger(MediaText.class);
	
	private static final String ICON_FILENAME = "text_icon.png";
	private static final String ICON_ALT_TEXT = "Text";
	
	private final String id;
	private String name;
	private String text;
	private int textSize;

	private Color color;
	/** Coordinates of the bottom-left corner of the text label. */
	private Point position;
	
	private int visualFontWidth;
	private int visualFontHeight;
	
	private Icon icon = null;
	
	public MediaText(final String id,
					 final String name,
			         final String text,
			         final int size, 
			         final Color color, 
			         final Point position) {
		this.id = id;
		this.name = name;
		this.text = text;
		this.textSize = size;
		this.color = color;
		this.position = position;
		
		/* Dummy estimates. Correct numbers are set at the first presentation/rendering. */
		this.visualFontWidth  = text.length() * size;
		this.visualFontHeight = size;
		
		Configuration config = Configuration.getInstance();
		String imgPath = config.resolveVariables("$IMG_DIR");
		imgPath += Configuration.FILE_SEPARATOR + ICON_FILENAME;
		File iconFile = new File(imgPath);
		if (iconFile.isFile()) {
			try {
				this.icon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Text icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find text icon.");
		}
	}
	
	public MediaText(final String id, 
					 final String name, 
					 final MediaText old) {
		
		this(id, name, old.getText(), old.getTextSize(), old.getColor(), old.getVisualPosition());
	}

	@Override
	public void continuePresentation() {
	}

	@Override
	public String getID() {
		return this.id;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	@Override
	public void setName(final String newName) {
		this.name = newName;
	}
	
	public String getText() {
		return this.text;
	}
	public void setText(final String newText) {
		this.text = newText;
	}
	
	public int getTextSize() {
		return this.textSize;
	}
	public void setTextSize(final int newTextSize) {
		this.textSize = newTextSize;
	}
	
	public Color getColor() {
		return this.color;
	}
	public void setColor(final Color newColor) {
		this.color = newColor;
	}
	
	@Override
	public Point getVisualPosition() {
		return this.position;
	}
	@Override
	public void setVisualPosition(final Point newPosition) {
		this.position = newPosition;
	}
	
	@Override
	public MediaObjectKind getKind() {
		return MediaObjectKind.TEXT;
	}

	@Override
	public void pausePresentation() {
	}

	@Override
	public void present(final JPanel frame, final Graphics g) {
		// render text
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(this.color);
		
		Font font = new Font("SansSerif", Font.PLAIN, this.textSize);
		g2d.setFont(font);
		
		this.visualFontWidth  = g2d.getFontMetrics(font).stringWidth(this.text);
		this.visualFontHeight = g2d.getFontMetrics(font).getHeight();
		g2d.drawString(this.text, 
					   this.position.x, 
					   frame.getHeight() - this.position.y);// - this.visualFontHeight);
	}

	@Override
	public void stopPresentation(final JPanel frame) {
	}

	@Override
	public JComponent getPreviewIcon() {
		if (this.icon == null) {
			return new JLabel(ICON_ALT_TEXT);
		}
		
		return new JLabel(this.icon);
	}

	@Override
	public Dimension getVisualSize() {
		return new Dimension(this.visualFontWidth, this.visualFontHeight);
	}

	@Override
	public Rectangle getVisualRect() {
		return new Rectangle(this.position.x, 
							 this.position.y, 
							 this.visualFontWidth, 
							 this.visualFontHeight);
	}

	@Override
	public boolean isVisual() {
		return true;
	}

}
