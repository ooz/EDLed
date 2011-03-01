package design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import design.bart.DesignElement;

public class ConvolutionView extends JPanel implements Observer {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Convolution";
	
	private static final double TIMESTEPS_PER_PIXEL = 0.5;
	private static final double COLUMN_WIDTH = 100.0;
	
	private DesignElement design;
	
	public ConvolutionView(final DesignElement design) {
		this.design = design;
		this.design.addObserver(this);
		this.setBackground(Color.BLACK);
	}
	
	@Override
	public void finalize() {
		this.design.deleteObserver(this);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Setting up graphics.
		Graphics2D g2D = (Graphics2D) g;
//		g2D.setFont(this.captionFont);
		g2D.setStroke(new BasicStroke(1.0f));
		
//		FontMetrics fontMetrics = g2D.getFontMetrics();
		
		g2D.setBackground(Color.BLACK);
		g2D.setColor(Color.BLACK);
		g2D.fillRect(0, 0, getWidth(), getHeight());
		
//		g2D.setColor(Color.WHITE);
//		g2D.setBackground(Color.WHITE);
//		
//		int regNr = 0;
//		for (Regressor reg : this.design.getRegressorList()) {
//			for (Trial trial : reg.regTrialList) {
//				g2D.fillRect(regNr * widthPerReg, 
//							 (int) ((trial.onset / 1000.0f) * heightPerSec), 
//							 widthPerReg, 
//							 (int) ((trial.duration / 1000.0f) * heightPerSec));
////				System.out.println(trial);
//			}
//			regNr++;
//		}
		
		// Find min/maxRegValue
		float[][] regValues = this.design.getRegressorValues();
		float minRegValue = Float.MAX_VALUE;
		float maxRegValue = Float.MIN_VALUE;
		for (int row = 0; row < regValues.length; row++) {
			for (int col = 0; col < regValues[row].length; col++) {
				float regValue = regValues[row][col];
				if (regValue < minRegValue) {
					minRegValue = regValue;
				}
				if (regValue > maxRegValue) {
					maxRegValue = regValue;
				}
			}
		}
		
		if (minRegValue < 0.0f) {
			maxRegValue += Math.abs(minRegValue);
		} else {
			maxRegValue -= minRegValue;
		}
		
		// Draw regressor value matrix
		for (int row = 0; row < regValues.length; row++) {
			for (int col = 0; col < regValues[row].length; col++) {
				float colorValue = regValues[row][col];
				if (minRegValue < 0.0f) {
					colorValue += Math.abs(minRegValue);
				} else {
					colorValue -= minRegValue;
				}
				colorValue /= maxRegValue;
				Color color = new Color(colorValue, colorValue, colorValue);
				g2D.setColor(color);
				g2D.setBackground(color);
				
				g2D.fillRect((int) (row * COLUMN_WIDTH), 
					 		 (int) (col / TIMESTEPS_PER_PIXEL), 
					 		 (int) COLUMN_WIDTH, 
						  	 (int) (1 / TIMESTEPS_PER_PIXEL));
			}
		}
		
		// TODO: remove (drawing orientation test)
		g2D.setBackground(Color.YELLOW);
		g2D.setColor(Color.YELLOW);
		g2D.fillRect(0, 0, 5, 5);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.design) {
			removeAll();
			paintComponent(getGraphics());
			revalidate();
		}
	}

}
