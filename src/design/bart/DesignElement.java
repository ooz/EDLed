package design.bart;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import flanagan.complex.Complex;
import flanagan.math.FourierTransform;

public class DesignElement extends Observable {
	
	/* ===== Classes ===== */
	/* From BAElement.h */
	public enum ImageDataType {
	    IMAGE_DATA_FLOAT,
	    IMAGE_DATA_SHORT,
		IMAGE_DATA_BYTE,
		IMAGE_DATA_UBYTE,
		IMAGE_DATA_USHORT;
	}
	
	/* From BADesignElement.h */
	public static class Trial {
		public long  id = 0; // Stimulus number: unsigned int
		public float onset = 0.0f;
		public float duration = 1.0f; // in seconds
		public float height = 1.0f;
		public Trial() {
		}
		public Trial(final long id, final float onset, final float duration, final float height) {
			this.id = id;
			this.onset = onset;
			this.duration = duration;
			this.height = height;
		}
		@Override
		public String toString() {
			return "Trial[id="       + this.id 
			          + ",onset="    + this.onset 
			          + ",duration=" + this.duration 
			          + ",height="   + this.height + "]";
		}
	}
	
	/* From NEDesignElementDyn */
	public static class Regressor {
		public List<Trial> regTrialList = new LinkedList<Trial>();
		public long regDerivations = 0; // unsigned int
		public String regID = "";
		public String regDescription = "";
		public DesignKernel regConvolKernel = null;
		
		@Override
		public String toString() {
			return "Regressor[regTrialList.size()="       + this.regTrialList.size() 
			             + ", regDerivations="            + this.regDerivations 
			             + ", regID=" 					  + this.regID 
			             + ", regDescription="   		  + this.regDescription + "]";
		}
	}
	
	/* ===== Constants ===== */
	/** Temporal resolution for convolution is 20 ms. */
	public static final double SAMPLING_RATE_IN_MS = 20.0; 
	/** Add some seconds to avoid wrap around problems with FFT. */
	public static final int    WRAP_AROUND_PADDING_IN_MS = 10000;
	
	/* ===== Attributes ===== */
	/* From BADesignElement.h */
	private long repetitionTimeInMs = 0; 		 // unsigned int
	private long numberExplanatoryVariables = 0; // unsigned int
	private int numberTimesteps = 0;   		     // unsigned int
	private long numberRegressors = 0; 			 // unsigned int
	private long numberCovariates = 0; 		     // unsigned int
	private ImageDataType imageDataType = ImageDataType.IMAGE_DATA_FLOAT;
	/* From NEDesignElementDyn.h */
	private List<Regressor> regressorList = new LinkedList<Regressor>();
	private int numberEvents = 0; 				// unsigned int
	private long numberSamplesForInit = 0; 		// unsigned long
//	private long numberSamplesNeededForExp = 0; // unsigned long
	private double[] timeOfRepetitionStartInMs;

	/** Generated/resulting design */
	private float[][] regressorValues = new float[0][0];
	private float[][] covariateValues;
	/** FFT buffers */
	private double[][] buffersForwardIn;  // one per each event
	private Complex[][] buffersForwardOut; // resulting HRFs (one per event)
	private Complex[][] buffersInverseIn;
	private double[][] buffersInverseOut; // one per each event
	/** Plans for FFT */
	private FourierTransform[] fftPlanForward;
	private FourierTransform[] fftPlanInverse;
	
	/* ===== Constructors ===== */
	
	/* ===== Static functions ===== */
	public static double[] padToNextPowerOfTwo(final double[] data) {
		double[] result = new double[FourierTransform.nextPowerOfTwo(data.length)];
//		System.out.println("(double) Data length: " + data.length + " Padded length: " + FourierTransform.nextPowerOfTwo(data.length));
//		System.out.println("    Padded " + (FourierTransform.nextPowerOfTwo(data.length) - data.length) + " elements");
		
		// Pad at end (data at beginning)
//		for (int i = 0; i < data.length; i++) {
//			result[i] = data[i];
//		}
//		for (int i = data.length; i < result.length; i++) {
//			result[i] = 0.0;
//		}
		// Pad at beginning (data at end)
//		int j = data.length - 1;
//		for (int i = result.length - 1; i >= 0; i--) {
//			if (j >= 0) {
//				result[i] = data[j--];
//			} else {
//				result[i] = 0.0;
//			}
//		}
		// Center data, pad at beginning and end
		int j = 0;
		int beginningLength = (result.length - data.length) / 2;
		for (int i = 0; i < result.length; i++) {
			if (i < beginningLength
				|| i >= beginningLength + data.length) {
				result[i] = 0.0;
			} else {
				result[i] = data[j++];
			}
		}
		
		return result;
	}
	public static Complex[] padToNextPowerOfTwo(final Complex[] data) {
		Complex[] result = new Complex[FourierTransform.nextPowerOfTwo(data.length)];
//		System.out.println("(Complex) Data length: " + data.length + " Padded length: " + FourierTransform.nextPowerOfTwo(data.length));
//		System.out.println("    Padded " + (FourierTransform.nextPowerOfTwo(data.length) - data.length) + " elements");
		
		// Pad at end (data at beginning)
		for (int i = 0; i < data.length; i++) {
			result[i] = data[i];
		}
		for (int i = data.length; i < result.length; i++) {
			result[i] = new Complex();
		}
		// Pad at beginning (data at end)
//		int j = data.length - 1;
//		for (int i = result.length - 1; i >= 0; i--) {
//			if (j >= 0) {
//				result[i] = data[j--];
//			} else {
//				result[i] = new Complex();
//			}
//		}
		// Center data, pad at beginning and end
//		int j = 0;
//		int beginningLength = (result.length - data.length) / 2;
//		for (int i = 0; i < result.length; i++) {
//			if (i < beginningLength
//				|| i >= beginningLength + data.length) {
//				result[i] = new Complex();
//			} else {
//				result[i] = data[j++];
//			}
//		}
		
		return result;
	}
	
	/* ===== Methods ===== */
	public void convolve(final int col, 
						 final int eventNr, 
						 final Complex[] kernel) {
		if (kernel == null) {
			throw new IllegalArgumentException("called DesignElement.convolve with kernel=null!");
		}
		
		int numberSamplesResult = (int) (this.numberSamplesForInit / 2) + 1; // fftw3 definition
		
		// Convolution
		for (int j = 0; j < numberSamplesResult; j++) { // unsigned int
			Complex valueEventSeries = new Complex(this.buffersForwardOut[eventNr][j].getReal(), 
												   this.buffersForwardOut[eventNr][j].getImag());
			Complex valueGammaKernel = new Complex(kernel[j].getReal(), 
												   kernel[j].getImag());
			this.buffersInverseIn[eventNr][j] = multiplComplex(valueEventSeries, valueGammaKernel);
		}
		
		// Inverse FFT
		this.fftPlanInverse[eventNr] = new FourierTransform(DesignElement.padToNextPowerOfTwo(buffersInverseIn[eventNr])); //= fftw_plan_dft_c2r_1d(mNumberSamplesForInit, mBuffersInverseIn[eventNr], mBuffersInverseOut[eventNr], FFTW_ESTIMATE);
		this.fftPlanInverse[eventNr].transform();
		this.buffersInverseOut[eventNr] = this.fftPlanInverse[eventNr].getTransformedDataAsAlternate();
		
		// Scaling
		for (int j = 0; j < this.numberSamplesForInit; j++) {
			this.buffersInverseOut[eventNr][j] /= (double) this.numberSamplesForInit;
		}
		
//		System.out.println("inverseOut.length: " + this.buffersInverseOut[eventNr].length);
		
		int padding = DesignElement.WRAP_AROUND_PADDING_IN_MS - 2;
		
		int transformedTSCount = (int) (this.numberTimesteps * (this.repetitionTimeInMs / SAMPLING_RATE_IN_MS)) + padding;
		// Sampling
		for (int timestep = 0; timestep < this.numberTimesteps; timestep++) {
			int j = (int) (this.timeOfRepetitionStartInMs[timestep] / SAMPLING_RATE_IN_MS);
//			System.out.println("start time: " + this.timeOfRepetitionStartInMs[timestep]);
//			System.out.println("j = " + j);
			if (j >= 0 && j < this.numberSamplesForInit) {
				this.regressorValues[col][timestep] = (float) this.buffersInverseOut[eventNr][transformedTSCount - j];
			}
		}
		
		this.setChanged();
	}
	
	private Complex multiplComplex(final Complex a, 
								   final Complex b) {
		return new Complex(a.getReal() * b.getReal() - a.getImag() * b.getImag(),
						   a.getReal() * b.getImag() + a.getImag() * b.getReal());
	}
	
	/**
	 * Computes the orthogonality between two design matrix columns.
	 * Column numbers start at 0, so regressor 1 is at column 0.
	 * 
	 * Column ordering:
	 *  Regressor 1 (column 0), 
	 *  optional first Deriv 1 (col 1), 
	 *  optional second Deriv 1 (col 2), 
	 *  Regressor 2 (col 3), etc.
	 * 
	 * If there are no/not all derivatives the indices collapse.
	 * 
	 * @param colA Index of column A. Ranges from 0 to (numberRegressors - 2).
	 * @param colB Index of column B. Ranges from 0 to (numberRegressors - 2).
	 * @return
	 */
	public float computeOrthogonality(final int colA, final int colB) {
//		int colsPerReg = (int) (this.numberRegressors / this.regressorList.size());

		float orthogonality = 0.0f;
		for (int ts = 0; ts < this.numberTimesteps; ts++) {
			orthogonality +=   Math.abs(this.regressorValues[colA /* * colsPerReg */][ts]) 
			                 * Math.abs(this.regressorValues[colB /* * colsPerReg */][ts]);
		}
		
		return orthogonality;
	}
	public float[][] computeOrthogonalityMatrix() {
//		int regCount = this.regressorList.size();
		int regCount = (int) this.numberRegressors - 1;
		float[][] matrix = new float[regCount][regCount];
		
		for (int regA = 0; regA < regCount; regA++) {
			for (int regB = 0; regB < regCount; regB++) {
				if (regA == regB) {
					// Parallel
					matrix[regA][regA] = computeOrthogonality(regA, regA);; 
				} else if (regB < regA) {
					// value was already computed
					matrix[regA][regB] = matrix[regB][regA];
				} else {
					matrix[regA][regB] = computeOrthogonality(regA, regB);
				}
			}
		}
		
		return matrix;
	}
	
	// TODO refactor: make private method when DesignElement is directly
	// updated with the data from the DOM nodes (not via DOMFormatter)
	public void correctForZeromean() {
		for (int i = 0; i < this.numberEvents; i++) {
			float sum1 = 0.0f;
			float sum2 = 0.0f;
//			float nx   = 0.0f;
			
			List<Trial> trials = this.regressorList.get(i).regTrialList;
			for (Trial trial : trials) {
				sum1 += trial.height;
				sum2 += trial.height * trial.height;
//				nx++;
			}
			
			float trialCount = (float) trials.size();
			if (trialCount > 1.0) {
				float mean = sum1 / trialCount;
				float sigma = (float) Math.sqrt((double) ((sum2 - trialCount * mean * mean) / (trialCount - 1.0)));
				if (sigma < 0.01f) {
					continue; // Not a parametric covariate.
				}
				
				for (Trial trial : trials) {
					trial.height -= mean;
				}
			}
		}
	}
	public void initRegressorValues() {
		this.regressorValues = new float[(int) this.numberRegressors][this.numberTimesteps];
		for (int col = 0; col < this.numberRegressors; col++) {
			for (int ts = 0; ts < this.numberTimesteps; ts++) {
				if (col == this.numberRegressors - 1) {
					this.regressorValues[col][ts] = 1.0f;
				} else {
					this.regressorValues[col][ts] = 0.0f;
				}
			}
	    }
	}
	public void initCovariateValues() {
		this.covariateValues = new float[(int) this.numberCovariates][this.numberTimesteps];
		for (int cov = 0; cov < this.numberCovariates; cov++) {
			for (int ts = 0; ts < this.numberTimesteps; ts++) {
				this.covariateValues[cov][ts] = 0.0f;
			}
		}
	}
	
	/* Getter and setters. */
	public long getRepetitionTimeInMs() { return repetitionTimeInMs; }
	public void setRepetitionTimeInMs(long repetitionTimeInMs) { this.repetitionTimeInMs = repetitionTimeInMs; }
	public long getNumberExplanatoryVariables() { return numberExplanatoryVariables; }
	public void setNumberExplanatoryVariables(long numberExplanatoryVariables) { this.numberExplanatoryVariables = numberExplanatoryVariables; }
	public int getNumberTimesteps() { return numberTimesteps; }
	public void setNumberTimesteps(int numberTimesteps) { this.numberTimesteps = numberTimesteps; }
	public long getNumberRegressors() { return numberRegressors; }
	public void setNumberRegressors(long numberRegressors) { this.numberRegressors = numberRegressors; }
	public long getNumberCovariates() { return numberCovariates; }
	public void setNumberCovariates(long numberCovariates) { this.numberCovariates = numberCovariates; }
	public ImageDataType getImageDataType() { return imageDataType;	}
	public void setImageDataType(ImageDataType imageDataType) {	this.imageDataType = imageDataType;	}
	
	// regressorList
	public List<Regressor> getRegressorList() { return regressorList; }
	public void setRegressorList(List<Regressor> regressorList) { this.regressorList = regressorList; }
	// numberEvents
	public int getNumberEvents() { return numberEvents; }
	public void setNumberEvents(int numberEvents) {	this.numberEvents = numberEvents; }
	// numberSamplesForInit
	public long getNumberSamplesForInit() { return numberSamplesForInit; }
	public void setNumberSamplesForInit(long numberSamplesForInit) { this.numberSamplesForInit = numberSamplesForInit; }
	// timeOfRepetitionStartInMs
	public double[] getTimeOfRepetitionStartInMs() { return timeOfRepetitionStartInMs; }
	public void setTimeOfRepetitionStartInMs(double[] timeOfRepetitionStartInMs) { this.timeOfRepetitionStartInMs = timeOfRepetitionStartInMs; }
	
	// Generated design
	public float[][] getRegressorValues() { return regressorValues;	}
	
	// buffersForwardIn
	public double[][] getBuffersForwardIn() { return buffersForwardIn; }
	public void setBuffersForwardIn(double[][] buffersForwardIn) { this.buffersForwardIn = buffersForwardIn; }
	// buffersForwardOut
	public Complex[][] getBuffersForwardOut() { return buffersForwardOut; }
	public void setBuffersForwardOut(Complex[][] buffersForwardOut) { this.buffersForwardOut = buffersForwardOut; }
	// buffersInverseIn
	public Complex[][] getBuffersInverseIn() { return buffersInverseIn; }
	public void setBuffersInverseIn(Complex[][] buffersInverseIn) { this.buffersInverseIn = buffersInverseIn; }
	// buffersInverseOut
	public double[][] getBuffersInverseOut() { return buffersInverseOut; }
	public void setBuffersInverseOut(double[][] buffersInverseOut) { this.buffersInverseOut = buffersInverseOut; }
	
	// fftPlanFoward
	public FourierTransform[] getFftPlanForward() { return this.fftPlanForward; }
	public void setFftPlanForward(FourierTransform[] fftPlanForward) { this.fftPlanForward = fftPlanForward; }
	// fftPlanInverse
	public FourierTransform[] getFftPlanInverse() { return this.fftPlanInverse; }
	public void setFftPlanInverse(FourierTransform[] fftPlanInverse) { this.fftPlanInverse = fftPlanInverse; }
	
}
