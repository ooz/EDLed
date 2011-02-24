package design.bart;

import java.util.LinkedList;
import java.util.List;

import flanagan.complex.Complex;

public class DesignElement {
	
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
	private long numberSamplesNeededForExp = 0; // unsigned long
	private double[] timeOfRepetitionStartInMs;
	/** Generated/resulting design */
	private float[][] regressorValues;
	private float[][] covariateValues;
	/** FFT buffers */
	private double[][] buffersForwardIn;  // one per each event
	private double[][] buffersInverseOut; // one per each event
	private Complex[][] buffersForwardOut; // resulting HRFs (one per event)
	private Complex[][] buffersInverseIn;
	/** Plans for FFT */
//	private fftw_plan fftPlanForward;
//	private fftw_plan fftPlanInverse;
	
	/* ===== Constructors ===== */
	
	
	/* ===== Methods ===== */
	private void convolve(final int col, 
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
	
}
