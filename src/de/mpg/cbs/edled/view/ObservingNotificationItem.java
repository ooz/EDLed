package de.mpg.cbs.edled.view;

import java.util.Observable;
import java.util.Observer;

import de.mpg.cbs.edled.core.Notification;


public class ObservingNotificationItem extends NotificationItem implements Observer {
	
	/** Generated. */
	private static final long serialVersionUID = 3926495998174191472L;
	
	
	private Notification<String> n;

	public ObservingNotificationItem(final NotificationPanel pane,
									 final Notification<String> n) {
		super(pane, n);
		
		this.n = n;
		n.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (this.n == o) {
			this.n = null;
			this.pane.remove(this);
		}
	}

}
