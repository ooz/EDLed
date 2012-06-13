package de.mpg.cbs.edledplugin.stimulus;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class Timetable extends Observable implements Observer {
	
	private final MediaObjectList mediaObjList;
	
	private List<String> mediaObjectIDs;
	
	private Map<String, List<StimEvent>> eventsToHappen;
	private Map<String, List<StimEvent>> happenedEvents;
	
	private long duration;
	private long timeTolerance;
	
	Timetable(final MediaObjectList mediaObjList) {
		this.mediaObjList = mediaObjList;
		this.mediaObjList.addObserver(this);
		
		this.mediaObjectIDs = new LinkedList<String>();
		
		this.eventsToHappen = new LinkedHashMap<String, List<StimEvent>>();
		this.happenedEvents = new LinkedHashMap<String, List<StimEvent>>();
		this.duration = 0;
		this.timeTolerance = 0;
	}
	
	@Override
	public void finalize() {
		this.mediaObjList.deleteObserver(this);
	}
	
	synchronized StimEvent nextEventAt(final long time) {
		for (String mediaObjID : this.eventsToHappen.keySet()) {
			List<StimEvent> eventsForMediaObj = this.eventsToHappen.get(mediaObjID);
			if (eventsForMediaObj.size() > 0) {
				StimEvent event = eventsForMediaObj.get(0);
				if (event.time <= time) {
//					if (event.time + event.duration > time) {
						this.happenedEvents.get(mediaObjID).add(event);
						eventsForMediaObj.remove(event);
						return event;
//					} else {
//						eventsForMediaObj.remove(event);
//					}
				}
			}
		}
		
		return null;
	}
	
	synchronized List<String> getMediaObjectIDs() {
		return new LinkedList<String>(this.mediaObjectIDs);
	}
	synchronized long getDuration() {
		return this.duration;
	}
	synchronized void setDuration(final long newDuration) {
		this.duration = newDuration;
	}
	synchronized long getTolerance() {
		return this.timeTolerance;
	}
	synchronized void setTolerance(final long newTimeTolerance) {
		this.timeTolerance = newTimeTolerance;
	}
//	public int getNumberOfMediaObjects() {
//		return this.mediaObjects.size();
//	}
	
	synchronized List<StimEvent> getHappenedEventsFor(final String mediaObjectID) {
		return new LinkedList<StimEvent>(this.happenedEvents.get(mediaObjectID));
	}
	synchronized List<StimEvent> getEventsToHappenFor(final String mediaObjectID) {
		return new LinkedList<StimEvent>(this.eventsToHappen.get(mediaObjectID));
	}
	
	void add(final StimEvent event) {
		_add(event, true);
	}
	synchronized private void _add(final StimEvent event, final boolean notifyObservers) {
		if (event == null) {
			return;
		}
		
		if (event.duration > 0) {
			
			if (event.time + event.duration > this.duration) {
				this.duration = event.time + event.duration;
			}
			
			String mediaObjID = event.mediaObject.getID();
			if (!this.eventsToHappen.containsKey(mediaObjID)
				/* || !this.happenedEvents.containsKey(mediaObjID) */) {
				this.eventsToHappen.put(mediaObjID, new LinkedList<StimEvent>());
				this.happenedEvents.put(mediaObjID, new LinkedList<StimEvent>());
			}
			if(!this.mediaObjectIDs.contains(mediaObjID)) {
				this.mediaObjectIDs.add(mediaObjID);
			}
			
			List<StimEvent> eventsForMediaObj = this.eventsToHappen.get(mediaObjID);
			StimEvent.startTimeSortedInsert(event, eventsForMediaObj);
			removeMaskedEvents(event, eventsForMediaObj);
			
			if (notifyObservers) {
				setChanged();
				notifyObservers();
			}
		}
	}
	void add(final List<StimEvent> events) {
		for (StimEvent toAdd : events) {
			_add(toAdd, false);
		}
		
		synchronized (this) {
			setChanged();
			notifyObservers();
		}
	}
	synchronized void remove(final StimEvent toRemove) {
		if (toRemove == null) {
			return;
		}
		
		String mediaObjID = toRemove.mediaObject.getID();
		List<StimEvent> eventsToHappenForMediaObj = this.eventsToHappen.get(mediaObjID);
		List<StimEvent> happenedEventsForMediaObj = this.happenedEvents.get(mediaObjID);
		
		if (eventsToHappenForMediaObj.contains(toRemove)) {
			eventsToHappenForMediaObj.remove(toRemove);
		} else if (happenedEventsForMediaObj.contains(toRemove)) {
			happenedEventsForMediaObj.remove(toRemove);
		}
		
		if (eventsToHappenForMediaObj.size() == 0
			&& happenedEventsForMediaObj.size() == 0) {
			this.eventsToHappen.remove(mediaObjID);
			this.happenedEvents.remove(mediaObjID);
			this.mediaObjectIDs.remove(mediaObjID);
		}
		
		setChanged();
		notifyObservers();
	}
	synchronized void replace(final StimEvent toReplace, 
	   						  final StimEvent replacement) {
		if (replacement.duration <= 0) {
			remove(toReplace);
			return;
		}
		
		if (toReplace.mediaObject == replacement.mediaObject) {
			String mediaObjID = toReplace.mediaObject.getID();
			List<StimEvent> eventsToHappenForMediaObj = this.eventsToHappen.get(mediaObjID);
			List<StimEvent> happenedEventsForMediaObj = this.happenedEvents.get(mediaObjID);
			
			if (eventsToHappenForMediaObj.contains(toReplace)) {
				eventsToHappenForMediaObj.remove(toReplace);
				StimEvent.startTimeSortedInsert(replacement, eventsToHappenForMediaObj);
				removeMaskedEvents(replacement, eventsToHappenForMediaObj);
			} else if (happenedEventsForMediaObj.contains(toReplace)) {
				happenedEventsForMediaObj.remove(toReplace);
				StimEvent.startTimeSortedInsert(replacement, happenedEventsForMediaObj);
				removeMaskedEvents(replacement, happenedEventsForMediaObj);
			}
			
			setChanged();
			notifyObservers();
		} else {
			remove(toReplace);
			add(replacement);
		}
	}
	
	synchronized void resetTimetable() {
		for (String mediaObjID : this.happenedEvents.keySet()) {
			
			List<StimEvent> happenedEventsForMediaObj = this.happenedEvents.get(mediaObjID);
			for (StimEvent eventToAdd : happenedEventsForMediaObj) {
				StimEvent.startTimeSortedInsert(eventToAdd, this.eventsToHappen.get(mediaObjID));
			}
			
			happenedEventsForMediaObj.clear();
		}
		
		setChanged();
		notifyObservers();
	}
	
	synchronized void clear() {
		this.eventsToHappen.clear();
		this.happenedEvents.clear();
		this.mediaObjectIDs.clear();
		
		this.duration = 0;
		this.timeTolerance = 0;
		
		setChanged();
		notifyObservers();
	}
	
	private void removeMaskedEvents(StimEvent event, 
									final List<StimEvent> events) {
		
		List<StimEvent> maskedEvents = new LinkedList<StimEvent>();
		long eventEndTime = event.time + event.duration;
		int eventIndex = events.indexOf(event);
		
		// Check whether event is masking/masked by its predecessor.
		if (eventIndex > 0) {
			StimEvent previousEvent = events.get(eventIndex - 1);
			long previousEndTime = previousEvent.time + previousEvent.duration;
			if (previousEndTime >= event.time) {
				if (previousEndTime >= eventEndTime) {
					maskedEvents.add(event);
				} else {
					StimEvent replacement = new StimEvent(previousEvent.time, 
																  (eventEndTime - previousEvent.time), 
																  event.mediaObject);
					events.set(eventIndex, replacement);
					event = replacement;
					maskedEvents.add(previousEvent);
				}
			}
		}
		
		int nextEventIndex = eventIndex + 1;
		
		// Check whether event masks events following it.
		while (nextEventIndex < events.size()) {
			StimEvent nextEvent = events.get(nextEventIndex);
			if (nextEvent.time <= eventEndTime) {
				long nextEventEndTime = nextEvent.time + nextEvent.duration;
				if (nextEventEndTime > eventEndTime) {
					StimEvent replacement = new StimEvent(event.time, 
																  (nextEventEndTime - event.time), 
																  event.mediaObject);
					events.set(eventIndex, replacement);
					nextEventIndex = events.size(); // Negate loop condition.
				}
				maskedEvents.add(nextEvent);
				nextEventIndex++;
			} else {
				nextEventIndex = events.size(); // Negate loop condition.
			}
		}
		
		events.removeAll(maskedEvents);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.mediaObjList) {
			synchronized (this) {
				List<String> validIDs = this.mediaObjList.getMediaObjectIDs();
				List<String> _IDsToRemove = new LinkedList<String>();
				
				for (String mediaObjID : this.mediaObjectIDs) {
					if (!validIDs.contains(mediaObjID)) {
						_IDsToRemove.add(mediaObjID);
					}
				}
				
				for (String idToRemove : _IDsToRemove) {
					this.mediaObjectIDs.remove(idToRemove);
					this.happenedEvents.remove(idToRemove);
					this.eventsToHappen.remove(idToRemove);
				}
				
				if (_IDsToRemove.size() > 0) {
					setChanged();
					notifyObservers();
				}
			}
		}
	}
}
