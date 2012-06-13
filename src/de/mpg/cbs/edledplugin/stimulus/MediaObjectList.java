package de.mpg.cbs.edledplugin.stimulus;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class MediaObjectList extends Observable {
	
	private static final String DEFAULT_MEDIAOBJECT_ID = "mo";
	
	private final Map<String, MediaObject> mediaObjects;
	
	MediaObjectList() {
		this.mediaObjects = new LinkedHashMap<String, MediaObject>();
	}
	
	synchronized List<String> getMediaObjectIDs() {
		return new LinkedList<String>(this.mediaObjects.keySet());
	}
	synchronized MediaObject getMediaObject(final String id) {
		return this.mediaObjects.get(id);
	}
	synchronized List<MediaObject> getMediaObjects() {
		return new LinkedList<MediaObject>(this.mediaObjects.values());
	}
	
	void add(final MediaObject toAdd) {
		add(toAdd, true);
	}
	
	void add(final List<MediaObject> objs) {
		for (MediaObject toAdd : objs) {
			add(toAdd, false);
		}
		
		synchronized (this) {
			setChanged();
			notifyObservers();
		}
	}
	
	synchronized void add(final MediaObject toAdd, 
						  final boolean notifyObservers) {
		
		String id = toAdd.getID();
		String name = toAdd.getName();
		boolean recreateMediaObj = false;
		
		if (id == null || id.equals("") || this.mediaObjects.keySet().contains(id)) {
			int counter = 0;
			id = String.format(DEFAULT_MEDIAOBJECT_ID + "%d", counter);
			while (this.mediaObjects.keySet().contains(id)) {
				id = String.format(DEFAULT_MEDIAOBJECT_ID + "%d", counter);
				counter++;
			}
			recreateMediaObj = true;
		}
		
		if (name == null || name.equals("")) {
			name = new String(id);
			recreateMediaObj = true;
		}
		
		if (recreateMediaObj) {
			switch (toAdd.getKind()) {
			case TEXT:
				this.mediaObjects.put(id, new MediaText(id, name, (MediaText) toAdd));
				break;
			case IMAGE:
				this.mediaObjects.put(id, new MediaImage(id, name, (MediaImage) toAdd));
				break;
			case AUDIO:
				this.mediaObjects.put(id, new MediaAudio(id, name, (MediaAudio) toAdd));
				break;
			case VIDEO:
				this.mediaObjects.put(id, new MediaVideo(id, name, (MediaVideo) toAdd));
				break;
			default:
				throw new RuntimeException("Tried to add media object of kind other than text/image/audio/video!");
			}
		} else {
			this.mediaObjects.put(toAdd.getID(), toAdd);
		}
		
		if (notifyObservers) {
			setChanged();
			notifyObservers();
		}
	}
	synchronized void removeMediaObject(final MediaObject toRemove) {
		this.mediaObjects.remove(toRemove.getID());
		setChanged();
		notifyObservers();
	}
	
	synchronized void clear() {
		this.mediaObjects.clear();
		setChanged();
		notifyObservers();
	}

}
