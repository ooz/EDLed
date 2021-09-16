package de.mpg.cbs.edled.view;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import de.mpg.cbs.edled.util.Configuration;


/** 
 * Class that keeps track of all icons used in the application.
 * 
 * @author Oliver Z.
 */
public class IconProvider {
	
	/** */
	private static final Logger logger = Logger.getLogger(IconProvider.class);
	
	private static IconProvider instance = null;
	
	/* 
	 * All following unqualified file names will be preceeded either by
	 * ICON_SIZE_MODIFIER_12 or ICON_SIZE_MODIFIER_16. 
	 */
	/** Unqualified file name of the "info/appinfo/annotation"-icon. */
	private static final String INFO_FILENAME = "info.png";
	/** Alternative text that will be rendered if no "info/appinfo/annotation"-icon is available. */
	public static final String INFO_ALT_TEXT = "[?]";
	
	/** Unqualified file name of the "plugin"-icon. */
	private static final String PLUGIN_FILENAME = "plugin.png";
	/** Alternative text that will be rendered if no "plugin"-icon is available. */
	public static final String PLUGIN_ALT_TEXT = "[P]";
	
	/** Unqualified file name of the "add additional node"-icon.  */
	private static final String ADD_ADDITIONAL_FILENAME = "add.png";
	/** Alternative text that will be rendered if no "add additional node"-icon is available. */
	public static final String ADD_ADDITIONAL_ALT_TEXT = "[+]";
	
	/** Unqualified file name of the "add child node"-icon. */
	private static final String ADD_CHILD_FILENAME = "add_child.png";
	/** Alternative text that will be rendered if no "add child node"-icon is available. */
	public static final String ADD_CHILD_ALT_TEXT = "[#]";
	
	/** Unqualified file name of the "add attribute"-icon. */
	private static final String ADD_ATTRIBUTE_FILENAME = "add_attr.png";
	/** Alternative text that will be rendered if no "add attribute"-icon is available. */
	public static final String ADD_ATTRIBUTE_ALT_TEXT = "[A+]";
	
	/** Unqualified file name of the "XSD/EDLRule validation error"-icon. */
	private static final String ERROR_FILENAME = "error.png";
	/** Alternative text that will be rendered if no "XSD/EDLRule validation error"-icon is available. */
	public static final String ERROR_ALT_TEXT = "[!]";
	
	/** Unqualified file name of the "remove this node"-icon. */
	private static final String REMOVE_FILENAME = "remove.png";
	/** Alternative text that will be rendered if no "remove this node"-icon is available. */
	public static final String REMOVE_ALT_TEXT = "[-]";
	
	/** Unqualified file name of the "node has removable attribute(s)"-icon. */
	private static final String REMOVE_ATTRIBUTE_FILENAME = "rm_attr.png";
	/** Alternative text that will be rendered if no "node has removable attribute(s)"-icon is available. */
	public static final String REMOVE_ATTRIBUTE_ALT_TEXT = "[A-]";
	
	/** Unqualified file name of the "alternative node available"-icon. */
	private static final String CHOICE_FILENAME = "alternative.png";
	/** Alternative text that will be rendered if no "alternative node available"-icon is available. */
	public static final String CHOICE_ALT_TEXT = "[||]";
	
	/** Unqualified file name of the "expand text"-icon. */
	private static final String MORE_FILENAME = "more.png";
	/** Alternative text that will be rendered if no "expand text"-icon is available. */
	public static final String MORE_ALT_TEXT = "[more]";
	
	/** Unqualified file name of the "discard/close item"-icon. */
	private static final String CLOSE_FILENAME = "close.png";
	/** Alternative text that will be rendered if no "discard/close"-icon is available. */
	public static final String CLOSE_ALT_TEXT = "[close]";
	
	/** Icon indicating that a description for a configuration entry is available. */
	private Icon infoIcon = null;
	/** Icon indicating that the node can be alternatively configured by a plugin. */
	private Icon pluginIcon = null;
	
	/** Icon indicating that additional nodes of the same type can be added. */
	private Icon addIcon = null;
	/** Icon indicating that an optional child node can be added. */
	private Icon addChildIcon = null;
	/** Icon indicating that an optional attribute node can be added. */
	private Icon addAttributeIcon = null;
	/** Icon indicating an XSD or EDLRule violation. */
	private Icon errorIcon = null;
	/** Icon indicating that the node can be deleted. */
	private Icon removeIcon = null;
	/** Icon indicating that the node has attribute nodes that can be deleted. */
	private Icon removeAttributeIcon = null;
	/** Icon indicating that an alternative node can be chosen. */
	private Icon choiceIcon = null;
	
	/** Icon indicating that there is additional (e.g. notification) text available. */
	private Icon moreIcon = null;
	/** Icon for discarding/removing/closing an item (e.g. notification). */
	private Icon closeIcon = null;
	
	private IconProvider() {
		Configuration config = Configuration.getInstance();
		String imgPath = config.resolveVariables("$IMG_DIR");
		String iconSizeModifier = getIconSizeModifier();
		
		// Load info icon.
		File iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + INFO_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.infoIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Info icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find plugin icon.");
		}
		
		// Load plugin icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + PLUGIN_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.pluginIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Plugin icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find plugin icon.");
		}
		
		// Load add icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ADD_ADDITIONAL_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.addIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Add icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find add icon.");
		}
		// Load add child icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ADD_CHILD_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.addChildIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Add child icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find add child icon.");
		}
		// Load add attribute icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ADD_ATTRIBUTE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.addAttributeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Add attribute icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find add attribute icon.");
		}
		// Load error icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + ERROR_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.errorIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Error icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find error icon.");
		}
		// Load remove icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + REMOVE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.removeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Remove icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find remove icon.");
		}
		// Load remove attribute icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + REMOVE_ATTRIBUTE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.removeAttributeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Remove attribute icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find remove attribute icon.");
		}
		// Load choice icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + CHOICE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.choiceIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Choice icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find choice icon.");
		}
		
		// Load more icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + MORE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.moreIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("More icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find more icon.");
		}
		
		// Load close icon.
		iconFile = new File(imgPath + Configuration.FILE_SEPARATOR + iconSizeModifier + CLOSE_FILENAME);
		if (iconFile.isFile()) {
			try {
				this.closeIcon = new ImageIcon(iconFile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.debug("Close icon URL malformed!", e);
			}
		} else {
			logger.info("Could not find close icon.");
		}
	}
	
	public static IconProvider getInstance() {
		if (IconProvider.instance == null) {
			IconProvider.instance = new IconProvider();
		}
		
		return IconProvider.instance;
	}
	
	public String getIconSizeModifier() {
		if (System.getProperty("os.name").contains("Linux")) {
			return View.ICON_SIZE_MODIFIER_16;
		} else {
			return View.ICON_SIZE_MODIFIER_12;
		}
	}
	
	public Icon getInfoIcon() {
		return infoIcon;
	}

	public Icon getPluginIcon() {
		return pluginIcon;
	}

	public Icon getAddIcon() {
		return addIcon;
	}

	public Icon getAddChildIcon() {
		return addChildIcon;
	}

	public Icon getAddAttributeIcon() {
		return addAttributeIcon;
	}

	public Icon getErrorIcon() {
		return errorIcon;
	}

	public Icon getRemoveIcon() {
		return removeIcon;
	}

	public Icon getRemoveAttributeIcon() {
		return removeAttributeIcon;
	}

	public Icon getChoiceIcon() {
		return choiceIcon;
	}
	
	public Icon getMoreIcon() {
		return moreIcon;
	}
	
	public Icon getCloseIcon() {
		return closeIcon;
	}
	
	
	/* # Label generators # */
	private JLabel makeLabel(final Icon icon,
							 final String altText) {
		JLabel label;
		if (icon == null) {
			label = new JLabel(altText);
		} else {
			label = new JLabel(icon);
		}
		label.setMaximumSize(label.getPreferredSize());
		return label;
	}
	
	public JLabel makeInfoLabel() {
		return makeLabel(this.getInfoIcon(), INFO_ALT_TEXT);
	}
	
	public JLabel makePluginLabel() {
		return makeLabel(this.getPluginIcon(), PLUGIN_ALT_TEXT);
	}

	public JLabel makeAddLabel() {
		return makeLabel(this.getAddIcon(), ADD_ADDITIONAL_ALT_TEXT);
	}

	public JLabel makeAddChildLabel() {
		return makeLabel(this.getAddChildIcon(), ADD_CHILD_ALT_TEXT);
	}

	public JLabel makeAddAttributeLabel() {
		return makeLabel(this.getAddAttributeIcon(), ADD_ATTRIBUTE_ALT_TEXT);
	}

	public JLabel makeErrorLabel() {
		return makeLabel(this.getErrorIcon(), ERROR_ALT_TEXT);
	}

	public JLabel makeRemoveLabel() {
		return makeLabel(this.getRemoveIcon(), REMOVE_ALT_TEXT);
	}

	public JLabel makeRemoveAttributeLabel() {
		return makeLabel(this.getRemoveAttributeIcon(), REMOVE_ATTRIBUTE_ALT_TEXT);
	}

	public JLabel makeChoiceLabel() {
		return makeLabel(this.getChoiceIcon(), CHOICE_ALT_TEXT);
	}
	
	public JLabel makeMoreLabel() {
		return makeLabel(this.getMoreIcon(), MORE_ALT_TEXT);
	}
	
	public JLabel makeCloseLabel() {
		return makeLabel(this.getCloseIcon(), CLOSE_ALT_TEXT);
	}

}
