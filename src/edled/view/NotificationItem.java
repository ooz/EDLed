package edled.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import edled.core.Notification;

public class NotificationItem extends JPanel {

	/** Generated. */
	private static final long serialVersionUID = 4161259864915056060L;
	
	protected NotificationPanel pane;

	public NotificationItem(final NotificationPanel pane,
							final Notification<String> n) {
		super(new BorderLayout());

		this.pane = pane;

		switch (n.kind()) {
		case Error:
			this.setBackground(Color.RED);
			this.setForeground(Color.WHITE);
			break;
		case Warn:
			this.setBackground(Color.YELLOW);
 			break;
		default:
			this.setBackground(Color.WHITE);
		}

		JPanel labelPane = createNotificationLabelPane(n);

		this.add(labelPane, BorderLayout.WEST);
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
										  (int) this.getPreferredSize().getHeight()));
	}
	
	private JPanel createNotificationLabelPane(final Notification<String> n) {
		IconProvider ip = IconProvider.getInstance();
		
		final String brief = n.brief();
		final String verbose = n.verbose();
		
		final JPanel labelPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
		
		labelPane.setBackground(this.getBackground());
		
		final JTextArea briefArea = new JTextArea(brief);
		briefArea.setBackground(this.getBackground());
		briefArea.setForeground(this.getForeground());
		briefArea.setEditable(false);
		labelPane.add(briefArea);
		
		if (verbose != null && verbose != "") {
			final JLabel moreLabel = ip.makeMoreLabel();
			moreLabel.setToolTipText("Click to expand.");
			labelPane.add(moreLabel);
			
			MouseAdapter adapter = new MouseAdapter() {
				
				private boolean expand = true;
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (expand) {
						labelPane.removeAll();
						JTextArea ta = new JTextArea(verbose);
						ta.setEditable(false);
						ta.setBackground(getBackground());
						ta.setForeground(getForeground());
						
						ta.addMouseListener(this);
						labelPane.add(ta);
						expand = false;
						
						revalidate();
						setMaximumSize(new Dimension(Integer.MAX_VALUE, 
								  (int) getPreferredSize().getHeight()));
						//repaint();
					} else {
						labelPane.removeAll();
						labelPane.add(briefArea);
						labelPane.add(moreLabel);
						expand = true;
						
						revalidate();
						setMaximumSize(new Dimension(Integer.MAX_VALUE, 
								  (int) getPreferredSize().getHeight()));
						//repaint();
					}
				}
			};
			briefArea.addMouseListener(adapter);
			moreLabel.addMouseListener(adapter);
			labelPane.addMouseListener(adapter);
			this.addMouseListener(adapter);
		}
		
		return labelPane;
	}
	
}
