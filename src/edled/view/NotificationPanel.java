package edled.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class NotificationPanel extends JPanel {
	
	private final JTextArea txtArea = new JTextArea();
	
	public NotificationPanel() {
		super(new BorderLayout());
		
		this.txtArea.setEditable(false);
		this.add(new JScrollPane(this.txtArea));
	}
	
	public void add(final String txt) {
		this.txtArea.append(txt);
	}
	

}
