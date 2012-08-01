package de.mpg.cbs.edled.view;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import de.mpg.cbs.edled.util.Configuration;

/**
 * Window to show a node's detailed description.
 * 
 * @author Oliver Zscheyge
 */
public class DescriptionFrame extends JFrame {
	/** */
	private static final long serialVersionUID = -7022144393593348616L;

	private final static String FRAME_TITLE = "Description";
	
	private final JTextArea textarea;
	
	public DescriptionFrame() {
		setTitle(FRAME_TITLE);
		setIconImage(Configuration.getInstance().getAppIcon());
		
		this.textarea = new JTextArea();
		this.textarea.setEditable(false);
		getContentPane().add(this.textarea);
	}
	
	public void show(final String text) {
		this.textarea.setText(text);
	}

}
