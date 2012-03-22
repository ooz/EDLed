package edled.view;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edled.core.Notification;

public class NotificationItem extends JPanel {
	
	/** Generated. */
	private static final long serialVersionUID = 3926495998174191472L;

	public NotificationItem(final Notification n) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setBackground(new Color((float) Math.random(), 
									 (float) Math.random(),
									 (float) Math.random()));
		JLabel l = new JLabel(n.getMessage());
		this.add(l);
		this.setMaximumSize(this.getPreferredSize());
	}

}
