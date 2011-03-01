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
		public long id = 0; // Stimulus number: unsigned int
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
			return "Trial[id="+id+",onset="+onset+",duration="+duration+",height="+height+"]";
		}
	}
	
	/* From NEDesignElementDyn */
	public static class Regressor {
		public List<Trial> regTrialList = new LinkedList<Trial>();
		public long regDerivations = 0; // unsigned int
		public String regID = null;
		public String regDescription = null;
		public DesignKernel regConvolKernel = null;
	}
	
	/* ===== Constants ===== */
	public static final double SAMPLING_RATE_IN_MS = 20.0; /* Temporal resolution for convolution is 20 ms. */
	
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
		// TODO: translate!!!
		this.fftPlanInverse[eventNr] = new FourierTransform(buffersInverseIn[eventNr]); //= fftw_plan_dft_c2r_1d(mNumberSamplesForInit, mBuffersInverseIn[eventNr], mBuffersInverseOut[eventNr], FFTW_ESTIMATE);
		this.fftPlanInverse[eventNr].transform();
		this.buffersInverseOut[eventNr] = this.fftPlanInverse[eventNr].getTransformedDataAsAlternate();
//		fftw_execute(this.fftPlanInverse[eventNr]);
		
		// Scaling
		// TODO: translate (buffersInverseOut is written in the above line (fftw_execute)!)
		for (int j = 0; j < this.numberSamplesForInit; j++) {
			this.buffersInverseOut[eventNr][j] /= (double) this.numberSamplesForInit;
		}
		
		// Sampling
		for (int timestep = 0; timestep < this.numberTimesteps; timestep++) {
			int j = (int) (this.timeOfRepetitionStartInMs[timestep] / SAMPLING_RATE_IN_MS);
			
			if (j >= 0 && j < this.numberSamplesForInit) {
				this.regressorValues[col][timestep] = (float) this.buffersInverseOut[eventNr][j];
			}
		}
	}
	
	private Complex multiplComplex(final Complex a, 
								   final Complex b) {
		return new Complex(a.getReal() * b.getReal() - a.getImag() * b.getImag(),
						   a.getReal() * b.getImag() + a.getImag() * b.getReal());
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
