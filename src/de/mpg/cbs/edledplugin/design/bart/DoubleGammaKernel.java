package de.mpg.cbs.edledplugin.design.bart;

import flanagan.complex.Complex;
import flanagan.math.FourierTransform;

public class DoubleGammaKernel extends DesignKernel {
	
	public static class GammaParams {
		/* Time unit: ms */
		public static final double TPEAK1_DEFAULT  = 5400.0;
		public static final double MWIDTH1_DEFAULT = 2500.0;
		public static final double SCALE1_DEFAULT  = 1.0;
		public static final double TPEAK2_DEFAULT  = 8800.0;
		public static final double MWIDTH2_DEFAULT = 3350.0;
		public static final double SCALE2_DEFAULT  = 0.25;
		public static final double OFFSET_DEFAULT  = 0.0;
		
		public static final double SPM_TPEAK1_DEFAULT  = 6000.0;
		public static final double SPM_MWIDTH1_DEFAULT = 5100.0;
		public static final double SPM_SCALE1_DEFAULT  = 1.0;
		public static final double SPM_TPEAK2_DEFAULT  = 16000.0;
		public static final double SPM_MWIDTH2_DEFAULT = 8000.0;
		public static final double SPM_SCALE2_DEFAULT  = 0.09;
		public static final double SPM_OFFSET_DEFAULT  = 0.0;
		
		/** Time to response-peak. */
		public double tPeak1;
		/** Dispersion of response. */
		public double mWidth1;
		/** Scale of response. */
		public double scale1;
		/** Time to undershoot-valley. */
		public double tPeak2;
		/** Dispersion of undershoot. */
		public double mWidth2;
		/** Scale of undershoot. */
		public double scale2;
		/** Offset of the function. */
		public double offset;
		/** Max length of the function domain. */
		public double overallWidth;
		
		public DesignKernelTimeUnit timeUnit;
		
		public GammaParams(final int overallWidth,
						   final boolean useSPMDefaults) {
			super();
			if (useSPMDefaults) {
				this.tPeak1 	  = SPM_TPEAK1_DEFAULT;
				this.mWidth1      = SPM_MWIDTH1_DEFAULT; 
				this.scale1		  = SPM_SCALE1_DEFAULT; 
				this.tPeak2       = SPM_TPEAK2_DEFAULT;
				this.mWidth2      = SPM_MWIDTH2_DEFAULT; 
				this.scale2       = SPM_SCALE2_DEFAULT; 
				this.offset       = SPM_OFFSET_DEFAULT; 
				this.overallWidth = overallWidth;
				this.timeUnit     = DesignKernelTimeUnit.KERNEL_TIME_MS;
			} else {
				this.tPeak1       = TPEAK1_DEFAULT;
				this.mWidth1      = MWIDTH1_DEFAULT; 
				this.scale1       = SCALE1_DEFAULT;
				this.tPeak2       = TPEAK2_DEFAULT; 
				this.mWidth2	  = MWIDTH2_DEFAULT; 
				this.scale2       = SCALE2_DEFAULT;
				this.offset       = OFFSET_DEFAULT; 
				this.overallWidth = overallWidth;
				this.timeUnit     = DesignKernelTimeUnit.KERNEL_TIME_MS;
			}
		}
		public GammaParams(final double tPeak1, 
						   final double mWidth1, 
						   final double scale1,
						   final double tPeak2,
						   final double mWidth2,
						   final double scale2,
						   final double offset,
						   final double overallWidth,
						   final DesignKernelTimeUnit timeUnit) {
			super();
			this.tPeak1       = tPeak1;
			this.mWidth1      = mWidth1;
			this.scale1       = scale1;
			
			this.tPeak2       = tPeak2;
			this.mWidth2      = mWidth2;
			this.scale2       = scale2;
			
			this.offset       = offset;
			this.overallWidth = overallWidth;
			
			this.timeUnit     = timeUnit;
		}
	}
	
	/* Three constants used in the double gamma function and its derivatives. */
	private static final double C1 = 2;
	private static final double C2 = 8.0;
	private static final double C3 = Math.log(2);
	
	private final GammaParams params;
	double numberSamplesForInit; // unsigned long
	double samplingRateInMs; // unsigned long
	private final double scaleTimeUnit;
	
	public DoubleGammaKernel(final String id,
							 final GammaParams params, 
							 final double numberSamplesForInit,
							 final double samplingRate) {
		super(id);
		this.params = params;
		this.numberSamplesForInit = numberSamplesForInit;
		this.samplingRateInMs = samplingRate;
		
		if (this.params.timeUnit == DesignKernelTimeUnit.KERNEL_TIME_MS) {
			this.scaleTimeUnit = 0.001;
		} else {
			this.scaleTimeUnit = 1.0;
		}
		
		generateGammaKernel();
	}
	
	private void generateGammaKernel() {
		// unsigned long
		long numberSamplesInResult = ((long) this.numberSamplesForInit / 2) + 1;//defined for results of fftw3
		/*always generate with both derivates - so you can ask member variables if you need them*/
		double[] kernel0 = new double[(int) this.numberSamplesForInit];//just temp to write values in
		this.kernelDeriv0 =  new Complex[(int) numberSamplesInResult];
		
		double[] kernel1 = new double[(int) this.numberSamplesForInit];
		this.kernelDeriv1 = new Complex[(int) numberSamplesInResult];
		
		double[] kernel2 = new double[(int) this.numberSamplesForInit];
		this.kernelDeriv2 = new Complex[(int) numberSamplesInResult];
		
		for (int i = 0; i < this.numberSamplesForInit; i++) {
			kernel0[i] = 0.0;
			kernel1[i] = 0.0;
			kernel2[i] = 0.0;
		}
		
		// sample the whole stuff e.g. something bout 20 ms;
		int indexS = 0;
		for (long timeSample = 0; timeSample < this.params.overallWidth; timeSample += this.samplingRateInMs) {
			if (indexS >= this.numberSamplesForInit) break;        
			//unsigned long indexS = (unsigned long)timeSample/mSamplingRateInMs;
			kernel0[indexS] = getGammaValue(timeSample, this.params.offset);
			kernel1[indexS] = getGammaDeriv1Value(timeSample, this.params.offset);
			kernel2[indexS] = getGammaDeriv2Value(timeSample, this.params.offset);
			indexS++;
		}

		/* do fft for kernels right now - result buffers are the members the convolution will ask for*/
		double[] paddedKernel0 = DesignElement.padToNextPowerOfTwo(kernel0);
		FourierTransform pk0 = new FourierTransform(paddedKernel0);
		pk0.transform();
		this.kernelDeriv0 = pk0.getTransformedDataAsComplex();
		
		FourierTransform pk1 = new FourierTransform(DesignElement.padToNextPowerOfTwo(kernel1));
		pk1.transform();
		this.kernelDeriv1 = pk1.getTransformedDataAsComplex();
		
		FourierTransform pk2 = new FourierTransform(DesignElement.padToNextPowerOfTwo(kernel2));
		pk2.transform();
		this.kernelDeriv2 = pk2.getTransformedDataAsComplex();
	}

	@Override
	public float[][] plotGammaWithDerivs(int derivs) {
		double gammaFct;
	    double gammaDeriv1;
	    double gammaDeriv2;
	    double t0 = 0.0;
	    double step = 0.2;
	    
	    int ncols = (int) (28.0 / step);
	    int nrows = derivs + 2;
	    
	    float[][] dest = new float[ncols][nrows];
	    for (int col = 0; col < ncols; col++) {
	        for (int row = 0; row < nrows; row++) {
	            dest[col][row] = 0.0f;
	        }
	    }
	    
	    int j = 0;
	    for (double x = 0.0; x < 28.0; x += step) {
	        if (j >= ncols) {
	            break;
	        }

	        gammaFct = getGammaValue(x, t0);
	        gammaDeriv1 = getGammaDeriv1Value(x, t0);
	        gammaDeriv2 = getGammaDeriv2Value(x, t0);
			
	        dest[j][0] = (float) x;
	        dest[j][1] = (float) gammaFct;
	        if (derivs > 0) {
	            dest[j][2] = (float) gammaDeriv1;
	        }
	        if (derivs > 1) {	
	            dest[j][3] = (float) gammaDeriv2;
	        }
	        j++;
	    }
	    
	    return dest;
	}
	
	private double getGammaValue(final double val, final double t0) {
		double x = (val - t0); // * this.scaleTimeUnit;// scale to s
		if (x < 0.0 || x > 50.0) {
			return 0.0;
		}
		
//		System.out.println("DoubleGammaKernel.getGammaValue: x=" + x + " scaleTimeUnit=" + scaleTimeUnit);
		
		double A1 = this.params.tPeak1  * this.scaleTimeUnit;
		double W1 = this.params.mWidth1 * this.scaleTimeUnit;
		double K1 = this.params.scale1;
		double A2 = this.params.tPeak2  * this.scaleTimeUnit;
		double W2 = this.params.mWidth2 * this.scaleTimeUnit;
		double K2 = this.params.scale2;
		
		return K1 * Math.pow((x / A1),(Math.pow(A1, 2.0) / Math.pow(W1, C1) * C2 * C3)) 
	           * Math.exp((x - A1) / -(Math.pow(W1, 2.0) / A1 / C2 / C3)) 
			   - K2 * Math.pow(x / A2, Math.pow(A2, 2.0) / Math.pow(W2, C1) * C2 * C3) 
			   * Math.exp((x-A2) / -(Math.pow(W2, 2.0) / A2 / C2 / C3));
	}
	
	private double getGammaDeriv1Value(final double val, final double t0) {
		double x = (val - t0); // * this.scaleTimeUnit;// scale to s
		if (x < 0.0 || x > 50.0) {
			return 0.0;
		}
		
		double A1 = this.params.tPeak1  * this.scaleTimeUnit;
		double W1 = this.params.mWidth1 * this.scaleTimeUnit;
		double K1 = this.params.scale1;
		double A2 = this.params.tPeak2  * this.scaleTimeUnit;
		double W2 = this.params.mWidth2 * this.scaleTimeUnit;
		double K2 = this.params.scale2;
		
		return (A2 * C2 * C3 * K2 
				* Math.exp((A2 * C2 * C3 * (A2 - x)) / Math.pow(W2, 2.0))
				* Math.pow((x / A2), ((Math.pow(A2, 2.0) * C2 * C3) / Math.pow(W2, C1))))
			   / Math.pow(W2, 2.0) 
			   - (A1 * C2 * C3 * K1 
			      * Math.exp((A1 * C2 * C3 * (A1 - x)) / Math.pow(W1, 2.0)) 
			      * Math.pow((x / A1), ((Math.pow(A1, 2.0) * C2 * C3) / Math.pow(W1, C1)))) 
			   / Math.pow(W1, 2.0) 
			   + (A1 * C2 * C3 * K1 * Math.exp((A1 * C2 * C3 * (A1 - x)) / Math.pow(W1, 2.0)) * Math.pow((x/A1), ((Math.pow(A1, 2.0) * C2 * C3) / Math.pow(W1, C1) - 1)))
			   / Math.pow(W1, C1) 
			   - (A2 * C2 * C3 * K2 * Math.exp((A2 * C2 * C3 * (A2 - x)) / Math.pow(W2, 2.0)) * Math.pow((x / A2), ((Math.pow(A2, 2.0) * C2 * C3) / Math.pow(W2, C1) - 1))) 
			   / Math.pow(W2, C1);
	}
	
	private double getGammaDeriv2Value(final double val, final double t0) {
		double x = (val - t0); // * this.scaleTimeUnit;// scale to s
		if (x < 0.0 || x > 50.0) {
			return 0.0;
		}
		
		double A1 = this.params.tPeak1  * this.scaleTimeUnit;
		double W1 = this.params.mWidth1 * this.scaleTimeUnit;
		double K1 = this.params.scale1;
		double A2 = this.params.tPeak2  * this.scaleTimeUnit;
		double W2 = this.params.mWidth2 * this.scaleTimeUnit;
		double K2 = this.params.scale2;
		
		return (C2 * C3 * K1 
				* Math.exp((A1 * C2 * C3 * (A1 - x)) / Math.pow(W1, 2.0))
				* ((Math.pow(A1, 2.0) * C2 * C3) / Math.pow(W1, C1) - 1)
				* Math.pow((x / A1), ((Math.pow(A1, 2.0) * C2 * C3) / Math.pow(W1, C1) - 2))) 
				/ Math.pow(W1, C1) 
				- (C2 * C3 * K2 
				   * Math.exp((A2 * C2 * C3 * (A2 - x)) / Math.pow(W2, 2.0)) 
				   * ((Math.pow(A2, 2.0) * C2 * C3) / Math.pow(W2, C1) - 1) 
				   * Math.pow((x / A2), ((Math.pow(A2, 2.0) * C2 * C3) / Math.pow(W2, C1) - 2)))
				/ Math.pow(W2, C1) 
				+ (Math.pow(A1, 2.0) * Math.pow(C2, 2.0) * Math.pow(C3, 2.0) * K1 
				   * Math.exp((A1 * C2 * C3 * (A1 - x)) / Math.pow(W1, 2.0))
				   * Math.pow((x / A1), ((Math.pow(A1, 2.0) * C2 * C3) / Math.pow(W1, C1))))
				/ Math.pow(W1, 4.0) 
				- (Math.pow(A2, 2.0) * Math.pow(C2, 2.0) * Math.pow(C3, 2.0) * K2 
				   * Math.exp((A2 * C2 * C3 * (A2 - x)) / Math.pow(W2, 2.0))
				   * Math.pow((x / A2), ((Math.pow(A2, 2.0) * C2 * C3) / Math.pow(W2, C1)))) 
				/ Math.pow(W2, 4.0) 
				- (2.0 * Math.pow(A1, 2.0) * Math.pow(C2, 2.0) * Math.pow(C3, 2.0) * K1 
				   * Math.exp((A1 * C2 * C3 * (A1 - x)) / Math.pow(W1, 2.0))
				   * Math.pow((x / A1), ((Math.pow(A1, 2.0) * C2 * C3) / Math.pow(W1, C1) - 1))) 
				/ (Math.pow(W1, 2.0) * Math.pow(W1, C1)) 
				+ (2.0 * Math.pow(A2, 2.0) * Math.pow(C2, 2.0) * Math.pow(C3, 2.0) * K2 
				   * Math.exp((A2 * C2 * C3 * (A2 - x)) / Math.pow(W2, 2.0)) 
				   * Math.pow((x / A2), ((Math.pow(A2, 2.0) * C2 * C3) / Math.pow(W2, C1) - 1)))
				/ (Math.pow(W2, 2.0) * Math.pow(W2, C1));
	}

}
