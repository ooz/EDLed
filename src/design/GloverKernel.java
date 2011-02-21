package design;

public class GloverKernel {
	
	public static class GloverParams {
		public long maxLengthHrfInMs; // must be positive
		public double peak1; //fka a1 - 6
		public double scale1; //fka b1 - 0.9
		public double peak2; // fka a2 - 12
		public double scale2; //fka b2 - 0.9
		public double offset;	// fka hard coded - for gamma 0.0, gauss 5.0
		public double relationP1P2; // fka cc cx - for block 0.1, event 0.35
		public double heightScale; //fka hard coded voodoo scale - for block 120, event 20
		public DesignKernelTimeUnit timeUnit;
	}
	
	public static enum DesignKernelTimeUnit {
		KERNEL_TIME_MS,
		KERNEL_TIME_S;
	}
	
	public static class GeneralGammaParams {
		public long maxLengthHrfInMs; // must be positive
		double peak1;
		double scale1;
		double peak2;
		double scale2;
	}
	
	// From NEDesignGloverKernel.h/m:
	private GloverParams params;
	private double numberSamplesForInit;
	private double samplingRateInMs;
	private double scaleTimeUnit;
	
	GloverKernel(GloverParams params, double numberSamples) {
		
	}
	
	public static float[][] plotGammaWithDerivs(long derivs) {
		return null;
	}
	
	private void generateGammaKernel() { }
	private double getGammaValue(double val, double offset_t0) { return 0.0; }
	private double getGammaDeriv1Value(double val, double offset_t0) { return 0.0; }
	private double getGammaDeriv2Value(double val, double offset_t0) { return 0.0; }

}
