package de.mpg.cbs.edledplugin.design.bart;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.xml.XMLUtility;
import de.mpg.cbs.edledplugin.design.KernelFormatter;
import flanagan.complex.Complex;
import flanagan.math.FourierTransform;

/**
 * DesignElement from BART.
 * 
 * @author Lydia Hellrung (original)
 * @author Oliver Z. (port)
 */
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
	/**  */
	private static final Logger LOGGER = Logger.getLogger(DesignElement.class);
	
	/** Temporal resolution for convolution is 20 ms. */
	public static final double SAMPLING_RATE_IN_MS = 20.0; 
	/** Add some seconds to avoid wrap around problems with FFT. */
	public static final int    WRAP_AROUND_PADDING_IN_MS = 10000;
	
	private static final float ONSET_DEFAULT = 0.0f;
	private static final float DURATION_DEFAULT = 0.0f;
	private static final float HEIGHT_DEFAULT = 1.0f;
	
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
	
	/** Reference functions mapping from function ID to kernel object. */
	Map<String, DoubleGammaKernel> gammaKernels;
	Map<String, GloverKernel> gloverKernels;

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
	public DesignElement(final Node paradigmNode,
						 final Node trNode,
						 final Node measurementsNode,
						 final Node refFctsNode) {
		if (paradigmNode == null
			|| paradigmNode.getNodeType() != Node.ELEMENT_NODE
			|| trNode == null
			|| trNode.getNodeType() != Node.ELEMENT_NODE
			|| measurementsNode == null
			|| measurementsNode.getNodeType() != Node.ELEMENT_NODE
			|| refFctsNode == null
			|| refFctsNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException("Passed nodes are null or no element nodes.");
		}
		
		Element paradigmElem = (Element) paradigmNode;
		Element designElem = (Element) paradigmElem.getElementsByTagName("*").item(0);
		
		if (designElem == null) {
			throw new IllegalArgumentException("Tried to create DesignElement from node with no design struct specified.");
		}
		if (!designElem.getNodeName().equals("gwDesignStruct")
			&& !designElem.getNodeName().equals("swDesignStruct")
			&& !designElem.getNodeName().equals("dynamicDesignStruct")) {
			throw new IllegalArgumentException("Tried to create DesignElement from node with not yet supported design type.");
		}
		
		// TR
		this.repetitionTimeInMs = Long.parseLong(XMLUtility.getNodeValue(trNode));
		if (this.repetitionTimeInMs < 0) {
			throw new IllegalArgumentException("Tried to create DesignElement with negative repetition time (TR).");
		}
		
		// NumberTimesteps
		this.numberTimesteps = Integer.parseInt(XMLUtility.getNodeValue(measurementsNode));
		if (this.numberTimesteps < 0) {
			throw new IllegalArgumentException("Tried to create DesignElement with negative timestep count.");
		}
		
		// NumberCovariates
		Element covariateStructElem = (Element) paradigmElem.getElementsByTagName("covariateStruct").item(0);
		if (covariateStructElem != null) {
			long numberCovariates = covariateStructElem.getElementsByTagName("covariate").getLength();
			this.numberCovariates = numberCovariates;
		}
		
		// NumberEvents
		NodeList regressorNodes = designElem.getElementsByTagName("timeBasedRegressor");
		this.numberEvents = regressorNodes.getLength();
		
		// NumberSamples for FFT; add some seconds to avoid wrap around problems with fft (10 seconds)
		long numberSamplesForInit = (long) ((numberTimesteps * this.getRepetitionTimeInMs()) / DesignElement.SAMPLING_RATE_IN_MS 
											+ DesignElement.WRAP_AROUND_PADDING_IN_MS);
		this.numberSamplesForInit = numberSamplesForInit;
		
		// Fetch reference functions (gGamma/gloverKernel)
		KernelFormatter kernelFormatter = new KernelFormatter();
		this.gammaKernels = kernelFormatter.createGammaKernels(refFctsNode,
															   this.getNumberSamplesForInit());
		this.gloverKernels = kernelFormatter.createGloverKernels(refFctsNode, 
															     this.getNumberSamplesForInit());
		
//		int nrRefFcts = gloverKernelNodes.getLength() + gammaKernelNodes.getLength();
		
		List<Regressor> regressorList = new LinkedList<Regressor>();
		// Build all trials (statEvent) for each event (timeBasedRegressor)
		int nrDerivs = 0; // Count number of derivations per event = number of cols needed at the end
		for (int eventNr = 0; eventNr < this.getNumberEvents(); eventNr++) {
			Element regressorElem = (Element) regressorNodes.item(eventNr);
			NodeList statEventNodes = ((Element) regressorElem.getElementsByTagName("tbrDesign").item(0)).getElementsByTagName("statEvent");
			
			List<Trial> trials = new LinkedList<Trial>();
			for (int trialNr = 0; trialNr < statEventNodes.getLength(); trialNr++) {
				Element statEventElem = (Element) statEventNodes.item(trialNr);
				
				String timeAttrValue = statEventElem.getAttribute("time");
				float onset = timeAttrValue.equals("") ? ONSET_DEFAULT : Float.parseFloat(timeAttrValue);
				String durationAttrValue = statEventElem.getAttribute("duration");
				float duration = durationAttrValue.equals("") ? DURATION_DEFAULT : Float.parseFloat(durationAttrValue);
				String heightAttrValue = statEventElem.getAttribute("parametricScaleFactor");
				float height = heightAttrValue.equals("") ? HEIGHT_DEFAULT : Float.parseFloat(heightAttrValue);
				
				if (duration < 0.0f) { 
					duration = 1.0f; 
				}
				if (height < 0.0f) {
					height = 1.0f;
				}
				trials.add(new Trial((long) eventNr + 1, onset, duration, height));
			}
			
			Regressor regressor = new Regressor();
			regressor.regTrialList = trials;
			regressor.regID = regressorElem.getAttribute("regressorID");
			regressor.regDescription = regressorElem.getAttribute("name");
			
			// Number of derivations used per each event
			if (Boolean.parseBoolean(regressorElem.getAttribute("useRefFctSecondDerivative"))) {
				regressor.regDerivations = 2;
				nrDerivs += 2;
			} else if (Boolean.parseBoolean(regressorElem.getAttribute("useRefFctFirstDerivative"))) {
				regressor.regDerivations = 1;
				nrDerivs += 1;
			} else {
				regressor.regDerivations = 0;
			}
			
			String hrfKernelName = regressorElem.getAttribute("useRefFct");
			
			// TODO skipped timeUnit check!
			LOGGER.debug("DesignElement creation: Skipped timeUnit check.");
			if (gammaKernels.get(hrfKernelName) != null) {
				regressor.regConvolKernel = this.gammaKernels.get(hrfKernelName);
			} else {
				regressor.regConvolKernel = this.gloverKernels.get(hrfKernelName);
			}
			
			regressorList.add(regressor);
		}
		
		this.regressorList = regressorList;
		this.numberRegressors = this.getNumberEvents() + nrDerivs + 1;
		this.numberExplanatoryVariables = this.getNumberRegressors() + this.getNumberCovariates();
		
		// initDesign:
		boolean zeromean = true;
		
		double[] timeOfRepetitionStartInMs = new double[this.getNumberTimesteps()];
		for (int i = 0; i < this.getNumberTimesteps(); i++) {
			// TODO: Gabi fragen letzter Zeitschritt im moment nicht einbezogen xx[i] = (double) i * tr * 1000.0;
			timeOfRepetitionStartInMs[i] = (double) (i) * this.getRepetitionTimeInMs();
		}
		this.timeOfRepetitionStartInMs = timeOfRepetitionStartInMs;

//	    long maxExpLengthInMs = (long) (timeOfRepetitionStartInMs[0] + timeOfRepetitionStartInMs[design.getNumberTimesteps() - 1] + design.getRepetitionTimeInMs());//+1 repetition to add time of last rep
	     /*
	     ** check amplitude: must have zero mean for parametric designs
		 ** for not parametric nothing will be corrected due to check of stddev
	     */
	    if (zeromean == true) { 
			this.correctForZeromean();
		}
	    if (this.getNumberEvents() < 1) {
	    	LOGGER.error("No events were found in the experiment design!");
	    }

		/* alloc memory for all NEDesignDyn specific stuff*/
		this.initRegressorValues();
	   
		if (this.getNumberCovariates() > 0) {
	        this.initCovariateValues();
		}
	        
	    int numberSamplesInResult = (int) (this.getNumberSamplesForInit() / 2) + 1;//defined for results of fftw3

	    /* make plans one per each event*/
		/* alloc input/output buffers for forward/inverse fft one per each event*/
		double[][] buffersForwardIn = new double[this.getNumberEvents()][(int) this.getNumberSamplesForInit()];
	    Complex[][] buffersForwardOut = new Complex[this.getNumberEvents()][numberSamplesInResult];
	    Complex[][] buffersInverseIn = new Complex[this.getNumberEvents()][numberSamplesInResult];
	    double[][] buffersInverseOut = new double[this.getNumberEvents()][(int) this.getNumberSamplesForInit()];

	    FourierTransform[] fftPlanForward = new FourierTransform[this.getNumberEvents()];
	    FourierTransform[] fftPlanInverse = new FourierTransform[this.getNumberEvents()];
	    
		/* alloc gamma kernels one per each event*/
	    for (int eventNr = 0; eventNr < this.getNumberEvents(); eventNr++) {
	    	for (int sampleNr = 0; sampleNr < this.getNumberSamplesForInit(); sampleNr++) {
	    		buffersForwardIn[eventNr][sampleNr] = 0.0;
	    		buffersInverseOut[eventNr][sampleNr] = 0.0;
	    	}
	    }
	    this.buffersForwardIn = buffersForwardIn;
	    this.buffersForwardOut = buffersForwardOut;
	    this.buffersInverseIn  = buffersInverseIn;
	    this.buffersInverseOut = buffersInverseOut;
	    this.fftPlanForward = fftPlanForward;
	    this.fftPlanInverse = fftPlanInverse;
	    
	    for (int eventNr = 0; eventNr < this.getNumberEvents(); eventNr++) {   
	        /* get data */
//	        int trialcount = 0;
	        double t0;
	        double h;
			    
	        Regressor reg = this.getRegressorList().get(eventNr);
	        List<Trial> trials = reg.regTrialList;
	        for (Trial trial : trials) {
//	        	trialcount++;
	        	
	        	t0 = trial.onset;
	        	double tmax = t0 + trial.duration;
	        	h = trial.height;
	        	int k = (int) (t0 / DesignElement.SAMPLING_RATE_IN_MS);
	        	
	        	for (double t = t0; t <= tmax; t += DesignElement.SAMPLING_RATE_IN_MS) {
	                if (k >= this.getNumberSamplesForInit()) {
	                    break;
	                }
	                this.buffersForwardIn[eventNr][k++] += h;
	            }
	        }
	        
	        /* TODO: Removed trialcount checks */
	        
	        /* fft */
	        fftPlanForward[eventNr] = new FourierTransform(DesignElement.padToNextPowerOfTwo(buffersForwardIn[eventNr])); //= fftw_plan_dft_r2c_1d(mNumberSamplesForInit, mBuffersForwardIn[eventNr], mBuffersForwardOut[eventNr], FFTW_ESTIMATE);
	        FourierTransform plan = this.getFftPlanForward()[eventNr];
	        plan.transform();
	        this.buffersForwardOut[eventNr] = plan.getTransformedDataAsComplex();
			// the actual column is added from all events and their derivations before
			int columnsForDerivs = 0;
			for (int countCols = 0; countCols < eventNr; countCols++) {
				columnsForDerivs += this.getRegressorList().get(countCols).regDerivations;
			}
			
			int col = eventNr + columnsForDerivs;
			this.convolve(col, eventNr, reg.regConvolKernel.kernelDeriv0);
	        col++;
	        if (1 <= reg.regDerivations) {
	            this.convolve(col, eventNr, reg.regConvolKernel.kernelDeriv1);
	            col++;
	        }
	        
	        if (2 == reg.regDerivations) {
	            this.convolve(col, eventNr, reg.regConvolKernel.kernelDeriv2);
	        }
	    }
	    
		this.notifyObservers();
	}
	
	
	
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
		LOGGER.trace("(Complex) Data length: " + data.length + " Padded length: " + FourierTransform.nextPowerOfTwo(data.length));
		LOGGER.trace("    Padded " + (FourierTransform.nextPowerOfTwo(data.length) - data.length) + " elements");
		
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
			throw new IllegalArgumentException("Called DesignElement.convolve with kernel=null!");
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
		
		int padding = DesignElement.WRAP_AROUND_PADDING_IN_MS - 2;
		
		int transformedTSCount = (int) (this.numberTimesteps * (this.repetitionTimeInMs / SAMPLING_RATE_IN_MS)) + padding;
		// Sampling
		for (int timestep = 0; timestep < this.numberTimesteps; timestep++) {
			int j = (int) (this.timeOfRepetitionStartInMs[timestep] / SAMPLING_RATE_IN_MS);

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

		float colAmagnitude = computeColumnMagnitude(colA);
		float colBmagnitude = computeColumnMagnitude(colB);
		
		float orthogonality = 0.0f;
		for (int ts = 0; ts < this.numberTimesteps; ts++) {
			orthogonality +=   Math.abs(this.regressorValues[colA /* * colsPerReg */][ts] / colAmagnitude) 
			                 * Math.abs(this.regressorValues[colB /* * colsPerReg */][ts] / colBmagnitude);
		}
		
//		float magnitudeProduct = computeColumnMagnitude(colA) * computeColumnMagnitude(colB);
		
		return orthogonality;
		//return (magnitudeProduct == 0.0f) ? 0.0f : (orthogonality / magnitudeProduct);
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
	private float computeColumnMagnitude(final int colNr) {
		float magnitude = 0.0f;
		
		for (int ts = 0; ts < this.numberTimesteps; ts++) {
			float value = this.regressorValues[colNr][ts];
			magnitude += value * value; 
		}
		
		return (float) Math.sqrt(magnitude);
	}
	
	private void correctForZeromean() {
		for (int i = 0; i < this.numberEvents; i++) {
			float sum1 = 0.0f;
			float sum2 = 0.0f;
			
			List<Trial> trials = this.regressorList.get(i).regTrialList;
			for (Trial trial : trials) {
				sum1 += trial.height;
				sum2 += trial.height * trial.height;
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
	
	/* Getters. */
	public long getRepetitionTimeInMs() { return repetitionTimeInMs; }
	public long getNumberExplanatoryVariables() { return numberExplanatoryVariables; }
	public int getNumberTimesteps() { return numberTimesteps; }
	public long getNumberRegressors() { return numberRegressors; }
	public long getNumberCovariates() { return numberCovariates; }
	public ImageDataType getImageDataType() { return imageDataType;	}
	
	public List<Regressor> getRegressorList() { return regressorList; }
	public int getNumberEvents() { return numberEvents; }
	public long getNumberSamplesForInit() { return numberSamplesForInit; }

	public double[] getTimeOfRepetitionStartInMs() { return timeOfRepetitionStartInMs; }
	
	/** Returns the generated design. */
	public float[][] getRegressorValues() { return regressorValues;	}
	
	public FourierTransform[] getFftPlanForward() { return this.fftPlanForward; }
	public FourierTransform[] getFftPlanInverse() { return this.fftPlanInverse; }
	
	public List<DesignKernel> getGammaKernels() {
		List<DesignKernel> kernels = new LinkedList<DesignKernel>();
		for (String kernelName : this.gammaKernels.keySet()) {
			kernels.add(this.gammaKernels.get(kernelName));
		}
		return kernels; 
	}
	public List<DesignKernel> getGloverKernels() {
		List<DesignKernel> kernels = new LinkedList<DesignKernel>();
		for (String kernelName : this.gloverKernels.keySet()) {
			kernels.add(this.gloverKernels.get(kernelName));
		}
		return kernels; 
	}
}
