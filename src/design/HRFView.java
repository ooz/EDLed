package design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import design.bart.DoubleGammaKernel;

public class HRFView extends JPanel {
	
	/** */
	private static final long serialVersionUID = 224325403482033601L;
	
	float[][] gammaFunction = null;
	
	public HRFView() {
		DoubleGammaKernel doubleGamma = new DoubleGammaKernel(new DoubleGammaKernel.GammaParams(60000, false));
		this.gammaFunction = doubleGamma.plotGammaWithDerivs(2);
		for (int i = 0; i < this.gammaFunction.length; i++) {
			System.out.println(i + "\t" + this.gammaFunction[i][0] 
                                 + "\t" + this.gammaFunction[i][1] 
                                 + "\t" + this.gammaFunction[i][1 + 1] 
                                 + "\t" + this.gammaFunction[i][1 + 2]);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Setting up graphics.
		Graphics2D g2D = (Graphics2D) g;
//		g2D.setFont(this.captionFont);
		g2D.setStroke(new BasicStroke(1.0f));
		g2D.setFont(DesignViewConstants.CAPTION_FONT);
		
//		FontMetrics fontMetrics = g2D.getFontMetrics();
		
		g2D.setBackground(Color.WHITE);
		g2D.setColor(Color.WHITE);
		g2D.fillRect(0, 0, getWidth(), getHeight());
		
		float fctMin = Float.MAX_VALUE;
		float fctMax = Float.MIN_VALUE;
		// find min/max value of function
		for (int i = 0; i < this.gammaFunction.length; i++) {
			if (this.gammaFunction[i][1] < fctMin) {
				fctMin = this.gammaFunction[i][1];
			} else if (this.gammaFunction[i][1] > fctMax) {
				fctMax = this.gammaFunction[i][1];
			}
		}
		
		// Draw HRF
		g2D.setPaint(Color.BLACK);
		float height = 200.0f;
		float valueCorrection;
		if (fctMin < 0.0) {
			valueCorrection = Math.abs(fctMin);
			fctMax += valueCorrection;
		} else {
			valueCorrection = -fctMin;
			fctMax -= valueCorrection;
		}
		for (int i = 0; i < this.gammaFunction.length; i++) {
			float value = this.gammaFunction[i][1];
			value += valueCorrection;
			if (fctMax != 0.0f) {
				value /= fctMax;
			} else {
				value = 0.0f;
			}
			
			if (true) {
				g2D.fillRect(i * 2, (int) (height - value * height), 2, 1);
			}
		}
		
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
//		FontMetrics fontMetrics = g2D.getFontMetrics();
//		int fontHeight = fontMetrics.getHeight();
		
	}

}
