package de.mpg.cbs.edledplugin.design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import de.mpg.cbs.edledplugin.design.bart.DesignElement;
import de.mpg.cbs.edledplugin.design.bart.DesignElement.Regressor;

/**
 * Orthogonality view of a DesignElement.
 * This view displays a grayscale matrix indicating the orthogonality
 * of each design column/regressor.
 * 
 * @author Oliver Z.
 */
public class OrthogonalityView extends JPanel implements Observer, DesignElementReceiver {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	private static final int TOP_PADDING = DesignViewConstants.TOP_PADDING;
	private static final int TOP_CAPTION_PADDING = DesignViewConstants.TOP_CAPTION_PADDING;
	private static final int LEFT_PADDING = DesignViewConstants.LEFT_PADDING - 5;
	private static final int LEFT_CAPTION_PADDING = DesignViewConstants.LEFT_CAPTION_PADDING;
	private static final double SQUARE_SIZE = 75.0;
	
	/** Gray value threshold. Below: invert font color for better
	 *  readability. */
	private static final float INVERSE_COLOR_THRESHOLD = 0.40f;
	
	/** The design data to display. */
	private DesignElement design;
	private float[][] orthogonalityMatrix = null;
	
	private final DecimalFormat numberFormat;
	
	public OrthogonalityView(final DesignElement design) {
		this.design = design;
		if (this.design != null) {
			this.design.addObserver(this);
		}
		
		this.setBackground(Color.BLACK);
		this.numberFormat = new DecimalFormat("##########################0.##");
	}
	
	@Override
	public void finalize() {
		if (this.design != null) this.design.deleteObserver(this);
	}
	
	@Override
	public void register(final DesignElement design) {
		if (this.design != null) {
			this.design.deleteObserver(this);
		}
		
		this.design = design;
		if (this.design != null) {
			this.design.addObserver(this);
		}
		this.update(design, design);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Setting up graphics.
		Graphics2D g2D = (Graphics2D) g;
		g2D.setFont(DesignViewConstants.CAPTION_FONT);
		
		g2D.setPaint(Color.BLACK);
		g2D.fillRect(0, 0, getWidth(), getHeight());
		
		g2D.setStroke(new BasicStroke(1.0f));
		g2D.setPaint(Color.WHITE);
		
		if (this.orthogonalityMatrix != null) {
			int matrixCard = this.orthogonalityMatrix.length;
			
			FontMetrics fontMetrics = g2D.getFontMetrics();
			int fontHeight = fontMetrics.getHeight();
			
			int regCount = this.design.getNumberEvents();
			int colCount = (int) this.design.getNumberExplanatoryVariables();
			int colsPerReg = colCount / regCount;
			
			// Draw top caption (x axis)
			for (int col = 0; col < matrixCard; col++) {
				String colNrStr = "" + (col + 1);
				g2D.drawString(colNrStr, 
							   (int) (col * SQUARE_SIZE + SQUARE_SIZE / 2 + LEFT_PADDING - fontMetrics.stringWidth(colNrStr) / 2), 
							   fontHeight + TOP_CAPTION_PADDING);
				
				// Print regressor ID and description/name
				if (col % colsPerReg == 0
					&& col < colCount - 1) {
					Regressor reg = this.design.getRegressorList().get(col / colsPerReg);
					g2D.drawString(reg.regID, 
							       (int) (col * SQUARE_SIZE + SQUARE_SIZE / 2 + LEFT_PADDING - fontMetrics.stringWidth(reg.regID) / 2), 
							       2 * fontHeight + TOP_CAPTION_PADDING);
					g2D.drawString(reg.regDescription, 
						           (int) (col * SQUARE_SIZE + SQUARE_SIZE / 2 + LEFT_PADDING - fontMetrics.stringWidth(reg.regDescription) / 2), 
						           3 * fontHeight + TOP_CAPTION_PADDING);
				}
			}
			
			for (int row = 0; row < matrixCard; row++) {
				// Draw left caption (y axis)
				String rowNrStr = "" + (row + 1);
				g2D.setPaint(Color.WHITE);
				g2D.drawString(rowNrStr, 
					           LEFT_CAPTION_PADDING, 
					           (int) (row * SQUARE_SIZE + SQUARE_SIZE / 2 + TOP_PADDING));
				
				// Draw matrix
				for (int col = 0; col < matrixCard; col++) {
					
//					float colorValue = (matrixMax == 0.0f) ? 0.0f : (this.orthogonalityMatrix[row][col] / matrixMax);
					float colorValue = this.orthogonalityMatrix[row][col];
					// Invert color: Maximum value means parallel (black, 1.0), minimum value means orthogonal (white, 0.0)
					colorValue = 1.0f - colorValue;
					
					if (colorValue > 1.0f) colorValue = 1.0f;
					if (colorValue < 0.0f) colorValue = 0.0f;
					
					Color color = new Color(colorValue, colorValue, colorValue);
					g2D.setPaint(color);
					
					g2D.fillRect((int) (col * SQUARE_SIZE + LEFT_PADDING), 
					 		     (int) (row * SQUARE_SIZE + TOP_PADDING), 
					 		     (int) SQUARE_SIZE, 
						  	     (int) SQUARE_SIZE);
					
					// Draw value into square
					if (colorValue >= INVERSE_COLOR_THRESHOLD) {
						g2D.setPaint(Color.BLACK);
					} else {
						g2D.setPaint(Color.WHITE);
					}
					String valueString = numberFormat.format(this.orthogonalityMatrix[row][col]);
					g2D.drawString(valueString, 
							       (int) (col * SQUARE_SIZE + LEFT_PADDING + SQUARE_SIZE / 2 - fontMetrics.stringWidth(valueString) / 2), 
							       (int) (row * SQUARE_SIZE + TOP_PADDING + 0.5 * SQUARE_SIZE));
					
				}
			}
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o == this.design) {
			if (this.design != null) {
				this.orthogonalityMatrix = this.design.computeOrthogonalityMatrix();
			} else {
				this.orthogonalityMatrix = null;
			}
			
			removeAll();
			paintComponent(getGraphics());
			revalidate();
		}
	}
}
