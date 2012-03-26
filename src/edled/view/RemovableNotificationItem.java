package edled.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import edled.core.Notification;

public class RemovableNotificationItem extends NotificationItem {
	
	/** Generated. */
	private static final long serialVersionUID = -7105764978359273927L;
	
	private final RemovableNotificationItem self = this;
	
	private final static Cursor HAND = new Cursor(Cursor.HAND_CURSOR);
	private final static Cursor NORMAL = new Cursor(Cursor.DEFAULT_CURSOR);

	public RemovableNotificationItem(final NotificationPanel pane,
									 final Notification n) {
		super(pane, n);
		
		this.removeAll();
		this.setLayout(new BorderLayout());
		
		this.add(new JLabel(n.getMessage()), BorderLayout.CENTER);
		
		IconProvider ip = IconProvider.getInstance();
		JLabel rm;
		if (ip.getRemoveIcon() != null) {
			rm = new JLabel(ip.getRemoveIcon());
		} else {
			rm = new JLabel(IconProvider.REMOVE_ALT_TEXT);
		}
		
		rm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				self.pane.remove(self);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(HAND);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(NORMAL);
			}
		});
		
		this.add(rm, BorderLayout.EAST);
	}

}
