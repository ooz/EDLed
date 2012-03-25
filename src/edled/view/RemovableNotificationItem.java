package edled.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import edled.core.Notification;

public class RemovableNotificationItem extends NotificationItem {
	
	/** Generated. */
	private static final long serialVersionUID = -7105764978359273927L;
	
	private final RemovableNotificationItem self = this;

	public RemovableNotificationItem(final NotificationPanel pane,
									 final Notification n) {
		super(pane, n);
		
		JLabel rm = new JLabel("[remove]");
		rm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				self.pane.remove(self);
			}
		});
		this.add(rm);
	}

}
