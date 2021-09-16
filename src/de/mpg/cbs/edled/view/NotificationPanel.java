package de.mpg.cbs.edled.view;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class NotificationPanel extends JPanel {
	
	/** Generated. */
	private static final long serialVersionUID = 153448051603781971L;
	
	private final JScrollPane scrollPane;
	private final JScrollBar vertBar;
	private final JPanel itemPane = new JPanel();
	
	/** Indicates whether the vertical scrollbar is at the lowest location. */
	private Boolean atBottom = true;
	/** Ignore the scrolling caused by adding NotificationItems. */
	private Boolean justAddedIgnoreScroll = true;
	
	public NotificationPanel() {
		super(new BorderLayout());
		
		this.scrollPane = new JScrollPane(this.itemPane);
		this.vertBar = this.scrollPane.getVerticalScrollBar();
		this.vertBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				Adjustable a = e.getAdjustable();
				
				if (!justAddedIgnoreScroll) {
					if (a.getValue() + a.getVisibleAmount() < a.getMaximum()) {
						atBottom = false;
					} else {
						atBottom = true;
					}
				}
				
				if (atBottom) {
					a.setValue(a.getMaximum());
					justAddedIgnoreScroll = false;
				}
			}
		});
		
		this.add(this.scrollPane);
		
		this.itemPane.setLayout(new BoxLayout(this.itemPane, BoxLayout.Y_AXIS));
	}
	
//	public void add(final String txt) {
//		this.itemPane.add(new NotificationItem(
//							this,
//							new Notification(txt, NotificationKind.Info)));
//		this.itemPane.revalidate();
//		
//		if (atBottom) {
//			justAddedIgnoreScroll = true;
//		}
//	}
	
	public void add(final NotificationItem ni) {
		this.itemPane.add(ni);
		updateScrollBehaviour();
	}
	
	private void updateScrollBehaviour() {
		this.itemPane.revalidate();
		
		if (this.atBottom) {
			this.justAddedIgnoreScroll = true;
		}
	}
	
	@Override
	public void remove(final Component comp) {
		this.itemPane.remove(comp);
		this.itemPane.revalidate();
		this.itemPane.repaint();
	}

}
