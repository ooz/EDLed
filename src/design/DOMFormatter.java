package design;

import java.awt.image.SampleModel;
import java.util.LinkedList;
import java.util.List;

import junit.extensions.RepeatedTest;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import design.bart.DesignElement;
import design.bart.DesignElement.Regressor;
import design.bart.DesignElement.Trial;
import edled.xml.XMLUtility;

public class DOMFormatter {

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
		long numberSamplesForInit = (long) ((numberTimesteps * design.getRepetitionTimeInMs()) / DesignElement.SAMPLING_RATE_IN_MS + 10000);
		design.setNumberSamplesForInit(numberSamplesForInit);
		
		// Fetch reference functions (gGamma/gloverKernel)
		NodeList gloverKernelNodes = ((Element) refFctsNode).getElementsByTagName("gloverKernel");
		NodeList gammaKernelNodes = ((Element) refFctsNode).getElementsByTagName("dGamma");
		int nrRefFcts = gloverKernelNodes.getLength() + gammaKernelNodes.getLength();
		
		List<Regressor> regressorList = new LinkedList<Regressor>();
		// Build all trials (statEvent) for each event (timeBasedRegressor)
		int nrDerivs = 0; // Count number of derivations per event = number of cols needed at the end
		for (int eventNr = 0; eventNr < design.getNumberEvents(); eventNr++) {
			Element regressorElem = (Element) regressorNodes.item(eventNr);
			NodeList statEventNodes = ((Element) regressorElem.getElementsByTagName("tbrDesign").item(0)).getElementsByTagName("statEvent");
			
			List<Trial> trials = new LinkedList<Trial>();
			for (int trialNr = 0; trialNr < statEventNodes.getLength(); trialNr++) {
				Element statEventElem = (Element) statEventNodes.item(trialNr);
				float onset = Float.parseFloat(statEventElem.getAttribute("time"));
				float duration = Float.parseFloat(statEventElem.getAttribute("duration"));
				float height = Float.parseFloat(statEventElem.getAttribute("parametricScaleFactor"));
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
			
			for (int refNr = 0; refNr < gloverKernelNodes.getLength(); refNr++) {
				Element gloverKernelElem = (Element) gloverKernelNodes.item(refNr);
				String refFctID = gloverKernelElem.getAttribute("refFctID");
				if (!refFctID.equals("") && hrfKernelName.equals(refFctID)) {
					// TODO
				}
			} 
			for (int refNr = 0; refNr < gammaKernelNodes.getLength(); refNr++) {
				Element gammaKernelElem = (Element) gammaKernelNodes.item(refNr);
				String refFctID = gammaKernelElem.getAttribute("refFctID");
				if (!refFctID.equals("") && hrfKernelName.equals(refFctID)) {
					// TODO
				}
			}
			
			regressorList.add(regressor);
		}
		
		design.setRegressorList(regressorList);
		design.setNumberRegressors(design.getNumberEvents() + nrDerivs + 1);
		design.setNumberExplanatoryVariables(design.getNumberRegressors() + design.getNumberCovariates());
		
		return true;
	}
	
}
