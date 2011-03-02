package design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import design.bart.DesignElement;
import design.bart.DesignElement.Regressor;

public class ConvolutionView extends JPanel implements Observer {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Convolution";
	
	private static final double TIMESTEPS_PER_PIXEL = 0.5;
	private static final double COLUMN_WIDTH = 100.0;
	
	private static final int TOP_PADDING = 65;
	private static final int TOP_CAPTION_PADDING = 5;
	private static final int LEFT_PADDING = 55;
	private static final int LEFT_CAPTION_PADDING = 10;
	
	private static final int TIMESTEP_STEP_WIDTH = 50;
	private static final int Y_AXSIS_DASH_LENGTH = 5;
	
	private DesignElement design;
	
	private Font captionFont;
	
	public ConvolutionView(final DesignElement design) {
		this.design = design;
		this.design.addObserver(this);
		this.setBackground(Color.BLACK);
		
		this.captionFont = new Font("SansSerif", Font.PLAIN, 12);
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
		g2D.setFont(this.captionFont);
		
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
		
		g2D.setPaint(Color.WHITE);
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int fontHeight = fontMetrics.getHeight();
	
		int regCount = this.design.getNumberEvents();
		int colCount = (int) this.design.getNumberExplanatoryVariables();
		
		if (regCount > 0) {
			// Draw top end captions (column number, regressor ID, regressor description)
			int colsPerReg = colCount / regCount; // No need to separately handle the last column due to integer division 
			List<Regressor> regressors = this.design.getRegressorList();
			for (int col = 0; col < colCount; col++) {
				String colNrStr = "" + (col + 1);
				g2D.drawString(colNrStr, 
							   (int) (col * COLUMN_WIDTH + COLUMN_WIDTH / 2 + LEFT_PADDING - fontMetrics.stringWidth(colNrStr) / 2), 
							   fontHeight + TOP_CAPTION_PADDING);
				// Print regressor ID and description/name
				if (col % colsPerReg == 0
					&& col < colCount - 1) {
					Regressor reg = regressors.get(col / colsPerReg);
					g2D.drawString(reg.regID, 
							       (int) (col * COLUMN_WIDTH + COLUMN_WIDTH / 2 + LEFT_PADDING - fontMetrics.stringWidth(reg.regID) / 2), 
							       2 * fontHeight + TOP_CAPTION_PADDING);
					g2D.drawString(reg.regDescription, 
						           (int) (col * COLUMN_WIDTH + COLUMN_WIDTH / 2 + LEFT_PADDING - fontMetrics.stringWidth(reg.regDescription) / 2), 
						           3 * fontHeight + TOP_CAPTION_PADDING);
				}
			}
			
			// Draw captions on the left (timesteps)
			int timestepCount = this.design.getNumberTimesteps();
			for (int timestepCaptionNr = 0; 
				 timestepCaptionNr <= (timestepCount / TIMESTEP_STEP_WIDTH); 
				 timestepCaptionNr++) {
				
				int yPos = (int) ((timestepCaptionNr * TIMESTEP_STEP_WIDTH) / TIMESTEPS_PER_PIXEL + TOP_PADDING);
				String timestepCaption = "";
				if (timestepCaptionNr == 0) {
					timestepCaption += "1"; // Start counting with 1
				} else {
					timestepCaption += (timestepCaptionNr * TIMESTEP_STEP_WIDTH);
					yPos -= (1 / TIMESTEPS_PER_PIXEL);
				}
				
				// Caption
				g2D.drawString(timestepCaption, 
					       	   LEFT_CAPTION_PADDING, 
					       	   yPos + (fontHeight / 2));
				// Dash
				g2D.fillRect(LEFT_PADDING - Y_AXSIS_DASH_LENGTH, 
							 yPos, 
							 (int) Y_AXSIS_DASH_LENGTH, 
							 (int) (1 / TIMESTEPS_PER_PIXEL));
			}
			
			
			// Find min/maxRegValue (for gray scale normalization)
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
					
					g2D.fillRect((int) (row * COLUMN_WIDTH + LEFT_PADDING), 
						 		 (int) (col / TIMESTEPS_PER_PIXEL + TOP_PADDING), 
						 		 (int) COLUMN_WIDTH, 
							  	 (int) (1 / TIMESTEPS_PER_PIXEL));
				}
			}
			
			// TODO: remove (drawing orientation test)
	//		g2D.setBackground(Color.YELLOW);
	//		g2D.setColor(Color.YELLOW);
	//		g2D.fillRect(0, 0, 5, 5);
		}
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
