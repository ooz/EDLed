package design;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import design.bart.DoubleGammaKernel;

public class HRFView extends JPanel implements ItemListener {
	
	/** */
	private static final long serialVersionUID = 224325403482033601L;
	
	/** HRF + 2 derivs */
	private static final int SERIES_COUNT = 3;
	
	float[][] gammaFunction = null;
	XYSeriesCollection allHRFSeries = new XYSeriesCollection();
	XYSeries hrfSeries;
	XYSeries deriv1Series;
	XYSeries deriv2Series;
	
	private JFreeChart hrfChart;
	private JCheckBox hrfChecker;
	private JCheckBox deriv1Checker;
	private JCheckBox deriv2Checker;
	
	public HRFView() {
        this.setLayout(new BorderLayout());
        createSeriesData();
        this.allHRFSeries.addSeries(this.hrfSeries);
    	this.hrfChart = createHRFChart();
        final ChartPanel chartPanel = new ChartPanel(hrfChart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        this.add(chartPanel, BorderLayout.CENTER);
        this.add(createCheckBoxPanel(), BorderLayout.NORTH);
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
	
	private JPanel createCheckBoxPanel() {
		this.hrfChecker = new JCheckBox("HRF");
		this.hrfChecker.setSelected(true);
		this.deriv1Checker = new JCheckBox("1st derivative");
		this.deriv1Checker.setSelected(false);
		this.deriv2Checker = new JCheckBox("2nd derivative");
		this.deriv2Checker.setSelected(false);
		
		addItemListenerToCheckBoxes();
		
		JPanel cbPanel = new JPanel();
		cbPanel.add(this.hrfChecker);
		cbPanel.add(this.deriv1Checker);
		cbPanel.add(this.deriv2Checker);
		
		return cbPanel;
	}
	private void addItemListenerToCheckBoxes() {
		this.hrfChecker.addItemListener(this);
		this.deriv1Checker.addItemListener(this);
		this.deriv2Checker.addItemListener(this);
	}
	
	private void createSeriesData() {
		DoubleGammaKernel doubleGamma = new DoubleGammaKernel(new DoubleGammaKernel.GammaParams(60000, false));
		this.gammaFunction = doubleGamma.plotGammaWithDerivs(2);
		this.hrfSeries = new XYSeries(new Comparable<Object>() {
			@Override
			public int compareTo(Object o) { return 0; }
			@Override
			public String toString()       { return "HRF"; }
		});
		this.deriv1Series = new XYSeries(new Comparable<Object>() {
			@Override
			public int compareTo(Object o) { return 0; }
			@Override
			public String toString()       { return "1st derivative"; }
		});
		this.deriv2Series = new XYSeries(new Comparable<Object>() {
			@Override
			public int compareTo(Object o) { return 0; }
			@Override
			public String toString()       { return "2nd derivative"; }
		});
		
		for (int i = 0; i < this.gammaFunction.length; i++) {
	//		System.out.println(i + "\t" + this.gammaFunction[i][0] 
	//                             + "\t" + this.gammaFunction[i][1] 
	//                             + "\t" + this.gammaFunction[i][1 + 1] 
	//                             + "\t" + this.gammaFunction[i][1 + 2]);
	//		
			hrfSeries.add(this.gammaFunction[i][0], this.gammaFunction[i][1]);
			deriv1Series.add(this.gammaFunction[i][0], this.gammaFunction[i][2]);
			deriv2Series.add(this.gammaFunction[i][0], this.gammaFunction[i][3]);
		}
	}

	private JFreeChart createHRFChart() {
	    return ChartFactory.createXYLineChart(
	        "HRF",
	        "Time in seconds", 
	        "Height", 
	        allHRFSeries,
	        PlotOrientation.VERTICAL,
	        true,
	        true,
	        false
	    );
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		this.allHRFSeries.addSeries(this.hrfSeries);
		this.allHRFSeries.addSeries(this.deriv1Series);
		this.allHRFSeries.addSeries(this.deriv2Series);
		
		this.allHRFSeries.removeAllSeries();
		if (this.hrfChecker.isSelected()) {
			this.allHRFSeries.addSeries(this.hrfSeries);
		}
		if (this.deriv1Checker.isSelected()) {
			this.allHRFSeries.addSeries(this.deriv1Series);
		}
		if (this.deriv2Checker.isSelected()) {
			this.allHRFSeries.addSeries(this.deriv2Series);
		}
	}
}

