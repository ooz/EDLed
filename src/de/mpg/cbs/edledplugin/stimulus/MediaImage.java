package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.mpg.cbs.edled.util.Configuration;



public class MediaImage implements MediaObject {
	
	private static final Logger logger = Logger.getLogger(MediaImage.class);
	
	private static final String ICON_FILENAME = "image_icon.png";
	private static final String ICON_ALT_TEXT = "Image"; 
	
	private final String id;
	private String name;
	/** Coordinates of the bottom-left corner of the image. */
	private Point position;
	private File imageFile;
	private Icon icon = null;
	private BufferedImage image = null;
	
	public MediaImage(final String id,
					  final String name,
					  final Point position, 
					  final File imageFile) {
		this.id = id;
		this.name = name;
		this.position = position;
		setImageFile(imageFile);
		
		Configuration config = Configuration.getInstance();
		String imgPath = config.resolveVariables("$IMG_DIR");
		imgPath += Configuration.FILE_SEPARATOR + ICON_FILENAME;
		File iconFile = new File(imgPath);
		if (iconFile.isFile()) {
			try {
				this.icon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Image icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find image icon.");
		}
	}
	
	public MediaImage(final String id, 
			 		  final String name,
			 		  final MediaImage old) {
		this(id, name, old.getVisualPosition(), old.getImageFile());
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
	
	@Override
	public Point getVisualPosition() {
		return this.position;
	}
	@Override
	public void setVisualPosition(final Point newPosition) {
		this.position = newPosition;
	}
	
	public File getImageFile() {
		return this.imageFile;
	}
	public void setImageFile(final File newImageFile) {
		this.imageFile = newImageFile;
		
		try {
			this.image = ImageIO.read(imageFile);
		} catch (IOException e) {
			logger.warn("Could not find/read file " + imageFile.getPath());
		}
	}
	
	@Override
	public MediaObjectKind getKind() {
		return MediaObjectKind.IMAGE;
	}

	@Override
	public void pausePresentation() {
	}

	@Override
	public void present(final JPanel frame, final Graphics g) {
		if (this.image != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(this.image, 
						  this.position.x, 
						  frame.getHeight() - this.position.y - this.image.getHeight(), 
						  null);
		}
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
		return new Dimension(this.image.getWidth(), this.image.getHeight());
	}
	
	@Override
	public Rectangle getVisualRect() {
		return new Rectangle(this.position.x, 
							 this.position.y, 
							 this.image.getWidth(), 
							 this.image.getHeight());
	}
	
	@Override
	public boolean isVisual() {
		return true;
	}

}
