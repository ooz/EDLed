package edled.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edled.core.Notification;

public class ObservingNotificationItem extends JPanel implements Observer {
	
	/** Generated. */
	private static final long serialVersionUID = 3926495998174191472L;
	
	private NotificationPanel pane;
	private Notification n;

	public ObservingNotificationItem(final NotificationPanel pane,
							final Notification n) {
		super(new FlowLayout(FlowLayout.LEFT));
		
		this.pane = pane;
		this.n = n;
		n.addObserver(this);
		
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
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) this.getPreferredSize().getHeight()));
	}

	@Override
	public void update(Observable o, Object arg) {
		if (this.n == o) {
			this.n = null;
			this.pane.remove(this);
		}
	}

}
