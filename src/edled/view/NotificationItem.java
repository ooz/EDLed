package edled.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edled.core.Notification;

public class NotificationItem extends JPanel {

	/** Generated. */
	private static final long serialVersionUID = 4161259864915056060L;
	
	protected NotificationPanel pane;

	public NotificationItem(final NotificationPanel pane,
							final Notification n) {
		super(new FlowLayout(FlowLayout.LEFT));

		this.pane = pane;

		JLabel l = new JLabel(n.getMessage());

		switch (n.getKind()) {
		case Error:
			this.setBackground(Color.RED);
			l.setForeground(Color.WHITE);
			break;
		case Warn:
			this.setBackground(Color.YELLOW);
			break;
		default:
			this.setBackground(Color.WHITE);
		}

		this.setAlignmentX(LEFT_ALIGNMENT);

		this.add(l);
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) this
				.getPreferredSize().getHeight()));
	}
}
