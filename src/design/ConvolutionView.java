package design;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class ConvolutionView extends JPanel {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Convolution"; 
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Setting up graphics.
		Graphics2D g2D = (Graphics2D) g;
//		g2D.setFont(this.captionFont);
		g2D.setStroke(new BasicStroke(1.0f));
		
//		List<String> mediaObjIDs = this.timetable.getMediaObjectIDs();
		FontMetrics fontMetrics = g2D.getFontMetrics();
		
//		g2d.setBackground(Color.BLACK);
//		g2d.fillRect(getX(), getY(), getWidth(), getHeight());
	}

}
