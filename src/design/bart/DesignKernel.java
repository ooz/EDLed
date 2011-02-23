package design.bart;

/* From NEDesignKernel.h */
public interface DesignKernel {
	public enum DesignKernelTimeUnit {
		KERNEL_TIME_MS,
		KERNEL_TIME_S;
	}
	
	public static class GloverParams {
		public long maxLengthHrfInMs; // unsigned int
		public double peak1;
		public double scale1;
		public double peak2;
		public double scale2;
		public double offset;
		public double relationP1P2;
		public double heightScale;
		public DesignKernelTimeUnit timeUnit;
		/** Constructor */
		public GloverParams(long maxLengthHrfInMs, double peak1, double scale1,
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
		public long maxLengthHrfInMs; // unsigned int
		public double peak1;
		public double scale1;
		public double peak2;
		public double scale2;
	}
	
	public float[][] plotGammaWithDerivs(final int derivs); // unsigned int
}
