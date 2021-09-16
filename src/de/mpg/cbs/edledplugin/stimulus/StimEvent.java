package de.mpg.cbs.edledplugin.stimulus;

import java.util.List;

public class StimEvent {
	
	public final long time;
	public final long duration;
	public final MediaObject mediaObject;
	
	public StimEvent(final long time, 
					 final long duration,
					 final MediaObject mediaObject) {
		if (mediaObject == null) {
			throw new IllegalArgumentException("Given media object is null!");
		}
		
		this.time = time;
		this.duration = duration;
		this.mediaObject = mediaObject;
	}
	
	public static void startTimeSortedInsert(final StimEvent event, 
											 final List<StimEvent> eventList) {
		
		int insertIndex = 0;
		while (insertIndex <= eventList.size()) {
			if (insertIndex == eventList.size()) {
				eventList.add(event);
				insertIndex = eventList.size();
			} else {
				if (eventList.get(insertIndex).time > event.time) {
					eventList.add(insertIndex, event);
					insertIndex = eventList.size();
				}
			}
			
			insertIndex++;
		}
	}
	
	public static void endTimeSortedInsert(final StimEvent event, 
			 							   final List<StimEvent> eventList) {
		int insertIndex = 0;
		while (insertIndex <= eventList.size()) {
			if (insertIndex == eventList.size()) {
				eventList.add(event);
				insertIndex = eventList.size();
			} else {
				StimEvent eventAtIndex = eventList.get(insertIndex);
				if ((event.time + event.duration)
					< (eventAtIndex.time + eventAtIndex.duration)) {
					eventList.add(insertIndex, event);
					insertIndex = eventList.size();
				}
			}
			
			insertIndex++;
		}
	}
	
	@Override
	public String toString() {
		return "StimEvent[time=" 
		     + this.time 
		     + ",duration=" 
		     + this.duration 
		     + ",mediaObjID=" 
		     + this.mediaObject.getID()
		     + "]";
	}

}
