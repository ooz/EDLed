package de.mpg.cbs.edled.core;

public class StringNotification extends Notification<String> {
	
	public StringNotification(final String brief,
							  final String verbose,
							  final NotificationKind kind) {
		super(brief, verbose, kind);
	}
	
	public StringNotification(final String msg,
							  final NotificationKind kind) {
		super(generateBrief(msg), generateVerbose(msg), kind);
	}

	public static String generateBrief(final String msg) {
		if (msg != null && msg != "") {
			String trimmedMsg = msg.trim();
			if (trimmedMsg.contains("\n")) {
				return trimmedMsg.split("\n")[0];
			} else {
				return trimmedMsg;
			}
		}
		
		return "";
	}
	
	public static String generateVerbose(final String msg) {
		if (msg != null && msg != "") {
			String trimmedMsg = msg.trim();
			if (trimmedMsg.contains("\n")) {
				return trimmedMsg;
			}
		}
		
		return "";
	}
}
