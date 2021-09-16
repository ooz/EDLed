package de.mpg.cbs.edledplugin.stimulus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.media.PlugInManager;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.sourceforge.jffmpeg.CodecManager;

import org.apache.log4j.Logger;

/**
 * Plugin view. Also acts as a fascade for all other gui
 * components, this plugin might use.
 * 
 * @author Oliver Z.
 */
public class StimulusView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -147800815316169556L;
	
	private static final Logger logger = Logger.getLogger(StimulusView.class);
	
	private final StimulusPlugin controller;
	
	private final MediaObjectListPanel mediaObjectPane;
	private final TimetablePanel timetablePane;
	
	private PresentationFrame presentationFrame = null;

	StimulusView(final StimulusPlugin controller) {
		super(new BorderLayout());
		
		this.controller = controller;
		this.mediaObjectPane = new MediaObjectListPanel(this.controller.getModel().getMediaObjectList(), this.controller);
		this.timetablePane = new TimetablePanel(this.controller.getModel().getTimetable());
		
		this.presentationFrame = new PresentationFrame(controller.getModel().getScreen());
		
		this.add(setupToolbar(), BorderLayout.NORTH);
		this.add(setupMediaObjectTimetableSplit(), BorderLayout.CENTER);
		
//		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);

		CodecManager codecManager = new CodecManager();
		
		PlugInManager.addPlugIn("net.sourceforge.jffmpeg.VideoDecoder", codecManager.getSupportedVideoFormats(), codecManager.getSupportedVideoFormats(), PlugInManager.CODEC);
		PlugInManager.addPlugIn("net.sourceforge.jffmpeg.AudioDecoder", codecManager.getSupportedAudioFormats(), codecManager.getSupportedAudioFormats(), PlugInManager.CODEC);
		try {
			PlugInManager.commit();
		} catch (IOException e) {
			logger.warn("Could not load additional audio/video decoders.", e);
		}
	}
	
	private JToolBar setupToolbar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		
		toolbar.setFloatable(false);
		
		final JPanel self = this;
		
		// Show presentation window.
		final JButton presentationButton = new JButton("Presentation");
		presentationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				if (presentationFrame == null) {
//					Dimension screenSize = controller.getModel().getScreenSize();
//					presentationFrame = new PresentationFrame(screenSize);
////					presentationFrame.addMediaObject(new MediaImage("bla", new Point(0, 0), new File("/media/DATA/studies/sem06/praktikum/mpi_svn/gr_bart/Misc/BARTPresentation/google_trends.png")));
////					presentationFrame.addMediaObject(new MediaText("blub", "Text...", 12, Color.WHITE, new Point(200, 400)));
////					presentationFrame.addMediaObject(new MediaVideo("blob", new Point (10, 10), new File("/media/MULE/stuff/futurama_s02e03.avi")));
//				}
				
				presentationFrame.setVisible(true);
				JDialog dialog = DialogFactory.getDefaultFactory().createScreenDialog(self, controller);
				dialog.setVisible(true);
			}
		});
		toolbar.add(presentationButton);
		
		// Add media object button.
		final JButton addMediaObjButton = new JButton("Add media");
		addMediaObjButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = DialogFactory.getDefaultFactory().createMediaObjectDialog(self, controller, null);
				dialog.setVisible(true);
			}
		});
		toolbar.add(addMediaObjButton);
		
		// Add event button.
		final JButton addEventButton = new JButton("Add event");
		addEventButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = DialogFactory.getDefaultFactory().createStimEventDialog(self, controller);
				dialog.setVisible(true);
			}
		});
		toolbar.add(addEventButton);
		
		final JButton playButton = new JButton("Play");
		final JButton stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		// Play/pause/continue button.
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (playButton.getText().equals("Play")) {
					playButton.setText("Pause");
					presentationFrame.setVisible(true);
					controller.startPresentation();
					stopButton.setEnabled(true);
					presentationButton.setEnabled(false);
					addMediaObjButton.setEnabled(false);
					addEventButton.setEnabled(false);
					
				} else if (playButton.getText().equals("Pause")) {
					playButton.setText("Continue");
					controller.pausePresentation();
					
				} else if (playButton.getText().equals("Continue")) {
					playButton.setText("Pause");
					presentationFrame.setVisible(true);
					controller.continuePresentation();
				}
			}
		});
		toolbar.add(playButton);
		
		// Stop presentation button.
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playButton.setText("Play");
				presentationButton.setEnabled(true);
				addMediaObjButton.setEnabled(true);
				addEventButton.setEnabled(true);
				stopButton.setEnabled(false);
				controller.stopPresentation();
			}
		});
		toolbar.add(stopButton);
		
//		toolbar.add(new JButton("Delete"));
//		toolbar.add(new JButton("Edit"));
		return toolbar;
	}
	
	private JSplitPane setupMediaObjectTimetableSplit() {
		JPanel layoutDummy = new JPanel();
		layoutDummy.setLayout(new BoxLayout(layoutDummy, BoxLayout.Y_AXIS));
		layoutDummy.setBackground(Color.BLACK);
		this.timetablePane.setAlignmentY(BOTTOM_ALIGNMENT);
		this.timetablePane.setAlignmentX(LEFT_ALIGNMENT);
		layoutDummy.add(Box.createVerticalGlue());
		layoutDummy.add(this.timetablePane);
		
		JScrollPane mediaObjScrollPane = new JScrollPane(this.mediaObjectPane);
		mediaObjScrollPane.setMinimumSize(new Dimension(100, 100));
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
										  mediaObjScrollPane, 
										  new JScrollPane(layoutDummy));
		split.setDividerLocation(0.5);
		
		return split;
	}
	
	public MediaObjectListPanel getMediaObjectListPanel() {
		return this.mediaObjectPane;
	}
	
//	private class MediaObjectDialog extends JDialog {
//		public MediaObjectDialog() {
//			super();
//			
//			this.setTitle("Add media object");
//		}
//		
//		public MediaObject showAndGetMediaObject() {
//			this.add(new JLabel("add dialog"));
//			
//			this.setVisible(true);
//			
//			return null;
//		}
//	}
	
	void setCurrentTime(final long newCurrentTime) {
		this.timetablePane.setCurrentTime(newCurrentTime);
	}
	
	void startPresentation() {
		this.timetablePane.setHandleMouseEvents(false);
	}
	void pausePresentation() {
		this.presentationFrame.pausePresentation();
	}
	void continuePresentation() {
		this.presentationFrame.continuePresentation();
	}
	void stopPresentation() {
		this.presentationFrame.removeAllMediaObjects();
		this.timetablePane.setCurrentTime(this.controller.getTime());
		this.timetablePane.setHandleMouseEvents(true);
	}
	
	void present(final MediaObject mediaObj) {
		this.presentationFrame.addMediaObject(mediaObj);
	}
	void stopPresentationOf(final MediaObject mediaObj) {
		this.presentationFrame.removeMediaObject(mediaObj);
	}
	
	void updateTimeline() {
		// TODO: implement
	}
	
}
