package de.mpg.cbs.edled.view;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.mpg.cbs.edled.plugin.Plugin;
import de.mpg.cbs.edled.util.Configuration;
import de.mpg.cbs.edled.util.FileStatus;



/**
 * Main application window.
 * 
 * @author Oliver Z.
 */
public class MainFrame extends JFrame implements TreeReceiver {
	
	private static final long serialVersionUID = 3172688540921699213L;
	
	/** Tag that will be replaced with the actual application name. */
	private static final String APPNAME = "<appname>";
	/** Tag that will be replaced with the actual version number. */
	private static final String VERSION = "<version>";
	/** Tag that will be replaced with the actual application author's name. */
	private static final String AUTHOR = "<author>";
	/** Text shown in the "About"-window. */
	private static final String ABOUT = APPNAME + " v" + VERSION + "\n"
									  + "by " + AUTHOR + "\n"
									  + "\n"
									  + "Icons\n" 
									  + "by the Tango Desktop Project\n"
									  + "(http://tango.freedesktop.org/)";
	
	/** Indicator for Apple's Mac OS X in system property "os.name". */
	private final static String MAC_OS_X = "Mac OS X";
	
	/** Reference to the view fascade. */
	private View view = null;
	
	/** Menu list of recently opened files. */
	private JMenu recents = new JMenu("Open Recent");
	
	/** Tab panel containing the generalPanel and all plugin views. */
	private JTabbedPane tabbedPane = null;
	/** The general/default view on the EDL/XML document. */
	private GeneralPanel generalPanel = null;
	
	private JTree treeView = null;
	private InspectorPanel inspector = null;
	
	private final int accelerator_mask;
	
	/** 
	 * Map holding the plugin views (key) and their associated qualified
	 * names (value). 
	 */
	private final Map<JPanel, String> pluginViews;
	/**
	 * Previous selected tab.
	 */
	private Component previousSeletion;
	
	/** Constructor */
	public MainFrame(final View view, 
					 final InspectorPanel edlInspectorPanel, 
					 final NotificationPanel notificationPanel) {
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeMainWindow();
			}
		});
		
		this.view = view;
		this.inspector = edlInspectorPanel;
		this.pluginViews = new LinkedHashMap<JPanel, String>();
		
		if (System.getProperty("os.name").startsWith(MAC_OS_X)) {
			this.accelerator_mask = InputEvent.META_DOWN_MASK;
		} else {
			this.accelerator_mask = InputEvent.CTRL_DOWN_MASK;
		}
		
		setupWindow();
		setupMenuBar();
		setupSplitsAndTabs(edlInspectorPanel, 
						   notificationPanel);
	}
	
	private void closeMainWindow() {
//		if (JOptionPane.showConfirmDialog(this,
//				 "Do you really want to quit?",
//				 "Quit",
//				 JOptionPane.YES_NO_OPTION) 
//				 	== JOptionPane.YES_OPTION) {
//			System.exit(0);
//		}
		System.exit(0);
	}
	
	/** Initializes the window. */
	private void setupWindow() {
		this.setTitle(this.view.getAppName());
		this.setMinimumSize(new Dimension(320, 240));
		this.setSize(800, 600);
		this.setIconImage(Configuration.getInstance().getAppIcon());
	}
	
	/** Creates the window's menu bar. */
	private void setupMenuBar() {
		JMenuBar menubar = new JMenuBar();
		JMenuItem item;
		
		// START File menu.
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		// New...
		item = new JMenuItem("New");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newDocument();
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke('N', this.accelerator_mask));
		fileMenu.add(item);
		
		// Open...
		item = new JMenuItem("Open...");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				load();
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke('O', this.accelerator_mask));
		fileMenu.add(item);
		
		// Open Recent
		updateRecentsMenu();
		fileMenu.add(this.recents);
		
		fileMenu.addSeparator();
		
		// Save
		item = new JMenuItem("Save");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save(false);
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke('S', this.accelerator_mask));
		fileMenu.add(item);
		
		// Save as...
		item = new JMenuItem("Save as...");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save(true);
			}
		});
		fileMenu.add(item);
		
		fileMenu.addSeparator();
		
		// Quit.
		item = new JMenuItem("Quit");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeMainWindow();
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke('Q', this.accelerator_mask));
		fileMenu.add(item);
		menubar.add(fileMenu);
		// END File menu.
		
		// START Edit menu.
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		// Preferences.
		item = new JMenuItem("Preferences");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog editPrefDialog = createConfigDialog();
				editPrefDialog.setVisible(true);
			}
		});
		editMenu.add(item);
		menubar.add(editMenu);
		// END Edit menu.
		
//		JMenu windowMenu = new JMenu("Window");
//		windowMenu.setMnemonic(KeyEvent.VK_W);
//		menubar.add(windowMenu);
		
		// START Help menu.
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		// Manual...
		item = new JMenuItem("Manual");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String documentationPath = Configuration.getInstance().resolveVariables("$DOCUMENTATION_DIR$/");
				documentationPath += "index.html";
				File documentationFile = new File(documentationPath);
				
				Desktop desktop = Desktop.getDesktop();
				if(!desktop.isSupported(Desktop.Action.BROWSE)) {
					view.showWarnDialog("Could not open a web browser!\n" +
										"Please go to the \"documentation\" folder in the application directory\n" +
										"and open \"index.html\" manually!");
		        } else if (documentationFile.exists()) {
		        	try {
						desktop.browse(documentationFile.toURI());
					} catch (IOException e) {
						view.showErrorDialog("Error reading " + documentationFile.getAbsolutePath());
					}
		        } else {
		        	view.showErrorDialog("Could not find manual file!\n" +
		        						 "Please check your EDLed installation!");
		        }
			}
		});
		helpMenu.add(item);
		
		// About...
		item = new JMenuItem("About");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.showInfoDialog(MainFrame.ABOUT.replace(APPNAME, view.getAppName())
												   .replace(VERSION, view.getVersion())
												   .replace(AUTHOR, view.getAuthor()));
			}
		});
		helpMenu.add(item);
		
		menubar.add(helpMenu);
		// END Help menu.
		
		this.setJMenuBar(menubar);
	}
	
	private void updateRecentsMenu() {
		List<String> recentFiles = this.view.getRecentFiles();
		
		this.recents.removeAll();
		JMenuItem item;
		for (String path : recentFiles) {
			item = new JMenuItem(path);
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem src = (JMenuItem) e.getSource();
					String path = src.getText();
					loadFile(new File(path));
				}
				
			});
			this.recents.add(item);
		}
	}
	
	/**
	 * Divides the window's visible space into the upper tabbed view containing
	 * the general panel + all plugin views and the lower message output area.
	 * 
	 * @param edlInspectorPanel InspectorPanel showing detailed information
	 * 							about the selected node.
	 * 							(part of the general panel)
	 * @param messageArea		Text message output area in the lower part of
	 * 							the window.
	 */
	private void setupSplitsAndTabs(final InspectorPanel edlInspectorPanel,
									final NotificationPanel notificationPanel) {
		
		this.tabbedPane = new JTabbedPane();
		this.generalPanel = new GeneralPanel(edlInspectorPanel);
		this.previousSeletion = this.generalPanel;
		this.tabbedPane.addTab("General", this.generalPanel);
		
		this.tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateAppAndPlugins();
			}
		});
		
//		StimulusPanel designStimulusPanel = new StimulusPanel();
//		tabbedPane.addTab("Stimuli", designStimulusPanel);
		
//		LogPanel logPanel = new LogPanel();
//		tabbedPane.addTab("Log", logPanel);
		JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
												  tabbedPane, 
												  notificationPanel);
		verticalSplit.setResizeWeight(0.75);
		
		this.getContentPane().add(verticalSplit);
	}
	
	/**
	 * Updates the underlying models based on tab switching.
	 */
	synchronized private void updateAppAndPlugins() {
		Component selectedComp = tabbedPane.getSelectedComponent();
		
		if (this.previousSeletion != this.generalPanel) {
			String pluginName = this.pluginViews.get(this.previousSeletion);
			if (pluginName != null) {
				view.updateFromPlugin(pluginName);
			}
		}
		
		if (selectedComp != this.generalPanel
			&& selectedComp != this.previousSeletion) {
			this.view.updatePlugin(this.pluginViews.get(selectedComp));
		}
		
		this.previousSeletion = selectedComp;
	}
	
	/**
	 * Creates a new EDL/XML document.
	 */
	private void newDocument() {
		this.view.newDocument();
		this.setTitle(this.view.getAppName());
	}
	
	/**
	 * Opens an existing EDL/XML document.
	 */
	private void load() {
		File currentFile = this.view.getCurrentFile();
		JFileChooser fileChooser;
		if (currentFile == null) {
			fileChooser = new JFileChooser(Configuration.getInstance().getSysProp("user.dir"));
		} else {
			fileChooser = new JFileChooser(currentFile.getParent());
		}
		
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			loadFile(fileChooser.getSelectedFile());
		}
	}
	private void loadFile(final File fileToLoad) {
		FileStatus status = this.view.loadXMLFile(fileToLoad);
		switch (status) {
		case SUCCESS:
			this.setTitle(fileToLoad.getPath() + " - " + this.view.getAppName());
			this.previousSeletion = null;
			updateRecentsMenu();
			updateAppAndPlugins();
			break;
		case NOT_FOUND:
			this.view.showErrorDialog("The file " 
	                  + fileToLoad.getName() 
	                  + " could not be found!");
			break;
		case NOT_COMPLIANT:
			this.view.showErrorDialog("The file " 
	                  + fileToLoad.getName() 
	                  + " is not a valid according to the given XSD (see Menu>Edit>Preferences)!");
			break;
		default:
			break;
		}
	}
	
	/**
	 * Saves the current EDL/XML document.
	 * 
	 * @param promptForFile Flag indicating whether the current document should
	 * 						be saved to a new file.
	 * 						True:  file chooser is shown. Current document is 
	 * 							   saved to the selected file.
 	 *						False: Document is saved to the current file.
 	 *							   If there is no current file nothing will be
 	 *							   saved.
	 */
	private void save(final boolean promptForFile) {
		File currentXML = this.view.getCurrentFile();
		boolean aborted = false;
		if (promptForFile
			|| currentXML == null) {
			JFileChooser fileChooser = new JFileChooser(Configuration.getInstance().getSysProp("user.dir"));
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				currentXML = fileChooser.getSelectedFile();
			} else {
				aborted = true;
			}
		}
		
		if (currentXML != null
			&& !aborted) {
			updateAppAndPlugins();
			this.view.save(currentXML);
			this.setTitle(currentXML.getName() + " - " + this.view.getAppName());
			
			this.treeView.repaint();
			this.inspector.repaint();
		}
	}
	
	/**
	 * Builds a JDialog allowing the user to view and edit all configuration 
	 * entries (key-value pairs).
	 * 
	 * @return JDialog representing the configuration dialog.
	 */
	private JDialog createConfigDialog() {
		final Configuration config = Configuration.getInstance();
		Set<String> keys = config.getKeys();
		final Map<String, String> modifiedEntries = new HashMap<String, String>();
		
		final JDialog dialog = new JOptionPane().createDialog(this, "Preferences");
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new GridLayout(keys.size() + 1, 2));
		dialog.setContentPane(contentPane);
		
		for (final String key : keys) {
			contentPane.add(new JLabel(key));
			final JTextField input = new JTextField(config.getPropWithoutResolvedVariables(key));
			input.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
				}
				@Override
				public void keyReleased(KeyEvent e) {
					modifiedEntries.put(key, input.getText());
					config.setProp(key, input.getText());
				}
				@Override
				public void keyPressed(KeyEvent e) {
				}
			});
			contentPane.add(input);
		}
		
		JButton saveButton = new JButton("save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String key : modifiedEntries.keySet()) {
					String value = modifiedEntries.get(key);
					config.setProp(key, value);
				}
				config.save();
				
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
//		saveButton.setMaximumSize(saveButton.getPreferredSize());
		contentPane.add(saveButton);
		
		JButton cancelButton =  new JButton("cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
//		cancelButton.setMaximumSize(cancelButton.getPreferredSize());
		contentPane.add(cancelButton);
		
		dialog.setResizable(true);
		dialog.pack();
		
		return dialog;
//		config.save();
	}

	@Override
	public void setTree(final JTree tree) {
		this.treeView = tree;
		this.generalPanel.setTree(tree);
		
		this.repaint();
	}
	
	/**
	 * Acknowledges a plugin to the main view (adding the plugin's view to
	 * the main view).
	 * 
	 * @param plugin The Plugin to add.
	 */
	public void addPlugin(final Plugin plugin) {
		JPanel pluginView = plugin.getView();
		this.pluginViews.put(pluginView, plugin.getQualifiedName());
		this.tabbedPane.addTab(plugin.getName(), pluginView);
	}

}
