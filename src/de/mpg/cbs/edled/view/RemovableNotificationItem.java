package de.mpg.cbs.edled.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.mpg.cbs.edled.core.Notification;


public class RemovableNotificationItem extends NotificationItem {
	
	/** Generated. */
	private static final long serialVersionUID = -7105764978359273927L;
	
	private final static String REMOVE_TOOLTIP = "Click to remove.";
	
	private final RemovableNotificationItem self = this;
	
	private final static Cursor HAND = new Cursor(Cursor.HAND_CURSOR);
	private final static Cursor NORMAL = new Cursor(Cursor.DEFAULT_CURSOR);

	public RemovableNotificationItem(final NotificationPanel pane,
									 final Notification<String> n) {
		super(pane, n);
		
		IconProvider ip = IconProvider.getInstance();
		JLabel rm = ip.makeCloseLabel();
		rm.setToolTipText(REMOVE_TOOLTIP);
		
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
		
		JPanel rmPane = new JPanel();
		rmPane.setLayout(new BoxLayout(rmPane, BoxLayout.PAGE_AXIS));
		rmPane.setAlignmentY(TOP_ALIGNMENT);
		rmPane.setBackground(getBackground());
		
		rmPane.add(rm);
		
		this.add(rmPane, BorderLayout.EAST);
	}

}
