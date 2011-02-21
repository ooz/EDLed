package design.experimentdata;

import java.util.LinkedList;
import java.util.List;

/** Binding for /experimentData/paradigm/[swDesignStruct|gwDesignStruct] */
public class DesignStruct {
	private final boolean isSlidingWindow;
	public List<TimeBasedRegressor> regressors;
	
	/**
	 * 
	 * @param isSlidingWindow Flag indicating whether the DesignStruct ist a
	 * 						  swDesignStruct (true) oder gwDesignStruct (false).
	 */
	public DesignStruct(boolean isSlidingWindow,
						List<TimeBasedRegressor> regressors) {
		this.isSlidingWindow = isSlidingWindow;
		if (regressors == null) {
			this.regressors = new LinkedList<TimeBasedRegressor>();
		} else {
			this.regressors = regressors;
		}
	}
	
	public boolean isSlidingWindowDesign() {
		return this.isSlidingWindow;
	}
	public boolean isGrowingWindowDesign() {
		return !this.isSlidingWindow;
	}

}
