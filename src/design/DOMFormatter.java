package design;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import design.bart.DesignElement;
import design.bart.DesignGloverKernel;
import design.bart.DesignElement.Regressor;
import design.bart.DesignElement.Trial;
import design.bart.DesignKernel.DesignKernelTimeUnit;
import design.bart.DesignKernel.GloverParams;
import edled.xml.XMLUtility;
import flanagan.complex.Complex;
import flanagan.math.FourierTransform;

public class DOMFormatter {
	
	private static final Logger logger = Logger.getLogger(DOMFormatter.class);
	
	private static final float ONSET_DEFAULT = 0.0f;
	private static final float DURATION_DEFAULT = 0.0f;
	private static final float HEIGHT_DEFAULT = 1.0f;

	public boolean fill(final DesignElement design, 
						final Node paradigmNode,
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
			return false;
		}
		
		// TODO clear/reset design
		// design.clear()
		logger.debug("DesignElement not yet cleared!");
		
		Element paradigmElem = (Element) paradigmNode;
		Element designElem = (Element) paradigmElem.getElementsByTagName("*").item(0);
		
		if (designElem == null) {
			// No design struct specified
			return false;
		}
		if (!designElem.getNodeName().equals("gwDesignStruct")
			&& !designElem.getNodeName().equals("swDesignStruct")
			&& !designElem.getNodeName().equals("dynamicDesignStruct")) {
			// Design type not supported
			return false;
		}
		
		// TR
		long tr = Long.parseLong(XMLUtility.getNodeValue(trNode));
		if (tr < 0) {
			return false;
		} else {
			design.setRepetitionTimeInMs(tr);
		}
		
		// NumberTimesteps
		int numberTimesteps = Integer.parseInt(XMLUtility.getNodeValue(measurementsNode));
		if (numberTimesteps < 0) {
			return false;
		} else {
			design.setNumberTimesteps(numberTimesteps);
		}
		
		// NumberCovariates
		Element covariateStructElem = (Element) paradigmElem.getElementsByTagName("covariateStruct").item(0);
		if (covariateStructElem != null) {
			long numberCovariates = covariateStructElem.getElementsByTagName("covariate").getLength();
			design.setNumberCovariates(numberCovariates);
		}
		
		// NumberEvents
		NodeList regressorNodes = designElem.getElementsByTagName("timeBasedRegressor");
		design.setNumberEvents(regressorNodes.getLength());
		
		// NumberSamples for FFT; add some seconds to avoid wrap around problems with fft (10 seconds)
		long numberSamplesForInit = (long) ((numberTimesteps * design.getRepetitionTimeInMs()) / DesignElement.SAMPLING_RATE_IN_MS + 10000); // 13000
		design.setNumberSamplesForInit(numberSamplesForInit);
		
		// Fetch reference functions (gGamma/gloverKernel)
		NodeList gloverKernelNodes = ((Element) refFctsNode).getElementsByTagName("gloverKernel");
		NodeList gammaKernelNodes = ((Element) refFctsNode).getElementsByTagName("dGamma");
//		int nrRefFcts = gloverKernelNodes.getLength() + gammaKernelNodes.getLength();
		
		List<Regressor> regressorList = new LinkedList<Regressor>();
		// Build all trials (statEvent) for each event (timeBasedRegressor)
		int nrDerivs = 0; // Count number of derivations per event = number of cols needed at the end
		for (int eventNr = 0; eventNr < design.getNumberEvents(); eventNr++) {
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
			logger.debug("Skipped timeUnit check.");
			
			for (int refNr = 0; refNr < gloverKernelNodes.getLength(); refNr++) {
				Element gloverKernelElem = (Element) gloverKernelNodes.item(refNr);
				String refFctID = gloverKernelElem.getAttribute("refFctID");
				if (!refFctID.equals("") && hrfKernelName.equals(refFctID)) {
					int overallWidth = Integer.parseInt(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("overallWidth").item(0)));
					double peak1 = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("tPeak1").item(0)));
					double scale1 = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("tPeak1Scale").item(0)));
					double peak2 = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("tPeak2").item(0)));
					double scale2 = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("tPeak2Scale").item(0)));
					double offset = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("offset").item(0)));
					double ratioTPeaks = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("ratioTPeaks").item(0)));
					double heightScale = Double.parseDouble(XMLUtility.getNodeValue(gloverKernelElem.getElementsByTagName("heightScale").item(0)));
					
					GloverParams params = new GloverParams(overallWidth, 
														   peak1, 
														   scale1, 
														   peak2, 
														   scale2, 
														   offset, 
														   ratioTPeaks, 
														   heightScale, 
														   DesignKernelTimeUnit.KERNEL_TIME_MS);
					regressor.regConvolKernel = new DesignGloverKernel(params, 
																	   design.getNumberSamplesForInit(), 
																	   DesignElement.SAMPLING_RATE_IN_MS);
				}
			} 
			for (int refNr = 0; refNr < gammaKernelNodes.getLength(); refNr++) {
				Element gammaKernelElem = (Element) gammaKernelNodes.item(refNr);
				String refFctID = gammaKernelElem.getAttribute("refFctID");
				if (!refFctID.equals("") && hrfKernelName.equals(refFctID)) {
					// TODO
					logger.warn("Tried to use a gamma kernel. Feature is not yet implemented!");
				}
			}
			
			regressorList.add(regressor);
		}
		
		design.setRegressorList(regressorList);
		design.setNumberRegressors(design.getNumberEvents() + nrDerivs + 1);
		design.setNumberExplanatoryVariables(design.getNumberRegressors() + design.getNumberCovariates());
		
		// initDesign:
		boolean zeromean = true;
		
		double[] timeOfRepetitionStartInMs = new double[design.getNumberTimesteps()];
		for (int i = 0; i < design.getNumberTimesteps(); i++) {
			timeOfRepetitionStartInMs[i] = (double) (i) * design.getRepetitionTimeInMs();//TODO: Gabi fragen letzter Zeitschritt im moment nicht einbezogen xx[i] = (double) i * tr * 1000.0;
		}
		design.setTimeOfRepetitionStartInMs(timeOfRepetitionStartInMs);

//	    long maxExpLengthInMs = (long) (timeOfRepetitionStartInMs[0] + timeOfRepetitionStartInMs[design.getNumberTimesteps() - 1] + design.getRepetitionTimeInMs());//+1 repetition to add time of last rep
	     /*
	     ** check amplitude: must have zero mean for parametric designs
		 ** for not parametric nothing will be corrected due to check of stddev
	     */
	    if (zeromean == true) { 
			design.correctForZeromean();
		}
	    if (design.getNumberEvents() < 1) {
	    	logger.error("No events were found in the experiment design!");
	    }

		/* alloc memory for all NEDesignDyn specific stuff*/
		design.initRegressorValues();
	   
		if (design.getNumberCovariates() > 0) {
	        design.initCovariateValues();
		}
	        
	    int numberSamplesInResult = (int) (design.getNumberSamplesForInit() / 2) + 1;//defined for results of fftw3

//	    /* make plans one per each event*/
//	    mFftPlanForward = (fftw_plan *) malloc(sizeof(fftw_plan) * mNumberEvents);
//	    mFftPlanInverse = (fftw_plan *) malloc(sizeof(fftw_plan) * mNumberEvents);
		
		/* alloc input/output buffers for forward/inverse fft one per each event*/
		double[][] buffersForwardIn = new double[design.getNumberEvents()][(int) design.getNumberSamplesForInit()];
	    Complex[][] buffersForwardOut = new Complex[design.getNumberEvents()][numberSamplesInResult];
	    Complex[][] buffersInverseIn = new Complex[design.getNumberEvents()][numberSamplesInResult];
	    double[][] buffersInverseOut = new double[design.getNumberEvents()][(int) design.getNumberSamplesForInit()];

	    FourierTransform[] fftPlanForward = new FourierTransform[design.getNumberEvents()];
	    FourierTransform[] fftPlanInverse = new FourierTransform[design.getNumberEvents()];
	    
		/* alloc gamma kernels one per each event*/
	    for (int eventNr = 0; eventNr < design.getNumberEvents(); eventNr++) {
	    	for (int sampleNr = 0; sampleNr < design.getNumberSamplesForInit(); sampleNr++) {
	    		buffersForwardIn[eventNr][sampleNr] = 0.0;
	    		buffersInverseOut[eventNr][sampleNr] = 0.0;
	    	}
	    }
	    design.setBuffersForwardIn(buffersForwardIn);
	    design.setBuffersForwardOut(buffersForwardOut);
	    design.setBuffersInverseIn(buffersInverseIn);
	    design.setBuffersInverseOut(buffersInverseOut);
	    design.setFftPlanForward(fftPlanForward);
	    design.setFftPlanInverse(fftPlanInverse);
	    
		// TODO generateDesign:
	    for (int eventNr = 0; eventNr < design.getNumberEvents(); eventNr++) {   
	        /* get data */
	        int trialcount = 0;
	        double t0;
	        double h;
			    
	        Regressor reg = design.getRegressorList().get(eventNr);
	        List<Trial> trials = reg.regTrialList;
	        for (Trial trial : trials) {
	        	trialcount++;
	        	
	        	t0 = trial.onset;
	        	double tmax = t0 + trial.duration;
	        	h = trial.height;
	        	int k = (int) (t0 / DesignElement.SAMPLING_RATE_IN_MS);
	        	
	        	for (double t = t0; t <= tmax; t += DesignElement.SAMPLING_RATE_IN_MS) {
	                if (k >= design.getNumberSamplesForInit()) {
	                    break;
	                }
	                design.getBuffersForwardIn()[eventNr][k++] += h;
	            }
	        }
	        
	        /* Removed trialcount checks */
	        
	        /* fft */
	        fftPlanForward[eventNr] = new FourierTransform(DesignElement.padToNextPowerOfTwo(buffersForwardIn[eventNr])); //= fftw_plan_dft_r2c_1d(mNumberSamplesForInit, mBuffersForwardIn[eventNr], mBuffersForwardOut[eventNr], FFTW_ESTIMATE);
	        FourierTransform plan = design.getFftPlanForward()[eventNr];
	        plan.transform();
	        design.getBuffersForwardOut()[eventNr] = plan.getTransformedDataAsComplex();
			// the actual column is added from all events and their derivations before
			int columnsForDerivs = 0;
			for (int countCols = 0; countCols < eventNr; countCols++) {
				columnsForDerivs += design.getRegressorList().get(countCols).regDerivations;
			}
			
			int col = eventNr + columnsForDerivs;
			design.convolve(col, eventNr, reg.regConvolKernel.kernelDeriv0);
	        col++;
	        if (1 <= reg.regDerivations) {
	            design.convolve(col, eventNr, reg.regConvolKernel.kernelDeriv1);
	            col++;
	        }
	        
	        if (2 == reg.regDerivations) {
	            design.convolve(col, eventNr, reg.regConvolKernel.kernelDeriv2);
	        }
	    }
		
		return true;
	}
	
}
