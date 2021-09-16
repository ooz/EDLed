package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Interface for all media objects.
 * 
 * @author Oliver Z.
 */
public interface MediaObject {
	
	/**
	 * Enum representing the kind/type of the media object.
	 */
	public enum MediaObjectKind {
		TEXT,
		IMAGE,
		AUDIO,
		VIDEO;
	}
	
	/**
	 * Starts the presentation of a media object.
	 * 
	 * @param frame If visual media object: present media object in this frame.
	 * @param g		If visual media object: use this graphics context
	 * 				to draw the media object.
	 */
	public void present(final JPanel frame, final Graphics g);
	
	/**
	 * Pauses the presentation of a continuous media object (audio/video).
	 * Should have no effect on still media objects.
	 */
	public void pausePresentation();
	/**
	 * Continues the presentation of a continuous media object (audio/video).
	 * Should have no effect on still media objects.
	 */
	public void continuePresentation();
	public void stopPresentation(final JPanel frame);
	
	/**
	 * Returns the unique media object identifier (unique despite the media 
	 * object kind!).
	 * 
	 * @return String representing the media object ID.
	 */
	public String getID();
	/**
	 * Returns the descriptive name of the media object.
	 * 
	 * @return String representing the name of the media object.
	 */
	public String getName();
	/**
	 * Sets the name of the media object.
	 * 
	 * @param newName The name string to set.
	 */
	public void setName(final String newName);
	/**
	 * Returns the point coordinate of the bottom-left corner of a visual
	 * media object. Has to be (-1, -1) for non-visual media objects.
	 * 
	 * @return Coordinate of the bottom-left corner. 
	 */
	public Point getVisualPosition();
	/**
	 * Sets a new visual position of a visual media object (should have no
	 * effect on non-visual media objects). 
	 * 
	 * @param newPosition The new visual position to set.
	 */
	public void setVisualPosition(final Point newPosition);
	/**
	 * Returns the visual size of a media object (should be (0, 0) for 
	 * non-visual media objects).
	 * 
	 * @return Dimension representing the visual size of the media object.
	 */
	public Dimension getVisualSize();
	/**
	 * Returns the visual rectangle of a media object (should be (-1, -1, 0, 0)
	 * for non-visual media objects).
	 * 
	 * @return Visual rectangle of the media object.
	 */
	public Rectangle getVisualRect();
	
	/**
	 * Returns the MediaObjectKind of a media object.
	 * 
	 * @return MediaObjectKind of the media object.
	 */
	public MediaObjectKind getKind();
	
	/**
	 * Indicates whether the media object has at least one visual component
	 * (text/image/video).
	 * 
	 * @return Boolean flag indicating whether the media object is visual (true)
	 * 		   or non-visual (false).
	 */
	public boolean isVisual();
	
	/**
	 * Returns an icon for the media object (representing its kind or giving
	 * a preview of its data/information).
	 * 
	 * @return JComponent representing a preview icon of the media object.
	 */
	public JComponent getPreviewIcon();

}
