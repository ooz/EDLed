package edled.core;

import java.util.Observable;
import java.util.Observer;

public class Notification extends Observable implements Observer {
	
	private final String msg;
	
	public Notification(final String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}

	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(arg);
	}
	
}
