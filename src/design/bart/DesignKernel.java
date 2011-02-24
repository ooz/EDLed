package design.bart;

/* From NEDesignKernel.h */
public interface DesignKernel {
	public static enum DesignKernelTimeUnit {
		KERNEL_TIME_MS,
		KERNEL_TIME_S;
	}
	
	public static class GloverParams {
		public int maxLengthHrfInMs; // must be positive
		public double peak1; //fka a1 - 6
		public double scale1; //fka b1 - 0.9
		public double peak2; // fka a2 - 12
		public double scale2; //fka b2 - 0.9
		public double offset;	// fka hard coded - for gamma 0.0, gauss 5.0
		public double relationP1P2; // fka cc cx - for block 0.1, event 0.35
		public double heightScale; //fka hard coded voodoo scale - for block 120, event 20
		public DesignKernelTimeUnit timeUnit;
		/** Constructor */
		public GloverParams(int maxLengthHrfInMs, double peak1, double scale1,
				double peak2, double scale2, double offset,
				double relationP1P2, double heightScale,
				DesignKernelTimeUnit timeUnit) {
			super();
			this.maxLengthHrfInMs = maxLengthHrfInMs;
			this.peak1 = peak1;
			this.scale1 = scale1;
			this.peak2 = peak2;
			this.scale2 = scale2;
			this.offset = offset;
			this.relationP1P2 = relationP1P2;
			this.heightScale = heightScale;
			this.timeUnit = timeUnit;
		}
	}
	
	public static class GeneralGammaParams {
		public int maxLengthHrfInMs; // unsigned int
		public double peak1;
		public double scale1;
		public double peak2;
		public double scale2;
	}
	
	public float[][] plotGammaWithDerivs(final int derivs); // unsigned int
}
