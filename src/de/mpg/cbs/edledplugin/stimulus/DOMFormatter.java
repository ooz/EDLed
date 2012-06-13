package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.util.Configuration;
import de.mpg.cbs.edled.util.FileUtility;
import de.mpg.cbs.edled.xml.XMLUtility;




public class DOMFormatter {
	
	public Node xmlTreeFor(final Screen screen) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (doc != null) {
			Element screenElem = doc.createElement("screen");
			
			Element widthElem = doc.createElement("screenResolutionX");
			widthElem.setTextContent(new Integer(screen.getWidth()).toString());
			Element heightElem = doc.createElement("screenResolutionY");
			heightElem.setTextContent(new Integer(screen.getHeight()).toString());
			
			screenElem.appendChild(widthElem);
			screenElem.appendChild(heightElem);
			
			return screenElem;
		}
		
		return null;
	}
	
	public Node xmlTreeFor(final MediaObjectList mediaObjectList, 
						   final File relatePathsTo) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (doc != null) {
			Element mediaObjListElem = doc.createElement("mediaObjectList");
			
			for (MediaObject mediaObj : mediaObjectList.getMediaObjects()) {
				Node mediaObjElem = xmlTreeFor(doc, mediaObj, relatePathsTo);
				if (mediaObjElem != null) {
					mediaObjListElem.appendChild(mediaObjElem);
				}
			}
			
			return mediaObjListElem;
		}
		
		return null;
	}
	private Node xmlTreeFor(final Document doc, 
							final MediaObject mediaObj,
							final File relatePathsTo) {
		
		Element mediaObjElem = doc.createElement("mediaObject");
		mediaObjElem.setAttribute("moID", mediaObj.getID());
		mediaObjElem.setAttribute("name", mediaObj.getName());
		
		switch (mediaObj.getKind()) {
		case TEXT:
			MediaText text = (MediaText) mediaObj;
			mediaObjElem.setAttribute("type", "TEXT");
			Element contentTextElem = doc.createElement("contentText");
			// Text.
			Element textElem = doc.createElement("text");
			textElem.setTextContent(text.getText());
			contentTextElem.appendChild(textElem);
			// Color.
			Element colorElem = doc.createElement("tColor");
			float[] rgbValues = text.getColor().getRGBColorComponents(null);
			Element redElem = doc.createElement("tcRed");
			redElem.setTextContent(new Float(rgbValues[0]).toString());
			colorElem.appendChild(redElem);
			Element greenElem = doc.createElement("tcGreen");
			greenElem.setTextContent(new Float(rgbValues[1]).toString());
			colorElem.appendChild(greenElem);
			Element blueElem = doc.createElement("tcBlue");
			blueElem.setTextContent(new Float(rgbValues[2]).toString());
			colorElem.appendChild(blueElem);
			contentTextElem.appendChild(colorElem);
			// Size.
			Element sizeElem = doc.createElement("tSize");
			sizeElem.setTextContent(new Integer(text.getTextSize()).toString());
			contentTextElem.appendChild(sizeElem);
			// x-coordinate.
			Element xTextElem = doc.createElement("posX");
			xTextElem.setTextContent(new Integer(text.getVisualPosition().x).toString());
			contentTextElem.appendChild(xTextElem);
			// y-coordinate.
			Element yTextElem = doc.createElement("posY");
			yTextElem.setTextContent(new Integer(text.getVisualPosition().y).toString());
			contentTextElem.appendChild(yTextElem);
			
			mediaObjElem.appendChild(contentTextElem);
			
			return mediaObjElem;
			
		case IMAGE:
			MediaImage image = (MediaImage) mediaObj;
			mediaObjElem.setAttribute("type", "IMAGE");
			Element contentImageElem = doc.createElement("contentImage");
			// Image file.
			Element imageFileElem = doc.createElement("imageFile");
			String imageFilePath = image.getImageFile().getPath();
			// Relativize file path.
			if (relatePathsTo != null) {
				String basePath = FilenameUtils.getFullPath(relatePathsTo.getPath());
				imageFilePath = FileUtility.relativize(imageFilePath, basePath, Configuration.FILE_SEPARATOR);
			}
			imageFileElem.setTextContent(imageFilePath);
			contentImageElem.appendChild(imageFileElem);
			// x-coordinate.
			Element xImageElem = doc.createElement("posX");
			xImageElem.setTextContent(new Integer(image.getVisualPosition().x).toString());
			contentImageElem.appendChild(xImageElem);
			// y-coordinate.
			Element yImageElem = doc.createElement("posY");
			yImageElem.setTextContent(new Integer(image.getVisualPosition().y).toString());
			contentImageElem.appendChild(yImageElem);
			
			mediaObjElem.appendChild(contentImageElem);
			
			return mediaObjElem;
			
		case AUDIO:
			MediaAudio audio = (MediaAudio) mediaObj;
			mediaObjElem.setAttribute("type", "SOUND");
			Element contentSoundElem = doc.createElement("contentSound");
			// Audio file.
			Element soundFileElem = doc.createElement("soundFile");
			String soundFilePath = audio.getAudioFile().getPath();
			// Relativize file path.
			if (relatePathsTo != null) {
				String basePath = FilenameUtils.getFullPath(relatePathsTo.getPath());
				soundFilePath = FileUtility.relativize(soundFilePath, basePath, Configuration.FILE_SEPARATOR);
			}
			soundFileElem.setTextContent(soundFilePath);
			contentSoundElem.appendChild(soundFileElem);
			
			mediaObjElem.appendChild(contentSoundElem);
			
			return mediaObjElem;
			
		case VIDEO:
			MediaVideo video = (MediaVideo) mediaObj;
			mediaObjElem.setAttribute("type", "VIDEO");
			Element contentVideoElem = doc.createElement("contentVideo");
			// Video file.
			Element videoFileElem = doc.createElement("videoFile");
			String videoFilePath = video.getVideoFile().getPath();
			// Relativize file path.
			if (relatePathsTo != null) {
				String basePath = FilenameUtils.getFullPath(relatePathsTo.getPath());
				videoFilePath = FileUtility.relativize(videoFilePath, basePath, Configuration.FILE_SEPARATOR);
			}
			videoFileElem.setTextContent(videoFilePath);
			contentVideoElem.appendChild(videoFileElem);
			// x-coordinate.
			Element xVideoElem = doc.createElement("posX");
			xVideoElem.setTextContent(new Integer(video.getVisualPosition().x).toString());
			contentVideoElem.appendChild(xVideoElem);
			// y-coordinate.
			Element yVideoElem = doc.createElement("posY");
			yVideoElem.setTextContent(new Integer(video.getVisualPosition().y).toString());
			contentVideoElem.appendChild(yVideoElem);
			
			mediaObjElem.appendChild(contentVideoElem);
			
			return mediaObjElem;
			
		default:
			return null;
		}
	}
	
	public Node xmlTreeFor(final Timetable timetable) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (doc != null) {
			Element timetableElem = doc.createElement("timeTable");
			timetableElem.setAttribute("timeTolerance", new Long(timetable.getTolerance()).toString());
			Element freeStimDesginElem = doc.createElement("freeStimulusDesign");
			freeStimDesginElem.setAttribute("overallPresLength", new Long(timetable.getDuration()).toString());
			
			timetable.resetTimetable();
			
			List<StimEvent> allEvents = new LinkedList<StimEvent>();
			for (String mediaObjID : timetable.getMediaObjectIDs()) {
				for (StimEvent event : timetable.getEventsToHappenFor(mediaObjID)) {
					StimEvent.startTimeSortedInsert(event, allEvents);
				}
			}
			
			for (StimEvent event : allEvents) {
				Element eventElem = doc.createElement("stimEvent");
				eventElem.setAttribute("duration", new Long(event.duration).toString());
				eventElem.setAttribute("time", new Long(event.time).toString());
				
				Element mediaObjIDElem = doc.createElement("mObjectID");
				mediaObjIDElem.setTextContent(event.mediaObject.getID());
				eventElem.appendChild(mediaObjIDElem);
				
				freeStimDesginElem.appendChild(eventElem);
			}
			
			timetableElem.appendChild(freeStimDesginElem);
			
			return timetableElem;
		}
		
		return null;
	}
	
	public boolean fill(final Screen screen, 
						final Node screenNode) {
		
		if (screenNode == null
			|| screenNode.getNodeType() != Node.ELEMENT_NODE
			|| !screenNode.getNodeName().equals("screen")) {
			
			return false;
		}
		
		Element screenElem = (Element) screenNode;
		Element screenResX = (Element) screenElem.getElementsByTagName("screenResolutionX").item(0);
		Element screenResY = (Element) screenElem.getElementsByTagName("screenResolutionY").item(0);
		
		if (screenResX != null
			&& screenResY != null) {
			try {
				int width = Integer.parseInt(XMLUtility.getNodeValue(screenResX).trim());
				int height = Integer.parseInt(XMLUtility.getNodeValue(screenResY).trim());
				screen.setWidth(width);
				screen.setHeight(height);
				return true;
				
			} catch (NumberFormatException e) {
			}
		}
		
		return false;
	}
	
	/**
	 * Fills the MediaObjectList model object in the plugin with
	 * the data from the DOM tree (EDL configuration).
	 * 
	 * @param mediaObjList	   The MediaObjectList to fill.
	 * @param mediaObjListNode The DOM node representing the 
	 * 						   media object list in the EDL configuration.
	 * @param relatePathsTo    Path of the current EDL file. The media objects
	 * 						   referencing files (image, audio, video) will resolve 
	 * 						   relative paths to the given relatePathsTo.
	 * @return				   Flag indicating the success of the operation. 
	 * 						   True if no errors occured.
	 */
	public boolean fill(final MediaObjectList mediaObjList, 
						final Node mediaObjListNode,
						final File relatePathsTo) {
		
		if (mediaObjListNode == null
			|| mediaObjListNode.getNodeType() != Node.ELEMENT_NODE
			|| !mediaObjListNode.getNodeName().equals("mediaObjectList")) {
			return false;
		}
		
		mediaObjList.clear();
		
		Element mediaObjListElem = (Element) mediaObjListNode;
		NodeList mediaObjects = mediaObjListElem.getElementsByTagName("mediaObject");
		for (int i = 0; i < mediaObjects.getLength(); i++) {
			MediaObject mediaObj = null;
			try {
				mediaObj = buildMediaObjFrom(mediaObjects.item(i), relatePathsTo);
			} catch (final NullPointerException e) {
				mediaObj = null;
			}
			if (mediaObj != null) {
				mediaObjList.add(mediaObj, false);
			}
		}
		
		// Update observers
		mediaObjList.add(new LinkedList<MediaObject>());
		
		return true;
	}
	/**
	 * Helper method for DOMFormatter.fill(MediaObjectList, Node, File).
	 * Creates a single media object from a DOM node representing the media object
	 * in the EDL configuration. 
	 * 
	 * @param mediaObjNode  Node from the EDL configuration representing the 
	 * 						media object that should be created.
	 * @param relatePathsTo Path of the current EDL file. Referenced media files
	 * 						(image, audio, video) with relative paths will be resolved
	 * 						based on relatePathsTo.
	 * @return				A MediaObject representing mediaObjNode in the plugin's model.
	 */
	private MediaObject buildMediaObjFrom(final Node mediaObjNode,
										  final File relatePathsTo) {
		
		if (mediaObjNode.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
		
		Element mediaObjElem = (Element) mediaObjNode;
		String id = mediaObjElem.getAttribute("moID").trim();
		String name = mediaObjElem.getAttribute("name");
		String type = mediaObjElem.getAttribute("type");
		
		if (type.equals("TEXT")) {
			Element contentText = (Element) mediaObjElem.getElementsByTagName("contentText").item(0);
			String text = XMLUtility.getNodeValue(contentText.getElementsByTagName("text").item(0));
			Element colorElem = (Element) contentText.getElementsByTagName("tColor").item(0);
			Color textColor;
			try {
				float red = Float.parseFloat(XMLUtility.getNodeValue(colorElem.getElementsByTagName("tcRed").item(0)).trim());
				float green = Float.parseFloat(XMLUtility.getNodeValue(colorElem.getElementsByTagName("tcGreen").item(0)).trim());
				float blue = Float.parseFloat(XMLUtility.getNodeValue(colorElem.getElementsByTagName("tcBlue").item(0)).trim());
				textColor = new Color(red, green, blue);
			} catch (NumberFormatException e) {
				textColor = new Color(1.0f, 1.0f, 1.0f);
			}
			int size;
			try {
				size = Integer.parseInt(XMLUtility.getNodeValue(contentText.getElementsByTagName("tSize").item(0)).trim());
			} catch (NumberFormatException e) {
				return null;
			}
			int posX;
			int posY;
			try {
				posX = Integer.parseInt(XMLUtility.getNodeValue(contentText.getElementsByTagName("posX").item(0)).trim());
				posY = Integer.parseInt(XMLUtility.getNodeValue(contentText.getElementsByTagName("posY").item(0)).trim());
			} catch (NumberFormatException e) {
				posX = 0;
				posY = 0;
			}
			
			return new MediaText(id, name, text, size, textColor, new Point(posX, posY));
			
		} else if (type.equals("IMAGE")) {
			Element contentImage = (Element) mediaObjElem.getElementsByTagName("contentImage").item(0);
			String imageFilePath = XMLUtility.getNodeValue(contentImage.getElementsByTagName("imageFile").item(0)).trim();
			if (!(new File(imageFilePath)).isAbsolute()
				&& relatePathsTo != null) {
				imageFilePath = FilenameUtils.getFullPath(relatePathsTo.getPath()) + imageFilePath;
			}
			
			int posX;
			int posY;
			try {
				posX = Integer.parseInt(XMLUtility.getNodeValue(contentImage.getElementsByTagName("posX").item(0)).trim());
				posY = Integer.parseInt(XMLUtility.getNodeValue(contentImage.getElementsByTagName("posY").item(0)).trim());
			} catch (NumberFormatException e) {
				posX = 0;
				posY = 0;
			}
			
			return new MediaImage(id, name, new Point(posX, posY), new File(imageFilePath));
			
		} else if (type.equals("SOUND")) {
			Element contentSound = (Element) mediaObjElem.getElementsByTagName("contentSound").item(0);
			String soundFilePath = XMLUtility.getNodeValue(contentSound.getElementsByTagName("soundFile").item(0)).trim();
			if (!(new File(soundFilePath)).isAbsolute()
				&& relatePathsTo != null) {
				soundFilePath = FilenameUtils.getFullPath(relatePathsTo.getPath()) + soundFilePath;
			}
			
			return new MediaAudio(id, name, new File(soundFilePath));
			
		} else if (type.equals("VIDEO")) {
			Element contentVideo = (Element) mediaObjElem.getElementsByTagName("contentVideo").item(0);
			String videoFilePath = XMLUtility.getNodeValue(contentVideo.getElementsByTagName("videoFile").item(0)).trim();
			if (!(new File(videoFilePath)).isAbsolute()
				&& relatePathsTo != null) {
				videoFilePath = FilenameUtils.getFullPath(relatePathsTo.getPath()) + videoFilePath;
			}
			
			int posX;
			int posY;
			try {
				posX = Integer.parseInt(XMLUtility.getNodeValue(contentVideo.getElementsByTagName("posX").item(0)).trim());
				posY = Integer.parseInt(XMLUtility.getNodeValue(contentVideo.getElementsByTagName("posY").item(0)).trim());
			} catch (NumberFormatException e) {
				posX = 0;
				posY = 0;
			}
			
			return new MediaVideo(id, name, new Point(posX, posY), new File(videoFilePath));
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param timetable
	 * @param timetableNode
	 * @param mediaObjList
	 * @return
	 */
	public boolean fill(final Timetable timetable,
						final Node timetableNode,
						final MediaObjectList mediaObjList) {
		
		if (timetableNode == null
			|| timetableNode.getNodeType() != Node.ELEMENT_NODE
			|| !timetableNode.getNodeName().equals("timeTable")) {
			return false;
		}
		
		timetable.clear();
		
		Element timetableElem = (Element) timetableNode;
		
		long timeTolerance;
		try {
			timeTolerance = Long.parseLong(timetableElem.getAttribute("timeTolerance").trim());
		} catch (NumberFormatException e) {
			timeTolerance = 0;
		}
		timetable.setTolerance(timeTolerance);
		
		int repeats;
		Element designElem  = (Element) timetableElem.getElementsByTagName("blockStimulusDesign").item(0);
		if (designElem != null) {
			try {
				repeats = Integer.parseInt(designElem.getAttribute("repeats").trim());
			} catch (NumberFormatException e) {
				repeats = 1;
			}
		} else {
			designElem = (Element) timetableElem.getElementsByTagName("freeStimulusDesign").item(0);
			repeats = 1;
		}
		
		long duration;
		try {
			duration = Long.parseLong(designElem.getAttribute("overallPresLength").trim());
		} catch (NumberFormatException e) {
			duration = 0;
		}
		timetable.setDuration(duration);
		
		int eventCounter = 0;
		int repeatCounter = 1;
		long timeOffset = 0;
		long blockDuration = 0;
		NodeList events = designElem.getElementsByTagName("stimEvent");
		Node event = events.item(eventCounter);
		List<StimEvent> eventsToAdd = new LinkedList<StimEvent>();
		while (repeatCounter <= repeats
			   && event != null) {
			
			StimEvent parsedEvent = buildStimEventFrom(event, mediaObjList);
			
	        if (repeatCounter == 1
		        && (parsedEvent.time + parsedEvent.duration) > blockDuration) {
	        	blockDuration = parsedEvent.time + parsedEvent.duration;
	        }
	        
	        if (parsedEvent.mediaObject != null) {
	        	eventsToAdd.add(new StimEvent(((repeatCounter - 1) * timeOffset + parsedEvent.time), 
	        								  parsedEvent.duration, 
	        								  parsedEvent.mediaObject));
	        }
	        
	        eventCounter++;
	        event = events.item(eventCounter);
	        if (event == null) {
	        	if (repeatCounter <= repeats) {
	        		if (repeatCounter == 1) {
	        			timeOffset = blockDuration;
	        		}
	        		repeatCounter++;
	        		eventCounter = 0;
	        		event = events.item(eventCounter);
	        	} else {
	        		Element outroElem = (Element) timetableElem.getElementsByTagName("outro").item(0);
	        		if (outroElem != null) {
	        			events = outroElem.getElementsByTagName("stimEvent");
	        			eventCounter = 0;
	        			event = events.item(eventCounter);
	        		}
	        	}
	        }
		}
		
		timetable.add(eventsToAdd);
		
		return true;
	}
	
	private StimEvent buildStimEventFrom(final Node eventNode, 
										 final MediaObjectList mediaObjects) {
		Element eventElem = (Element) eventNode;
		long eventTime;
		long eventDuration;
		try {
			eventTime = Long.parseLong(eventElem.getAttribute("time").trim());
			eventDuration = Long.parseLong(eventElem.getAttribute("duration").trim());
		} catch (NumberFormatException e) {
			eventTime = 0;
			eventDuration = 0;
		}
		
		String mediaObjID = XMLUtility.getNodeValue(eventElem.getElementsByTagName("mObjectID").item(0)).trim();
        MediaObject mediaObj = mediaObjects.getMediaObject(mediaObjID);
        
        return new StimEvent(eventTime, eventDuration, mediaObj);
	}
	
}
