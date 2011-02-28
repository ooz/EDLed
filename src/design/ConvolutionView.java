package design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import design.bart.DesignElement;
import design.bart.DesignElement.Regressor;
import design.bart.DesignElement.Trial;

public class ConvolutionView extends JPanel implements Observer {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Convolution";
	
	private DesignElement design;
	
	public ConvolutionView(final DesignElement design) {
		this.design = design;
		this.design.addObserver(this);
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
		
//		List<String> mediaObjIDs = this.timetable.getMediaObjectIDs();
//		FontMetrics fontMetrics = g2D.getFontMetrics();
		
		int widthPerReg = 100;
		int heightPerSec = 1;
		
		g2D.setBackground(Color.BLACK);
		g2D.fillRect(0, 0, getWidth(), getHeight());
		
		g2D.setColor(Color.WHITE);
		g2D.setBackground(Color.WHITE);
		
		int regNr = 0;
		for (Regressor reg : this.design.getRegressorList()) {
			for (Trial trial : reg.regTrialList) {
				g2D.fillRect(regNr * widthPerReg, 
							 (int) ((trial.onset / 1000.0f) * heightPerSec), 
							 widthPerReg, 
							 (int) ((trial.duration / 1000.0f) * heightPerSec));
//				System.out.println(trial);
			}
			regNr++;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.design) {
			removeAll();
			paintComponent(getGraphics()); // paint(getGraphics()); caused a deadlock - don't know why yet
			revalidate();
		}
	}

}
