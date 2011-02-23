package design.bart;

import design.bart.DesignKernel.GeneralGammaParams;
import design.bart.DesignKernel.GloverParams;

/** From NEDesignGloverKernel.h/m */
public class DesignGloverKernel implements DesignKernel {
	
	private GloverParams params;
	double numberSamplesForInit; // unsigned long
	double samplingRateInMs; // unsigned long
	double scaleTimeUnit;
	
	final FFTW.Complex[] kernelDeriv0 = {};
	final FFTW.Complex[] kernelDeriv1 = {};
	final FFTW.Complex[] kernelDeriv2 = {};
	
	public DesignGloverKernel(final GloverParams gammaParams, 
							  final double numberSamplesForInit, 
							  final double samplingRate) {
		if (gammaParams == null
			|| numberSamplesForInit < 0.0
			|| samplingRate < 0.0) {
			throw new IllegalArgumentException("Tried to create new DesignGloverKernel with illegal arguments.");
		}
		
		this.params = gammaParams;
		this.numberSamplesForInit = numberSamplesForInit;
		this.samplingRateInMs = samplingRate;
		
		if (this.params.timeUnit == DesignKernelTimeUnit.KERNEL_TIME_MS) {
			this.scaleTimeUnit = 0.001;
		} else {
			this.scaleTimeUnit = 1.0;
		}
		
		generateGammaKernel();
	}
	
//	public DesignGloverKernel(final GeneralGammaParams gammaParams) {
//
//	}
	
	private void generateGammaKernel() {
//		// unsigned long
//		long numberSamplesInResult = ((long) this.numberSamplesForInit / 2) + 1;//defined for results of fftw3
//		/*always generate with both derivates - so you can ask member variables if you need them*/
//		double[] kernel0 = new double[(int) this.numberSamplesForInit];//just temp to write values in
//		mKernelDeriv0 = (fftw_complex *)fftw_malloc (sizeof(fftw_complex) * numberSamplesInResult);
//		memset(kernel0, 0.0, sizeof(double) * mNumberSamplesForInit);
//		
//		double *kernel1 = NULL;
//		kernel1  = (double *)fftw_malloc(sizeof(double) * mNumberSamplesForInit);
//		mKernelDeriv1 = (fftw_complex *)fftw_malloc (sizeof (fftw_complex) * numberSamplesInResult);
//		memset(kernel1,0.0,sizeof(double) * mNumberSamplesForInit);
//		
//		double *kernel2 = NULL;
//		kernel2  = (double *)fftw_malloc(sizeof(double) * mNumberSamplesForInit);
//		mKernelDeriv2 = (fftw_complex *)fftw_malloc (sizeof (fftw_complex) * numberSamplesInResult);
//		memset(kernel2,0.0,sizeof(double) * mNumberSamplesForInit);
//		
//		// sample the whole stuff e.g. something bout 20 ms;
//		unsigned int indexS = 0;
//		for (unsigned long timeSample = 0; timeSample < mParams.maxLengthHrfInMs; timeSample += mSamplingRateInMs) {
//			if (indexS >= mNumberSamplesForInit) break;        
//			//unsigned long indexS = (unsigned long)timeSample/mSamplingRateInMs;
//			kernel0[indexS] = getGammaValue(timeSample, this.params.offset);
//			kernel1[indexS] = getGammaDeriv1Value(timeSample, params.offset);
//			kernel2[indexS] = getGammaDeriv2Value(timeSample, params.offset);
//			indexS++;
//		}
//
//		/* do fft for kernels right now - result buffers are the members the convolution will ask for*/
//		fftw_plan pk0;
//		pk0 = fftw_plan_dft_r2c_1d(mNumberSamplesForInit, kernel0, mKernelDeriv0, FFTW_ESTIMATE);
//		fftw_execute(pk0);
//		
//		fftw_plan pk1;
//		pk1 = fftw_plan_dft_r2c_1d(mNumberSamplesForInit, kernel1, mKernelDeriv1, FFTW_ESTIMATE);
//		fftw_execute(pk1);
//		
//		fftw_plan pk2;
//		pk2 = fftw_plan_dft_r2c_1d(mNumberSamplesForInit, kernel2, mKernelDeriv2, FFTW_ESTIMATE);
//		fftw_execute(pk2);
//		
//		fftw_free(kernel0);
//		fftw_free(kernel1);
//		fftw_free(kernel2);
	}

	@Override
	public float[][] plotGammaWithDerivs(final int derivs) {
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
	
	/**
	 * 
	 * @param val
	 * @param t0  Offset.
	 * @return
	 */
	private double getGammaValue(final double val, 
								 final double t0) {
		double x = (val - t0) * this.scaleTimeUnit;// scale to s
	    if (x < 0.0 || x > 50.0) {
	        return 0.0;
	    }
	    
		double peak1 = params.peak1 * scaleTimeUnit;
		double peak2 = params.peak2 * scaleTimeUnit;
		double d1 = peak1 * params.scale1;
	    double d2 = peak2 * params.scale2;
	    
		double overshootFct = Math.pow(x / d1, peak1) * Math.exp(-(x - d1) / params.scale1);
	    double undershootFct = Math.pow(x / d2, peak2) * Math.exp(-(x - d2) / params.scale2);
		double gammaFct = overshootFct - params.relationP1P2 * undershootFct;
	    gammaFct /= params.heightScale;
	    
	    return gammaFct;
	}
	
	/**
	 * First derivative.
	 * 
	 * @param val
	 * @param t0  Offset.
	 * @return
	 */
	private double getGammaDeriv1Value(final double val,
									   final double t0) {
		double x = (val - t0) * scaleTimeUnit;
	    if (x < 0.0 || x > 50.0) {
	        return 0.0;
	    }
	    
		double peak1 = params.peak1 * scaleTimeUnit;
		double peak2 = params.peak2 * scaleTimeUnit;
	   	double d1 = peak1 * params.scale1;
	    double d2 = peak2 * params.scale2;

	    
	    double overshootFct = Math.pow(d1, -peak1) * peak1 * Math.pow(x, (peak1 - 1.0)) * Math.exp(-(x - d1) / params.scale1) 
		- (Math.pow((x / d1), peak1) * Math.exp(-(x - d1) / params.scale1)) / params.scale1;
	    
	    double undershootFct = Math.pow(d2, -peak2) * peak2 * Math.pow(x, (peak2 - 1.0)) * Math.exp(-(x - d2) / params.scale2) 
		- (Math.pow((x / d2), peak2) * Math.exp(-(x - d2) / params.scale2)) / params.scale2;
	    
	    double gammFct = overshootFct - params.relationP1P2 * undershootFct;
		gammFct /= params.heightScale;
	    
	    return gammFct;
	}
	
	/**
	 * Second derivative.
	 * 
	 * @param val
	 * @param t0
	 * @return
	 */
	private double getGammaDeriv2Value(final double val, 
									   final double t0) {
		double x = (val - t0) * scaleTimeUnit;
	    if (x < 0.0 || x > 50.0) {
	        return 0.0;
	    }
	    
		double peak1 = params.peak1 * scaleTimeUnit;
		double peak2 = params.peak2 * scaleTimeUnit;
	   	double d1 = peak1 * params.scale1;
	    double d2 = peak2 * params.scale2;

		double overshootFct1 = Math.pow(d1, -peak1) * peak1 * (peak1 - 1) * Math.pow(x, peak1 - 2) * Math.exp(-(x - d1) / params.scale1) 
					- Math.pow(d1, -peak1) * peak1 * Math.pow(x, (peak1 - 1)) * Math.exp(-(x - d1) / params.scale1) / params.scale1;
		
	    double overshootFct2 = Math.pow(d1, -peak1) * peak1 * Math.pow(x, peak1 - 1) * Math.exp(-(x - d1) / params.scale1) / params.scale1
					- Math.pow((x / d1), peak1) * Math.exp(-(x - d1) / params.scale1) / (params.scale1 * params.scale1);
	    
		double undershootFct1 = Math.pow(d2, -peak2) * peak2 * (peak2 - 1) * Math.pow(x, peak2 - 2) * Math.exp(-(x - d2) / params.scale2) 
					- Math.pow(d2, -peak2) * peak2 * Math.pow(x, (peak2 - 1)) * Math.exp(-(x - d2) / params.scale2) / params.scale2;
		
	    double undershootFct2 = Math.pow(d2, -peak2) * peak2 * Math.pow(x, peak2 - 1) * Math.exp(-(x - d2) / params.scale2) / params.scale2
					- Math.pow((x / d2), peak2) * Math.exp(-(x - d2) / params.scale2) / (params.scale2 * params.scale2);
	    
		double gammaFct = (overshootFct1 - overshootFct2) - params.relationP1P2 * (undershootFct1 - undershootFct2);
	    gammaFct /= params.heightScale;
	    
	    return gammaFct;
	}
}
