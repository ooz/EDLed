package design.bart;

import flanagan.complex.Complex;

/* From NEDesignKernel.h */
public abstract class DesignKernel {
	/* Helper classes */
	public static enum DesignKernelTimeUnit {
		KERNEL_TIME_MS,
		KERNEL_TIME_S;
	}
	
	public static class GeneralGammaParams {
		public int maxLengthHrfInMs; // unsigned int
		public double peak1;
		public double scale1;
		public double peak2;
		public double scale2;
	}
	
	/* Attributes */
	public Complex[] kernelDeriv0 = {};
	public Complex[] kernelDeriv1 = {};
	public Complex[] kernelDeriv2 = {};
	
	/* Methods */
	public abstract float[][] plotGammaWithDerivs(final int derivs); // unsigned int
}
