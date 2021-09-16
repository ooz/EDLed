package de.mpg.cbs.edledplugin.design;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mpg.cbs.edled.xml.XMLUtility;
import de.mpg.cbs.edledplugin.design.bart.DesignElement;
import de.mpg.cbs.edledplugin.design.bart.DoubleGammaKernel;
import de.mpg.cbs.edledplugin.design.bart.GloverKernel;
import de.mpg.cbs.edledplugin.design.bart.DesignKernel.DesignKernelTimeUnit;
import de.mpg.cbs.edledplugin.design.bart.DoubleGammaKernel.GammaParams;
import de.mpg.cbs.edledplugin.design.bart.GloverKernel.GloverParams;

public class KernelFormatter {
	
	/**
	 * 
	 * @param referenceFunctionsNode
	 * @return Map. Key: function ID, value: function kernel.
	 */
	public Map<String, DoubleGammaKernel> createGammaKernels(final Node referenceFunctionsNode,
			 												 final long numberSamplesForInit) {
		Map<String, DoubleGammaKernel> kernels = new HashMap<String, DoubleGammaKernel>();
		NodeList gammaKernelNodes = ((Element) referenceFunctionsNode).getElementsByTagName("dGamma");
		
		for (int refNr = 0; refNr < gammaKernelNodes.getLength(); refNr++) {
			Element gammaKernelElem = (Element) gammaKernelNodes.item(refNr);
			String refFctID = gammaKernelElem.getAttribute("refFctID");
			if (!refFctID.equals("")) {
				double tPeak1 = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("tPeak1").item(0)));
				double mWidth1 = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("mWidth1").item(0)));
				double scale1 = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("scale1").item(0)));
				double tPeak2 = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("tPeak2").item(0)));
				double mWidth2 = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("mWidth2").item(0)));
				double scale2 = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("scale2").item(0)));
				double offset = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("offset").item(0)));
				double overallWidth = Double.parseDouble(XMLUtility.getNodeValue(gammaKernelElem.getElementsByTagName("overallWidth").item(0)));
				
				GammaParams params = new GammaParams(tPeak1, 
													 mWidth1, 
													 scale1, 
													 tPeak2, 
													 mWidth2, 
													 scale2, 
													 offset, 
													 overallWidth,
													 DesignKernelTimeUnit.KERNEL_TIME_MS);
				
				kernels.put(refFctID, new DoubleGammaKernel(refFctID, 
														    params,
														    numberSamplesForInit,
														    DesignElement.SAMPLING_RATE_IN_MS));
			}
		}
		
		return kernels;
	}
	
	/**
	 * 
	 * @param referenceFunctionsNode
	 * @param numberSamplesForInit
	 * @return Map. Key: function ID, value: function kernel.
	 */
	public Map<String, GloverKernel> createGloverKernels(final Node referenceFunctionsNode,
														 final long numberSamplesForInit) {
		Map<String, GloverKernel> kernels = new LinkedHashMap<String, GloverKernel>();
		NodeList gloverKernelNodes = ((Element) referenceFunctionsNode).getElementsByTagName("gloverKernel");
		
		for (int refNr = 0; refNr < gloverKernelNodes.getLength(); refNr++) {
			Element gloverKernelElem = (Element) gloverKernelNodes.item(refNr);
			String refFctID = gloverKernelElem.getAttribute("refFctID");
			if (!refFctID.equals("")) {
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
				
				kernels.put(refFctID, new GloverKernel(refFctID,
													   params, 
													   numberSamplesForInit, 
													   DesignElement.SAMPLING_RATE_IN_MS));
			}
		}
		
		return kernels;
	}
	
}
