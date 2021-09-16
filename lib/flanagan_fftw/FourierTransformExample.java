/*
*   FourierTransformExample
*
*   This class demonstrates a use of the FourierTransform class
*   A 256 point wave form formed by adding two sine waves
*       y = sin(2.pi.t) + 2sin(10.pi.t);
*   is first plotted, then its power spectrum is obtained
*   and displayed.  The transformed data is then put
*   through a inverse Fourier transform and the inverse
*   transformed data, which should be the same as the
*   original wave form, is then displayed.
*
*   AUTHOR: Dr Michael Thomas Flanagan
*   DATE:   24 January 2006
*
*   Copyright (c) January 2006  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

import flanagan.io.*;
import flanagan.math.*;
import flanagan.plot.*;

public class FourierTransformExample{

    public static void main(String[] args){

        int nPoints = 256;  // number of points
        double[] tdata = new double[nPoints];
        double[] ydata = new double[nPoints];

        double amplitude1 = 1.0D;
        double amplitude2 = 2.0D;
        double pointsPerCycle = 200;
        double deltaT = 1.0D/pointsPerCycle;

        // Create wave form
        for(int i=0; i<nPoints; i++){
            ydata[i]= amplitude1*Math.sin(2.0D*Math.PI*i/pointsPerCycle)+ amplitude2*Math.sin(10.0D*Math.PI*i/pointsPerCycle);
            tdata[i]=i*deltaT;
        }

        // Plot original data
        PlotGraph pg0 = new PlotGraph(tdata, ydata);
        pg0.setGraphTitle("y = sin(2.pi.t) + 2sin(10.pi.t)");
        pg0.setXaxisLegend("time");
        pg0.setXaxisUnitsName("s");
        pg0.setXaxisLegend("y");
        pg0.plot();

        // Obtain Power spectrum
        FourierTransform ft0 = new FourierTransform(ydata);
        ft0.setDeltaT(deltaT);
        double[][] powerSpectrum = ft0.powerSpectrum();

        // Plot power spectrum
        ft0.plotPowerSpectrum();

        // Obtain the transformed data
        double[] transformedData = ft0.getTransformedDataAsAlternate();

        // Inverse transform the transformed data
        FourierTransform ft1 = new FourierTransform();
        ft1.setFftData(transformedData);
        ft1.inverse();

        // Obtain the inverse transformed data
        double[] inverseTransform = ft1.getTransformedDataAsAlternate();

        // Arrange real parts for plotting
        double[] newYdata = new double[nPoints];
        int k=0;
        for(int i=0; i<nPoints; i++){
            newYdata[i] = inverseTransform[k];
            k += 2;
        }

        // Plot inverse transformed data
        PlotGraph pg1 = new PlotGraph(tdata, newYdata);
        pg1.setGraphTitle("y = sin(2.pi.t) + 2sin(10.pi.t) fft transformed and then inverse transformed");
        pg1.setXaxisLegend("time");
        pg1.setXaxisUnitsName("s");
        pg1.setXaxisLegend("y");
        pg1.plot();




    }


}
