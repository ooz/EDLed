package de.mpg.cbs.edled.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import de.mpg.cbs.edled.core.Notification;


public class NotificationItem extends JPanel {

	/** Generated. */
	private static final long serialVersionUID = 4161259864915056060L;
	
	private final static String EXPAND_TOOLTIP = "Click to expand.";
	private final static String COLLAPSE_TOOLTIP = "Click to collapse.";
	
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

		setupNotificationLabel(n);

		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
										  (int) this.getPreferredSize().getHeight()));
	}
	
	private void setupNotificationLabel(final Notification<String> n) {
		IconProvider ip = IconProvider.getInstance();
		
		final String brief = n.brief();
		final String verbose = n.verbose();
		
		final JPanel briefPane = new JPanel();
		briefPane.setLayout(new BoxLayout(briefPane, BoxLayout.LINE_AXIS));
		briefPane.setAlignmentX(LEFT_ALIGNMENT);
		briefPane.setBackground(this.getBackground());
		
		final JTextArea briefArea = new JTextArea(brief);
		briefArea.setBackground(this.getBackground());
		briefArea.setForeground(this.getForeground());
		briefArea.setEditable(false);
		
		briefPane.add(briefArea);
		
		if (verbose != null && verbose != "") {
			final JLabel moreLabel = ip.makeMoreLabel();
			briefArea.setToolTipText(EXPAND_TOOLTIP);
			moreLabel.setToolTipText(EXPAND_TOOLTIP);
			briefArea.append(" [...]");
			briefPane.add(moreLabel);
			
			final JPanel verbosePane = new JPanel();
			verbosePane.setLayout(new BoxLayout(verbosePane, BoxLayout.LINE_AXIS));
			verbosePane.setAlignmentX(LEFT_ALIGNMENT);
			verbosePane.setBackground(this.getBackground());
			
			final JTextArea verboseArea = new JTextArea(verbose);
			verboseArea.setToolTipText(COLLAPSE_TOOLTIP);
			verboseArea.setBackground(getBackground());
			verboseArea.setForeground(getForeground());
			verboseArea.setEditable(false);
			
			verbosePane.add(verboseArea);
			
			MouseAdapter adapter = new MouseAdapter() {
				private boolean expand = true;
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (expand) {
						remove(briefPane);
						add(verbosePane, BorderLayout.CENTER);
						expand = false;
//						verbosePane.revalidate();
//						verbosePane.repaint();
						
						revalidate();
						setMaximumSize(new Dimension(Integer.MAX_VALUE, 
								       (int) getPreferredSize().getHeight()));
//						repaint();

					} else {
						remove(verbosePane);
						add(briefPane, BorderLayout.CENTER);
						expand = true;
//						briefPane.revalidate();
//						briefPane.repaint();
						
						revalidate();
						setMaximumSize(new Dimension(Integer.MAX_VALUE, 
								       (int) getPreferredSize().getHeight()));
//						repaint();
					}
				}
			};
			
			briefArea.addMouseListener(adapter);
			moreLabel.addMouseListener(adapter);
			briefPane.addMouseListener(adapter);
			verboseArea.addMouseListener(adapter);
			verbosePane.addMouseListener(adapter);
			this.addMouseListener(adapter);
		}
		
		add(briefPane, BorderLayout.CENTER);
	}
	
}