package design.experimentdata;

import java.util.List;

/** Binding for /experimentData/paradigm/[swDesignStruct|gwDesignStruct]/
 *   			 timeBasedRegressor/tbrDesign */
public class TbrDesign {
	public double length; // unsigned long
	public long repetitions; // unsigned int
	public List<StatEvent> events;
	
	public TbrDesign(double length, long repetitions, List<StatEvent> events) {
		this.length = length;
		this.repetitions = repetitions;
		if (events.size() < 1) {
			throw new IllegalArgumentException("Tried to construct a TbrDesign with 0 events.");
		}
		this.events = events;
	}
}
