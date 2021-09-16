package de.mpg.cbs.edledplugin.stimulus;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.Application;
import de.mpg.cbs.edled.core.Model;
import de.mpg.cbs.edled.plugin.Plugin;
import de.mpg.cbs.edled.plugin.ReplacementManager;
import de.mpg.cbs.edled.util.Configuration;


/**
 * Plugin controller.
 * 
 * @author Oliver Z.
 */
public class StimulusPlugin implements Plugin {
	
	private static final Logger LOGGER = Logger.getLogger(StimulusPlugin.class);
	
	private static final String SCREEN_KEY = "SCREEN";
	private static final String MEDIAOBJECTLIST_KEY = "MEDIAOBJECTLIST";
	private static final String TIMETABLE_KEY = "TIMETABLE";
	
	private static final String NAME = "Stimuli";
	private static final String AUTHOR = "Oliver Z.";
	private static final String DESCRIPTION = "Plugin for graphical creation and preview of a fMRI stimulus presentation";
	private static final String VERSION = "1.2.0";
	
	private Application appController = null;
	private final StimulusView pluginView;
	private final StimulusData pluginModel;
	
	private final ReplacementManager nodeMapper;
	
	private Thread presentationThread;
	private long time;
	private List<StimEvent> eventsToEnd;
	
	public StimulusPlugin() {
		this.pluginModel = new StimulusData();
		this.pluginView = new StimulusView(this);
		
		this.presentationThread = null;
		this.time = 0;
		this.eventsToEnd = new LinkedList<StimEvent>();
		
		Configuration config = Configuration.getInstance();
		File mapFile = new File(config.resolveVariables("$PLUGIN_DIR") + Configuration.FILE_SEPARATOR + getQualifiedName() + ".map");
		this.nodeMapper = ReplacementManager.createFrom(mapFile);
		
//		Map<String, XPathExpression> modelToNode = new LinkedHashMap<String, XPathExpression>();
//		XPath xpath = XPathFactory.newInstance().newXPath();
//		
//		try {
//			modelToNode.put(SCREEN_KEY, xpath.compile("/rtExperiment/stimulusData/stimEnvironment/screen"));
//			modelToNode.put(MEDIAOBJECTLIST_KEY, xpath.compile("/rtExperiment/stimulusData/mediaObjectList"));
//			modelToNode.put(TIMETABLE_KEY, xpath.compile("/rtExperiment/stimulusData/timeTable"));
//		} catch (XPathExpressionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		this.nodeMapper = new NodeMapper(modelToNode);
		
//		this.pluginModel.getMediaObjectList().addMediaObject(new MediaImage("blaID", "blaName", new Point(0, 0), new File("/media/DATA/studies/sem06/praktikum/mpi_svn/gr_bart/Misc/BARTPresentation/google_trends.png")));
//		this.pluginModel.getMediaObjectList().addMediaObject(new MediaText("blubID", "blubName" ,"Text...", 12, Color.WHITE, new Point(200, 400)));
//		this.pluginModel.getMediaObjectList().addMediaObject(new MediaVideo("blobID", "blobName", new Point (10, 10), new File("/media/MULE/stuff/futurama_s02e03.avi")));
//		
//		this.pluginModel.getTimetable().add(new StimEvent(0, 10000, this.pluginModel.getMediaObjectList().getMediaObject("blaID")));
//		this.pluginModel.getTimetable().add(new StimEvent(10000, 10000, this.pluginModel.getMediaObjectList().getMediaObject("blubID")));
//		this.pluginModel.getTimetable().add(new StimEvent(20000, 10000, this.pluginModel.getMediaObjectList().getMediaObject("blobID")));
		
	}

	@Override
	public void initAppController(Application appController) {
		if (this.appController == null) {
			this.appController = appController;
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getQualifiedName() {
		return StimulusPlugin.class.getName();
	}
	
	@Override
	public String getAuthor() {
		return AUTHOR;
	}
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public StimulusView getView() {
		return this.pluginView;
	}
	
	@Override
	public ReplacementManager getReplacementManager(final boolean updateManager) {
		if (updateManager) {
			DOMFormatter formatter = new DOMFormatter();
			
			this.nodeMapper.put(SCREEN_KEY, 
							 	formatter.xmlTreeFor(this.pluginModel.getScreen()));
			this.nodeMapper.put(MEDIAOBJECTLIST_KEY, 
							 	formatter.xmlTreeFor(this.pluginModel.getMediaObjectList(), appController.getCurrentXMLFile()));
			this.nodeMapper.put(TIMETABLE_KEY, 
							 	formatter.xmlTreeFor(this.pluginModel.getTimetable()));
		}
		
		return this.nodeMapper;
	}

	@Override
	public void update(Model model) {
		Document doc = model.getDocument();
		try {
			Node screenNode = (Node) this.nodeMapper.xpathFor(SCREEN_KEY).evaluate(doc, XPathConstants.NODE);
			Node mediaObjListNode = (Node) this.nodeMapper.xpathFor(MEDIAOBJECTLIST_KEY).evaluate(doc, XPathConstants.NODE);
			Node timetableNode = (Node) this.nodeMapper.xpathFor(TIMETABLE_KEY).evaluate(doc, XPathConstants.NODE);
			DOMFormatter formatter = new DOMFormatter();
			if (model.getValidationResult(screenNode, true).isValid()) {
				formatter.fill(this.pluginModel.getScreen(), screenNode);
			}
			if (model.getValidationResult(mediaObjListNode, true).isValid()) {
				MediaObjectList mediaObjList = this.pluginModel.getMediaObjectList();
				formatter.fill(mediaObjList, mediaObjListNode, appController.getCurrentXMLFile());
				if (model.getValidationResult(timetableNode, true).isValid()) {
					formatter.fill(this.pluginModel.getTimetable(), timetableNode, mediaObjList);
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public StimulusData getModel() {
		return this.pluginModel;
	}
	
	public Application getApplication() {
		return this.appController;
	}
	
	void startPresentation() {
		this.presentationThread = new Thread(new PresentationRunner());
		this.pluginView.startPresentation();
		this.presentationThread.start();
	}
	void pausePresentation() {
		if (!this.presentationThread.isInterrupted()) {

			this.presentationThread.interrupt();
			this.pluginView.pausePresentation();
			
			try {
				this.presentationThread.join();
				this.presentationThread = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	void continuePresentation() {
		if (this.presentationThread == null
			|| !this.presentationThread.isAlive()) {
			
			this.presentationThread = new Thread(new PresentationRunner());
			this.presentationThread.start();
			this.pluginView.continuePresentation();
		}
	}
	void stopPresentation() {
		if (this.presentationThread != null) {
			this.presentationThread.interrupt();
			try {
				this.presentationThread.join();
				this.presentationThread = null;
			} catch (InterruptedException e) {
				LOGGER.warn("InterruptedException while stopping the presentation.");
			}	
		}
		
		this.time = 0;
		this.pluginModel.getTimetable().resetTimetable();
		this.pluginView.stopPresentation();
	}
	long getTime() {
		return this.time;
	}
	void setTime(final long newTime) {
		this.time = newTime;
	}
	
	private class PresentationRunner implements Runnable {
		
		/** The time interval for one update tick in milliseconds. */
		private static final long UPDATE_INTERVAL = 5;
		
		@Override
		public void run() {
			boolean finished = false;
			long lastTicksTime;
			while (!Thread.interrupted()
				   && !finished) {
				tick();
				lastTicksTime = new Date().getTime();
				try {
					Thread.sleep(UPDATE_INTERVAL);
					time += (new Date().getTime() - lastTicksTime);
				} catch (InterruptedException e) {
					finished = true;
				}
			}
		}
		
		private void tick() {
			if (time <= pluginModel.getTimetable().getDuration()) {
				
				pluginView.setCurrentTime(time);
				handleStartingEvents();
				handleEndingEvents();
				// [mViewManager displayPresentationView];
			} else {
				time = pluginModel.getTimetable().getDuration();
				tick();
				Thread.currentThread().interrupt();
			}
		}
		
		private void handleStartingEvents() {
			Timetable timetable = pluginModel.getTimetable();
			StimEvent event = timetable.nextEventAt(time);
			while (event != null) {
				pluginView.present(event.mediaObject);
				StimEvent.endTimeSortedInsert(event, eventsToEnd);
				
				event = timetable.nextEventAt(time);
			}
		}
		private void handleEndingEvents() {
			boolean done = false;
			while (eventsToEnd.size() > 0
				   && !done) {
				
				StimEvent event = eventsToEnd.get(0);
				if (event.time + event.duration <= time) {
					pluginView.stopPresentationOf(event.mediaObject);
					eventsToEnd.remove(event);
				} else {
					done = true;
				}
			}
		}
	}

}
