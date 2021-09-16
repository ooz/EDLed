package de.mpg.cbs.edled.view;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.mpg.cbs.edled.util.Configuration;

/**
 * Window to show a node's detailed description.
 * 
 * @author Oliver Z.
 */
public class DescriptionFrame extends JFrame {
	/** */
	private static final long serialVersionUID = -7022144393593348616L;

	private final static String FRAME_TITLE = "Description";
	private final static int FONT_SIZE = 12;
	
	private final JTextArea textarea;
	
	public DescriptionFrame() {
		setTitle(FRAME_TITLE);
		setIconImage(Configuration.getInstance().getAppIcon());
		
		this.textarea = new JTextArea();
		this.textarea.setEditable(false);
		this.textarea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
		getContentPane().add(new JScrollPane(this.textarea));
	}
	
	public void show(final String text) {
		this.textarea.setText(text);
	}

}
