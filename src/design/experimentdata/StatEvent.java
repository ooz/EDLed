package design.experimentdata;

/** Binding to /experimentData/paradigm/[swDesignStruct|gwDesignStruct]/
 *              timeBasedRegressor/tbrDesign/statEvent */
public class StatEvent {
	public double time; // unsigned long
	public long duration; // unsigned int
	public double parametricScaleFactor;
	
	public StatEvent(double time, 
					 long duration, 
					 double parametricScaleFactor) {
		this.time = time;
		this.duration = duration;
		this.parametricScaleFactor = parametricScaleFactor;
	}
}
