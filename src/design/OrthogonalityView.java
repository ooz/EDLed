package design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import design.bart.DesignElement;

public class OrthogonalityView extends JPanel implements Observer {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Orthogonality";
	
	private static final int TOP_PADDING = 50;
	private static final int LEFT_PADDING = 50;
	private static final double SQUARE_SIZE = 100.0;
	
	private static final float INVERSE_COLOR_THRESHOLD = 0.40f;
	
	private DesignElement design;
	private float[][] orthogonalityMatrix = null;
	
	private final Font captionFont;
	private final DecimalFormat numberFormat;
	
	public OrthogonalityView(final DesignElement design) {
		this.design = design;
		this.design.addObserver(this);
		this.setBackground(Color.BLACK);
		
		this.captionFont = new Font("SansSerif", Font.PLAIN, 12);
		this.numberFormat = new DecimalFormat("##########################0.##");
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
		
		if (this.orthogonalityMatrix != null) {
			float matrixMax = getMaxValue(this.orthogonalityMatrix);
			int matrixCard = this.orthogonalityMatrix.length;
			
			FontMetrics fontMetrics = g2D.getFontMetrics();
			
			// Draw top caption (x axis)
			for (int col = 0; col < matrixCard; col++) {
				
			}
			
			for (int row = 0; row < matrixCard; row++) {
				// Draw left captopm (y axis)
				
				// Draw matrix
				for (int col = 0; col < matrixCard; col++) {
					
					float colorValue = (matrixMax == 0.0f) ? 0.0f : (this.orthogonalityMatrix[row][col] / matrixMax);
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
	
	private float getMaxValue(final float[][] orthoMatrix) {
		float max = 0.0f;
		
		for (int i = 0; i < orthoMatrix.length; i++) {
			for (int j = i; j < orthoMatrix.length; j++) {
				if (orthoMatrix[i][j] > max) {
					max = orthoMatrix[i][j];
				}
			}
		}
		
		return max;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.design) {
			this.orthogonalityMatrix = this.design.computeOrthogonalityMatrix();
			
			removeAll();
			paintComponent(getGraphics());
			revalidate();
		}
	}
}
