package de.mpg.cbs.edled.core;

import java.util.Observable;
import java.util.Observer;

public class Notification<T> extends Observable implements Observer {
	
	public static enum NotificationKind {
		Info,
		Warn,
		Error
	}
	
	private final String brief;
	private final T      verbose;
	private final NotificationKind kind;
	
	public Notification(final String brief,
						final T verbose,
						final NotificationKind kind) {
		this.brief   = brief;
		this.verbose = verbose;
		this.kind =    kind;
	}
	
	public Notification(final String brief,
						final NotificationKind kind) {
		this(brief, null, kind);
	}
	
	public String brief() {
		return this.brief;
	}
	
	public T verbose() {
		return this.verbose;
	}
	
	public NotificationKind kind() {
		return this.kind;
	}

	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(arg);
	}
	
}
