package design;

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

import design.bart.DesignElement;
import design.bart.DesignKernel;
import design.bart.DoubleGammaKernel;

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
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        this.add(chartPanel, BorderLayout.CENTER);
        register(design);
	}

//	@Override
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		
//		// Setting up graphics.
//		Graphics2D g2D = (Graphics2D) g;
////		g2D.setFont(this.captionFont);
//		g2D.setStroke(new BasicStroke(1.0f));
//		g2D.setFont(DesignViewConstants.CAPTION_FONT);
//		
////		FontMetrics fontMetrics = g2D.getFontMetrics();
//		
//		g2D.setBackground(Color.WHITE);
//		g2D.setColor(Color.WHITE);
//		g2D.fillRect(0, 0, getWidth(), getHeight());
//		
//		float fctMin = Float.MAX_VALUE;
//		float fctMax = Float.MIN_VALUE;
//		// find min/max value of function
//		for (int i = 0; i < this.gammaFunction.length; i++) {
//			if (this.gammaFunction[i][1] < fctMin) {
//				fctMin = this.gammaFunction[i][1];
//			} else if (this.gammaFunction[i][1] > fctMax) {
//				fctMax = this.gammaFunction[i][1];
//			}
//		}
//		
//		// Draw HRF
//		g2D.setPaint(Color.BLACK);
//		float height = 200.0f;
//		float valueCorrection;
//		if (fctMin < 0.0) {
//			valueCorrection = Math.abs(fctMin);
//			fctMax += valueCorrection;
//		} else {
//			valueCorrection = -fctMin;
//			fctMax -= valueCorrection;
//		}
//		for (int i = 0; i < this.gammaFunction.length; i++) {
//			float value = this.gammaFunction[i][1];
//			value += valueCorrection;
//			if (fctMax != 0.0f) {
//				value /= fctMax;
//			} else {
//				value = 0.0f;
//			}
//			
//			if (true) {
//				g2D.fillRect(i * 2, (int) (height - value * height), 2, 1);
//			}
//		}
//		
////		g2D.setColor(Color.WHITE);
////		g2D.setBackground(Color.WHITE);
////		
////		int regNr = 0;
////		for (Regressor reg : this.design.getRegressorList()) {
////			for (Trial trial : reg.regTrialList) {
////				g2D.fillRect(regNr * widthPerReg, 
////							 (int) ((trial.onset / 1000.0f) * heightPerSec), 
////							 widthPerReg, 
////							 (int) ((trial.duration / 1000.0f) * heightPerSec));
//////				System.out.println(trial);
////			}
////			regNr++;
////		}
//		
//		g2D.setPaint(Color.WHITE);
////		FontMetrics fontMetrics = g2D.getFontMetrics();
////		int fontHeight = fontMetrics.getHeight();
//		
//	}

//public PieChart(String applicationTitle, String chartTitle) {
//    super(applicationTitle);
//    // This will create the dataset 
//    PieDataset dataset = createDataset();
//    // based on the dataset we create the chart
//    JFreeChart chart = createChart(dataset, chartTitle);
//    // we put the chart into a panel
//    ChartPanel chartPanel = new ChartPanel(chart);
//    // default size
//    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//    // add it to our application
//    setContentPane(chartPanel);
//
//}
//
///**
// * Creates a sample dataset 
// */
//private  PieDataset createDataset() {
//    DefaultPieDataset result = new DefaultPieDataset();
//    result.setValue("Linux", 29);
//    result.setValue("Mac", 20);
//    result.setValue("Windows", 51);
//    return result;
//    
//}
//
	
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
		DoubleGammaKernel doubleGamma = new DoubleGammaKernel("TestKernel",
															  new DoubleGammaKernel.GammaParams(60000, false));
		this.allSeries = new LinkedHashMap<String, XYSeries>();
		fillSeriesDataWithKernels(this.design.getGammaKernels());
		fillSeriesDataWithKernels(this.design.getGloverKernels());
	}
	
	private void fillSeriesDataWithKernels(final List<DesignKernel> kernels) {
		for (DesignKernel kernel : kernels) {
			String kernelName = kernel.getID();
			float[][] fctData = kernel.plotGammaWithDerivs(PLOT_DERIVS);
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
			if (PLOT_DERIVS == 2) {
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
		if (this.design != null) {
	        createSeriesData();
	        addCheckBoxPanel();
	        itemStateChanged(null);
		}
	}
}

