package design;

import design.bart.DesignElement;

public class DesignData {
	
	private DesignElement design;
	
	public DesignData() {
		this.design = null;
	}
	
	public void setDesign(final DesignElement design) {
		this.design = design;
	}
	public DesignElement getDesign() {
		return this.design;
	}
}
