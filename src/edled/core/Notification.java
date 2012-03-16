package edled.core;

import java.util.Observable;
import java.util.Observer;

public class Notification extends Observable implements Observer {
	
	public static enum NotificationKind {
		Info,
		Warn,
		Error
	}
	
	private final String msg;
	private final NotificationKind kind;
	
	public Notification(final String msg,
						final NotificationKind kind) {
		this.msg = msg;
		this.kind = kind;
	}
	
	public String getMessage() {
		return this.msg;
	}
	
	public NotificationKind getKind() {
		return this.kind;
	}

	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(arg);
	}
	
}
