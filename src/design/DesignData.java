package design;

import java.util.Observable;

import design.bart.DesignElement;

/**
 * Root model class for the design plugin.
 * 
 * @author Oliver Zscheyge
 */
public class DesignData extends Observable {
	
	/** The current DesignElement containing most/all of the design data. */
	private DesignElement design;
	
	/** Constructor. */
	public DesignData() {
		this.design = null;
	}
	
	/** Setter. */
	public void setDesign(final DesignElement design) {
		this.design = design;
	}
	/** Getter. */
	public DesignElement getDesign() {
		return this.design;
	}
}
