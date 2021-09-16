package de.mpg.cbs.edledplugin.stimulus;


/**
 * Represents the model of the plugin.
 * 
 * @author Oliver Z.
 */
public class StimulusData {
	
	private Screen screen;
	private MediaObjectList mediaObjectList;
	private final Timetable timetable;
	
	StimulusData() {
		
		this.screen = new Screen();
		this.mediaObjectList = new MediaObjectList();
		this.timetable = new Timetable(this.mediaObjectList);
		
	}
	
	public MediaObjectList getMediaObjectList() {
		return this.mediaObjectList;
	}
	
	public Timetable getTimetable() {
		return this.timetable;
	}
	
	public Screen getScreen() {
		return this.screen;
	}

}
