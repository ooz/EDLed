package edled.core;

import java.util.Observable;

import edled.core.validation.EDLRule;

public class RuleViolationNotification extends Notification<String> {
	
	private EDLRule rule;

	public RuleViolationNotification(final EDLRule rule) {
		super(rule.getMessage(), Notification.NotificationKind.Warn);
		
		this.rule = rule;
		rule.addObserver(this);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (this.rule == o) {
			setChanged();
			notifyObservers();
		}
	}

}
