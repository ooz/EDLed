package de.mpg.cbs.edled.core;

import java.util.Observable;

import de.mpg.cbs.edled.core.validation.EDLRule;


public class RuleViolationNotification extends StringNotification {
	
	private EDLRule rule;

	public RuleViolationNotification(final EDLRule rule) {
		super(rule.getMessage(),
			  Notification.NotificationKind.Warn);
		
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
