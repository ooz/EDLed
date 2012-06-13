package de.mpg.cbs.edledplugin.stimulus;

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



public class MediaAudio implements MediaObject {
	
	private static final Logger logger = Logger.getLogger(MediaAudio.class);
	
	private static final String ICON_FILENAME = "audio_icon.png";
	private static final String ICON_ALT_TEXT = "Audio"; 
	
	private final String id;
	private String name;
	private File audioFile;
	private Icon icon = null;
	private Player audioPlayer = null;
	
	public MediaAudio(final String id,
					  final String name,
					  final File audioFile) {
		this.id = id;
		this.name = name;
		setAudioFile(audioFile);
		
		Configuration config = Configuration.getInstance();
		String imgPath = config.resolveVariables("$IMG_DIR");
		imgPath += Configuration.FILE_SEPARATOR + ICON_FILENAME;
		File iconFile = new File(imgPath);
		if (iconFile.isFile()) {
			try {
				this.icon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Audio icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find audio icon.");
		}
		
	}
	
	public MediaAudio(final String id, 
					  final String name, 
					  final MediaAudio old) {
		this(id, name, old.getAudioFile());
	}

	@Override
	public void continuePresentation() {
		if (this.audioPlayer != null) {
			this.audioPlayer.start();
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
	
	public File getAudioFile() {
		return this.audioFile;
	}
	public void setAudioFile(final File newAudioFile) {
		this.audioFile = newAudioFile;
		
		try {
			this.audioPlayer = Manager.createRealizedPlayer(audioFile.toURI().toURL());
		} catch (NoPlayerException e) {
			logger.warn("NoPlayerException", e);
		} catch (CannotRealizeException e) {
			logger.warn("CannotRealizeException", e);
		} catch (MalformedURLException e) {
			logger.warn("Could not find/read file " + audioFile.getPath());
		} catch (IOException e) {
			logger.warn("Could not find/read file " + audioFile.getPath());
		}
	}
	
	@Override
	public MediaObjectKind getKind() {
		return MediaObjectKind.AUDIO;
	}

	@Override
	public void pausePresentation() {
		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
		}
	}

	@Override
	public void present(final JPanel frame, final Graphics g) {
		if (this.audioPlayer != null
			&& this.audioPlayer.getState() != Player.Started) {
			this.audioPlayer.start();
		}
	}

	@Override
	public void stopPresentation(final JPanel frame) {
		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
			this.audioPlayer.setMediaTime(new Time(0.0));
		}
	}

	@Override
	public JComponent getPreviewIcon() {
		if (this.icon == null) {
			return new JLabel(ICON_ALT_TEXT);
		}
		
		return new JLabel(this.icon);
	}

	/**
	 * Returns the point (-1, -1) (e.g. isn't in the visible space).
	 */
	@Override
	public Point getVisualPosition() {
		return new Point(-1, -1);
	}

	@Override
	public void setVisualPosition(Point newPosition) {
		// No effect...
	}

	/**
	 * Returns the dimension (0, 0) since this function is not 
	 * applicable for audio media objects. 
	 */
	@Override
	public Dimension getVisualSize() {
		return new Dimension(0, 0);
	}
	
	@Override
	public Rectangle getVisualRect() {
		return new Rectangle(-1, 
							 -1, 
							 0,
							 0);
	}

	@Override
	public boolean isVisual() {
		return false;
	}

}
