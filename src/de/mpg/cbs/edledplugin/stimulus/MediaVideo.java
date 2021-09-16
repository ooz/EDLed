package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.mpg.cbs.edled.util.Configuration;



public class MediaVideo implements MediaObject {
	
	private static final Logger logger = Logger.getLogger(MediaVideo.class);
	
	private static final String ICON_FILENAME = "video_icon.png";
	private static final String ICON_ALT_TEXT = "Video"; 
	
	private final String id;
	private String name;
	/** Coordinates of the bottom-left corner of the video frame. */
	private Point position;
	private File videoFile;
	private Icon icon = null;
	private Player videoPlayer = null;
	
	public MediaVideo(final String id,
					  final String name,
					  final Point position,
					  final File videoFile) {
		this.id = id;
		this.name = name;
		this.position = position;
		setVideoFile(videoFile);
		
		Configuration config = Configuration.getInstance();
		String imgPath = config.resolveVariables("$IMG_DIR");
		imgPath += Configuration.FILE_SEPARATOR + ICON_FILENAME;
		File iconFile = new File(imgPath);
		if (iconFile.isFile()) {
			try {
				this.icon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Video icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find video icon.");
		}
	}
	
	public MediaVideo(final String id, 
					  final String name, 
					  final MediaVideo old) {
		this(id, name, old.getVisualPosition(), old.getVideoFile());
	}

	@Override
	public void continuePresentation() {
		if (this.videoPlayer != null) {
			this.videoPlayer.start();
		}
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
	
	public File getVideoFile() {
		return this.videoFile;
	}
	public void setVideoFile(final File newVideoFile) {
		this.videoFile = newVideoFile;
		
		try {
			this.videoPlayer = Manager.createRealizedPlayer(videoFile.toURI().toURL());

			this.videoPlayer.realize();
			this.videoPlayer.prefetch();
			
			Component video = this.videoPlayer.getVisualComponent();
			if (video != null) {
				video.enableInputMethods(false);
				video.setEnabled(false);
				video.setFocusable(false);
			}
			
		} catch (NoPlayerException e) {
			logger.warn("NoPlayerException", e);
		} catch (CannotRealizeException e) {
			logger.warn("CannotRealizeException", e);
		} catch (MalformedURLException e) {
			logger.warn("Could not find/read file " + videoFile.getPath());
		} catch (IOException e) {
			logger.warn("Could not find/read file " + videoFile.getPath());
		}
	}
	
	@Override
	public MediaObjectKind getKind() {
		return MediaObjectKind.VIDEO;
	}

	@Override
	public void pausePresentation() {
		if (this.videoPlayer != null) {
			this.videoPlayer.stop();
		}
	}

	@Override
	public void present(final JPanel frame, final Graphics g) {
		if (this.videoPlayer != null
			&& this.videoPlayer.getState() != Player.Started) {
			
			Component video = this.videoPlayer.getVisualComponent();
			if (video != null) {
			
				frame.add(video);
				Dimension prefSize = video.getPreferredSize();
				video.setBounds(this.position.x, 
								frame.getHeight() - this.position.y - prefSize.height, 
								prefSize.width, 
								prefSize.height);
				
				this.videoPlayer.start();
//				frame.repaint();
			}
		}
	}

	@Override
	public void stopPresentation(final JPanel frame) {
		if (this.videoPlayer != null) {
			this.videoPlayer.stop();
			this.videoPlayer.setMediaTime(new Time(0.0));
			
			Component video = this.videoPlayer.getVisualComponent();
			if (video != null) {
				frame.remove(video);
			}
		}
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
		Component video = this.videoPlayer.getVisualComponent();
		if (video != null) {
			return new Dimension(video.getPreferredSize());
		}
		
		return new Dimension(0, 0);
	}
	
	@Override
	public Rectangle getVisualRect() {
		int width = 0;
		int height = 0;
		
		Component video = this.videoPlayer.getVisualComponent();
		if (video != null) {
			Dimension size = video.getPreferredSize();
			width = size.width;
			height = size.height;
		}
		
		return new Rectangle(this.position.x, 
							 this.position.y, 
							 width, 
							 height);
	}
	
	@Override
	public boolean isVisual() {
		return true;
	}

}
