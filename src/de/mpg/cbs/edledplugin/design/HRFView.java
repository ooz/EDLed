package de.mpg.cbs.edledplugin.design;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.mpg.cbs.edledplugin.design.bart.DesignElement;
import de.mpg.cbs.edledplugin.design.bart.DesignKernel;
import de.mpg.cbs.edledplugin.design.bart.GloverKernel;

public class HRFView extends JPanel implements ItemListener, DesignElementReceiver {
	
	/** */
	private static final long serialVersionUID = 224325403482033601L;
	
	/** Check all checkboxes for HRF functions and no checkbox for derivs (default setting). */
	private static final boolean DEFAULT_SHOW_HRFS = true;
	/** Number of derivs to plot for each kernel. */
	private static final int PLOT_DERIVS = 2;
	private static final String FIRST_DERIV_POSTFIX = " 1st deriv";
	private static final String SECOND_DERIV_POSTFIX = " 2nd deriv";
	
	private DesignElement design;
	
	private XYSeriesCollection hrfSeriesToShow = new XYSeriesCollection();
	/** Mapping series name to XYSeries. */
	private Map<String, XYSeries> allSeries;
	XYSeries hrfSeries;
	XYSeries deriv1Series;
	XYSeries deriv2Series;
	
	private JPanel checkBoxPanel;
	private List<JCheckBox> checkBoxes;
	private JFreeChart hrfChart;
	
	/** 
	 * Constructor. 
	 * 
	 * @param design DesignElement that contains the HRF data.
	 */
	public HRFView(final DesignElement design) {
        this.setLayout(new BorderLayout());

        this.hrfChart = createHRFChart();
    	final ChartPanel chartPanel = new ChartPanel(hrfChart);
        this.add(chartPanel, BorderLayout.CENTER);
        
        register(design);
	}
	
	private void addCheckBoxPanel() {
		this.checkBoxPanel = new JPanel();
		this.checkBoxPanel.setLayout(new BoxLayout(this.checkBoxPanel, BoxLayout.Y_AXIS));
		this.checkBoxes = new LinkedList<JCheckBox>();
		for (String seriesName : this.allSeries.keySet()) {
			JCheckBox cb = new JCheckBox(seriesName);
			cb.setSelected(DEFAULT_SHOW_HRFS);
//			cb.setMinimumSize(cb.getPreferredSize());
			cb.addItemListener(this);
			this.checkBoxes.add(cb);
			this.checkBoxPanel.add(cb);
		}
		
		this.add(this.checkBoxPanel, BorderLayout.LINE_END);
	}
	
	private void createSeriesData() {
//		DoubleGammaKernel doubleGamma = new DoubleGammaKernel("TestKernel",
//															  new DoubleGammaKernel.GammaParams(60000, false));
		this.allSeries = new LinkedHashMap<String, XYSeries>();
		List<DesignKernel> gammaKernels = new LinkedList<DesignKernel>();
		List<DesignKernel> gloverKernels = new LinkedList<DesignKernel>();
		if (this.design != null) {
			gammaKernels = this.design.getGammaKernels();
			gloverKernels = this.design.getGloverKernels();
		}
		fillSeriesDataWithKernels(gammaKernels);
		fillSeriesDataWithKernels(gloverKernels);
	}
	
	private void fillSeriesDataWithKernels(final List<DesignKernel> kernels) {
		for (DesignKernel kernel : kernels) {
			String kernelName = kernel.getID();
			float[][] fctData;
			// TODO: hack to fix scaling issues with glover kernels
			if (kernel instanceof GloverKernel) {
				fctData = ((GloverKernel) kernel).plotGammaWithDerivsHack(PLOT_DERIVS);
			} else {
				fctData = kernel.plotGammaWithDerivs(PLOT_DERIVS);
			}
			
			// TODO: similar code
			XYSeries fctSeries = createEmptySeries(kernelName);
			for (int i = 0; i < fctData.length; i++) {
				fctSeries.add(fctData[i][0], fctData[i][1]);
			}
			this.allSeries.put(kernelName, fctSeries);
			
			if (PLOT_DERIVS >= 1) {
				String fstDerivName = kernelName + FIRST_DERIV_POSTFIX;
				XYSeries fstDerivSeries = createEmptySeries(fstDerivName);
				for (int i = 0; i < fctData.length; i++) {
					fstDerivSeries.add(fctData[i][0], fctData[i][2]);
				}
				this.allSeries.put(fstDerivName, fstDerivSeries);
			}
			if (PLOT_DERIVS >= 2) {
				String sndDerivName = kernelName + SECOND_DERIV_POSTFIX;
				XYSeries sndDerivSeries = createEmptySeries(sndDerivName);
				for (int i = 0; i < fctData.length; i++) {
					sndDerivSeries.add(fctData[i][0], fctData[i][3]);
				}
				this.allSeries.put(sndDerivName, sndDerivSeries);
			}
		}
	}
	
	private XYSeries createEmptySeries(final String seriesName) {
		return new XYSeries(new Comparable<Object>() {
			@Override
			public int compareTo(Object o) { return 0; }
			@Override
			public String toString()       { return seriesName; }
		});
	}

	private JFreeChart createHRFChart() {
	    return ChartFactory.createXYLineChart(
	        "HRF",
	        "Time in seconds", 
	        "Height", 
	        hrfSeriesToShow,
	        PlotOrientation.VERTICAL,
	        true,
	        true,
	        false
	    );
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		this.hrfSeriesToShow.removeAllSeries();
		
		for (JCheckBox cb : this.checkBoxes) {
			if (cb.isSelected()) {
				this.hrfSeriesToShow.addSeries(this.allSeries.get(cb.getText()));
			}
		}
	}

	@Override
	public void register(DesignElement design) {
		this.design = design;
		
        createSeriesData();
        addCheckBoxPanel();
        itemStateChanged(null);
	}
}

