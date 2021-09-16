package de.mpg.cbs.edledplugin.design.bart;

import flanagan.complex.Complex;

/* From NEDesignKernel.h */
public abstract class DesignKernel {
	/* Helper classes */
	public static enum DesignKernelTimeUnit {
		KERNEL_TIME_MS,
		KERNEL_TIME_S;
	}
	
	public static class GeneralGammaParams {
		public double maxLengthHrfInMs; // unsigned int
		public double peak1;
		public double scale1;
		public double peak2;
		public double scale2;
	}
	
	/* Attributes */
	private String id;
	public Complex[] kernelDeriv0 = {};
	public Complex[] kernelDeriv1 = {};
	public Complex[] kernelDeriv2 = {};
	
	/* Constructors */
	public DesignKernel() {
		this.id = "";
	}
	public DesignKernel(final String kernelID) {
		this.id = kernelID;
	}
	
	/* Methods */
	public abstract float[][] plotGammaWithDerivs(final int derivs); // unsigned int
	
	/**
	 * Getter for the Kernel ID
	 * 
	 * @return Kernel ID. Empty string if no ID is set.
	 */
	public String getID() {
		return this.id;
	}
}
