package de.mpg.cbs.edledplugin.stimulus;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.mpg.cbs.edled.core.Notification;
import de.mpg.cbs.edled.core.Notification.NotificationKind;
import de.mpg.cbs.edled.util.Configuration;
import de.mpg.cbs.edled.view.View;


/**
 * Utility class used for building the dialogs for media objects, 
 * stimulus events and the screen object.
 * 
 * @author Oliver Z.
 */
public class DialogFactory {
	
	/** Default text field width. */
	private static final int DIALOG_TEXTFIELD_COLUMNS = 15;
	/** Default edge length for the color chooser icon. */
	private static final int COLOR_CHOOSER_ICON_SIZE = 16;
	/** Default color displayed in the color chooser. */
	private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;
	
	/** Options to appear in the media object kind dropdown menu: */
	private static final String TEXT_CARD = "Text";
	private static final String IMAGE_CARD = "Image";
	private static final String AUDIO_CARD = "Audio";
	private static final String VIDEO_CARD = "Video";
	
	/** 
	 * Labels for the coordinate input text fields (only applicable for
	 * text, image and video media objects.
	 */
	private static final String X_COORD_CAPTION = "Position: x-coordinate";
	private static final String Y_COORD_CAPTION = "Position: y-coordinate";
	/** 
	 * Default file name if no file is specified for a image/audio/video
	 * media object yet. 
	 */
	private static final String DEFAULT_FILE_NAME = "...";
	
	/** DialogFactory singleton. */
	private static DialogFactory defaultFactory = null;
	
	/** The color chooser for text media objects. */
	private static Color choosenColor = null;
	
	/**
	 * Singleton method.
	 * 
	 * @return The DialogFactory singleton.
	 */
	static DialogFactory getDefaultFactory() {
		if (DialogFactory.defaultFactory == null) {
			DialogFactory.defaultFactory = new DialogFactory();
		}
		
		return DialogFactory.defaultFactory;
	}
	
	/**
	 * Factory method to create a dialog for manipulation of the
	 * screen object.
	 * 
	 * @param parent     The parent Component that owns the dialog.
	 * @param controller The StimulusPlugin plugin controller.
	 * @return			 A screen dialog.
	 */
	JDialog createScreenDialog(final Component parent, 
							   final StimulusPlugin controller) {
		
		final Screen screen = controller.getModel().getScreen();
		int currentWidth = screen.getWidth();
		int currentHeight = screen.getHeight();
		
		final JDialog dialog = new JOptionPane().createDialog(parent, "Change screen size");
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		dialog.setContentPane(contentPane);
		
		JPanel screenPane = new JPanel();
		screenPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		screenPane.setLayout(new BoxLayout(screenPane, BoxLayout.Y_AXIS));
		screenPane.setBorder(BorderFactory.createTitledBorder("Screen"));
		
		JLabel widthCaption = new JLabel("Width");
		widthCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		screenPane.add(widthCaption);
		
		final JTextField widthInput = new JTextField();
		widthInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		widthInput.setText(new Integer(currentWidth).toString());
		screenPane.add(widthInput);
		
		JLabel heightCaption = new JLabel("Height");
		heightCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		screenPane.add(heightCaption);
		
		final JTextField heightInput = new JTextField();
		heightInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		heightInput.setText(new Integer(currentHeight).toString());
		screenPane.add(heightInput);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		buttonPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		
		JButton addButton = new JButton("Change");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int newWidth = Integer.parseInt(widthInput.getText());
					if (newWidth > 0) {
						screen.setWidth(newWidth);
					}
				} catch (NumberFormatException ex) {
				}
				
				try {
					int newHeight = Integer.parseInt(heightInput.getText());
					if (newHeight > 0) {
						screen.setHeight(newHeight);
					}
				} catch (NumberFormatException ex) {
				}
				
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonPane.add(addButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonPane.add(cancelButton);
		
		screenPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, screenPane.getPreferredSize().height));
		dialog.add(screenPane);
		dialog.add(Box.createVerticalGlue());
		dialog.add(buttonPane);
		
		dialog.setResizable(true);
		dialog.pack();
		
		return dialog;
	}
	
	// TODO: Refactor this mess.
	/**
	 * Factory method to create a dialog for media objects
	 * (creation and editing of media objects).
	 * 
	 * @paran parent	 The parent Component that owns the dialog.
	 * @param controller The StimulusPlugin plugin controller.
	 * @return 			 A media object dialog.
	 */
	JDialog createMediaObjectDialog(final Component parent, 
									final StimulusPlugin controller,
									final MediaObject mediaObj) {
		
		String mediaObjID = "";
		String mediaObjName = "";
		
		String mediaTextText = "";
		String mediaTextSize = "";
		Icon mediaTextColorIcon = createColoredIcon(DEFAULT_TEXT_COLOR, 
				 									COLOR_CHOOSER_ICON_SIZE * 2, 
				 									COLOR_CHOOSER_ICON_SIZE);
		DialogFactory.choosenColor = DEFAULT_TEXT_COLOR;
		String mediaTextX = "";
		String mediaTextY = "";
		
		String mediaImageFile = DEFAULT_FILE_NAME;
		String mediaImageX = "";
		String mediaImageY = "";
		
		String mediaAudioFile = DEFAULT_FILE_NAME;
		
		String mediaVideoFile = DEFAULT_FILE_NAME;
		String mediaVideoX = "";
		String mediaVideoY = "";
		
		final JDialog dialog;
		if (mediaObj == null) {
			dialog = new JOptionPane().createDialog(parent, "Add media object");
		} else {
			dialog = new JOptionPane().createDialog(parent, "Edit media object");
			
			mediaObjID = mediaObj.getID();
			mediaObjName = mediaObj.getName();
			
			switch (mediaObj.getKind()) {
			case TEXT:
				MediaText mediaText = (MediaText) mediaObj;
				mediaTextText = mediaText.getText();
				mediaTextSize = String.format("%d", mediaText.getTextSize());
				mediaTextColorIcon = createColoredIcon(mediaText.getColor(), 
													   COLOR_CHOOSER_ICON_SIZE * 2, 
													   COLOR_CHOOSER_ICON_SIZE);
				DialogFactory.choosenColor = mediaText.getColor();
				mediaTextX = String.format("%d", mediaText.getVisualPosition().x);
				mediaTextY = String.format("%d", mediaText.getVisualPosition().y);
				break;
				
			case IMAGE:
				MediaImage mediaImage = (MediaImage) mediaObj;
				mediaImageFile = mediaImage.getImageFile().getPath();
				mediaImageX = String.format("%d", mediaImage.getVisualPosition().x);
				mediaImageY = String.format("%d", mediaImage.getVisualPosition().y);
				break;
				
			case AUDIO:
				MediaAudio mediaAudio = (MediaAudio) mediaObj;
				mediaAudioFile = mediaAudio.getAudioFile().getPath();
				break;
				
			case VIDEO:
				MediaVideo mediaVideo = (MediaVideo) mediaObj;
				mediaVideoFile = mediaVideo.getVideoFile().getPath();
				mediaVideoX = String.format("%d", mediaVideo.getVisualPosition().x);
				mediaVideoY = String.format("%d", mediaVideo.getVisualPosition().y);
				break;
				
			default:
				break;
			}
		}
		Container contentPane = dialog.getContentPane();
		contentPane.removeAll();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		JPanel mediaObjectPanel = new JPanel();
		mediaObjectPanel.setLayout(new BoxLayout(mediaObjectPanel, BoxLayout.Y_AXIS));
		mediaObjectPanel.setBorder(BorderFactory.createTitledBorder("Media object"));
		mediaObjectPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		// Common fields among media objects: ID and name.
		JPanel commonPanel = new JPanel();
		commonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		commonPanel.setLayout(new BoxLayout(commonPanel, BoxLayout.Y_AXIS));
		
		JLabel _IDCaption = new JLabel("ID");
		_IDCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		commonPanel.add(_IDCaption);
		final JTextField _IDInput = new JTextField(mediaObjID, DIALOG_TEXTFIELD_COLUMNS);
		if (mediaObj != null) {
			_IDInput.setEnabled(false);
		}
		_IDInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		commonPanel.add(_IDInput);
		
		JLabel nameCaption = new JLabel("Name");
		nameCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		commonPanel.add(nameCaption);
		final JTextField nameInput = new JTextField(mediaObjName, DIALOG_TEXTFIELD_COLUMNS);
		nameInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		commonPanel.add(nameInput);
		
		// Use cards to handle different kinds of media objects.
		JLabel kindCaption = new JLabel("Kind");
		kindCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		commonPanel.add(kindCaption);
		
		final JPanel cards = new JPanel(new CardLayout());
		cards.setAlignmentX(Component.LEFT_ALIGNMENT);

		String[] cardOptions = {TEXT_CARD, IMAGE_CARD, AUDIO_CARD, VIDEO_CARD};
		final JComboBox cardChooser = new JComboBox(cardOptions);
		cardChooser.setEditable(false);
		cardChooser.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		commonPanel.add(cardChooser);
		commonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, commonPanel.getPreferredSize().height));
		
		JPanel fieldPanel;
		
		// Text.
		JPanel textCard = new JPanel();
		textCard.setLayout(new BoxLayout(textCard, BoxLayout.Y_AXIS));
		
		fieldPanel = new JPanel();
		fieldPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		fieldPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
		
		JLabel textCaption = new JLabel("Text");
		textCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(textCaption);
		
		final JTextField textInput = new JTextField(mediaTextText, DIALOG_TEXTFIELD_COLUMNS);
		textInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(textInput);
		
		JLabel sizeCaption = new JLabel("Size");
		sizeCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(sizeCaption);
		
		final JTextField sizeInput = new JTextField(mediaTextSize, DIALOG_TEXTFIELD_COLUMNS);
		sizeInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(sizeInput);
		
		JLabel colorCaption = new JLabel("Color");
		colorCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(colorCaption);
		
		final JButton colorInput = new JButton(mediaTextColorIcon);
		colorInput.setForeground(DEFAULT_TEXT_COLOR);
		colorInput.setBackground(DEFAULT_TEXT_COLOR);
		colorInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		colorInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(dialog, 
														  "Choose a text color", 
														  colorInput.getForeground());
				colorInput.setIcon(createColoredIcon(newColor, 
													 COLOR_CHOOSER_ICON_SIZE * 2, 
													 COLOR_CHOOSER_ICON_SIZE));
				DialogFactory.choosenColor = newColor;
			}
		});
		fieldPanel.add(colorInput);
		
		JLabel xCoordCaption = new JLabel(X_COORD_CAPTION);
		xCoordCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(xCoordCaption);
		
		final JTextField textXInput = new JTextField(mediaTextX, DIALOG_TEXTFIELD_COLUMNS);
		textXInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(textXInput);
		
		JLabel yCoordCaption = new JLabel(Y_COORD_CAPTION);
		yCoordCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(yCoordCaption);
		
		final JTextField textYInput = new JTextField(mediaTextY, DIALOG_TEXTFIELD_COLUMNS);
		textYInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(textYInput);
		
		fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldPanel.getPreferredSize().height));
		textCard.add(fieldPanel);
		textCard.add(Box.createVerticalGlue());
		
		cards.add(textCard, TEXT_CARD);
		
		// Image.
		JPanel imageCard = new JPanel();
		imageCard.setLayout(new BoxLayout(imageCard, BoxLayout.Y_AXIS));
		
		fieldPanel = new JPanel();
		fieldPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		fieldPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
		
		JLabel imageFilePathCaption = new JLabel("File");
		imageFilePathCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(imageFilePathCaption);
		
		final JButton imageFilePathButton = new JButton(mediaImageFile);
		imageFilePathButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		imageFilePathButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, imageFilePathButton.getPreferredSize().height));
		imageFilePathButton.addActionListener(createActionListenerForFileChooserButton(imageFilePathButton));
		fieldPanel.add(imageFilePathButton);
		
		xCoordCaption = new JLabel(X_COORD_CAPTION);
		xCoordCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(xCoordCaption);
		
		final JTextField imageXInput = new JTextField(mediaImageX, DIALOG_TEXTFIELD_COLUMNS);
		imageXInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(imageXInput);
		
		yCoordCaption = new JLabel(Y_COORD_CAPTION);
		yCoordCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(yCoordCaption);
		
		final JTextField imageYInput = new JTextField(mediaImageY, DIALOG_TEXTFIELD_COLUMNS);
		imageYInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(imageYInput);
		
		fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldPanel.getPreferredSize().height));
		imageCard.add(fieldPanel);
		imageCard.add(Box.createVerticalGlue());
		
		cards.add(imageCard, IMAGE_CARD);
		
		// Audio.
		JPanel audioCard = new JPanel();
		audioCard.setLayout(new BoxLayout(audioCard, BoxLayout.Y_AXIS));
		
		fieldPanel = new JPanel();
		fieldPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		fieldPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
		
		JLabel audioFilePathCaption = new JLabel("File");
		audioFilePathCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(audioFilePathCaption);
		
		final JButton audioFilePathButton = new JButton(mediaAudioFile);
		audioFilePathButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		audioFilePathButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, audioFilePathButton.getPreferredSize().height));
		audioFilePathButton.addActionListener(createActionListenerForFileChooserButton(audioFilePathButton));
		fieldPanel.add(audioFilePathButton);
		
		fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldPanel.getPreferredSize().height));
		audioCard.add(fieldPanel);
		audioCard.add(Box.createVerticalGlue());
		
		cards.add(audioCard, AUDIO_CARD);
		
		// Video.
		JPanel videoCard = new JPanel();
		videoCard.setLayout(new BoxLayout(videoCard, BoxLayout.Y_AXIS));
		
		fieldPanel = new JPanel();
		fieldPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		fieldPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
		
		JLabel videoFilePathCaption = new JLabel("File");
		videoFilePathCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(videoFilePathCaption);
		
		final JButton videoFilePathButton = new JButton(mediaVideoFile);
		videoFilePathButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		videoFilePathButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, videoFilePathButton.getPreferredSize().height));
		videoFilePathButton.addActionListener(createActionListenerForFileChooserButton(videoFilePathButton));
		fieldPanel.add(videoFilePathButton);
		
		xCoordCaption = new JLabel(X_COORD_CAPTION);
		xCoordCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(xCoordCaption);
		
		final JTextField videoXInput = new JTextField(mediaVideoX, DIALOG_TEXTFIELD_COLUMNS);
		videoXInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(videoXInput);
		
		yCoordCaption = new JLabel(Y_COORD_CAPTION);
		yCoordCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(yCoordCaption);
		
		final JTextField videoYInput = new JTextField(mediaVideoY, DIALOG_TEXTFIELD_COLUMNS);
		videoYInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldPanel.add(videoYInput);
		
		fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldPanel.getPreferredSize().height));
		videoCard.add(fieldPanel);
		videoCard.add(Box.createVerticalGlue());
		
		cards.add(videoCard, VIDEO_CARD);
		
		mediaObjectPanel.add(commonPanel);
		mediaObjectPanel.add(cards);
		
		// Enable card choose options or disable them when a media object is edited.
		final CardLayout layout = (CardLayout) cards.getLayout();
		if (mediaObj == null) {
			cardChooser.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					layout.show(cards, (String) e.getItem());
				}
			});
		} else {
			switch (mediaObj.getKind()) {
			case TEXT:
				layout.show(cards, TEXT_CARD);
				cardChooser.setSelectedItem(TEXT_CARD);
				break;
			case IMAGE:
				layout.show(cards, IMAGE_CARD);
				cardChooser.setSelectedItem(IMAGE_CARD);
				break;
			case AUDIO:
				layout.show(cards, AUDIO_CARD);
				cardChooser.setSelectedItem(AUDIO_CARD);
				break;
			case VIDEO:
				layout.show(cards, VIDEO_CARD);
				cardChooser.setSelectedItem(VIDEO_CARD);
				break;
			default:
				break;
			}
			cardChooser.setEnabled(false);
		}

		// Buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		JButton addEditButton;
		if (mediaObj == null) {
			addEditButton = new JButton("Add");
		} else {
			addEditButton = new JButton("Edit");
		}
		
		addEditButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {			
				String mediaObjKind = (String) cardChooser.getSelectedItem();
				
				if (mediaObj == null) {
					if (mediaObjKind.equals(TEXT_CARD)) {
						addTextMediaObject(controller, 
										   _IDInput.getText().trim(), 
										   nameInput.getText().trim(), 
										   textInput.getText().trim(),
										   sizeInput.getText().trim(), 
										   DialogFactory.choosenColor, 
										   textXInput.getText().trim(), 
										   textYInput.getText().trim());
						
					} else if (mediaObjKind.equals(IMAGE_CARD)) {
						addImageMediaObject(controller, 
											_IDInput.getText().trim(), 
											nameInput.getText().trim(), 
											imageFilePathButton.getText().trim(), 
											imageXInput.getText().trim(), 
											imageYInput.getText().trim());
						
					} else if (mediaObjKind.equals(AUDIO_CARD)) {
						addAudioMediaObject(controller, 
											_IDInput.getText().trim(), 
											nameInput.getText().trim(), 
											audioFilePathButton.getText().trim());
						
					} else if (mediaObjKind.equals(VIDEO_CARD)) {
						addVideoMediaObject(controller, 
											_IDInput.getText().trim(), 
											nameInput.getText().trim(), 
											videoFilePathButton.getText().trim(), 
											videoXInput.getText().trim(), 
											videoYInput.getText().trim());
					}
				} else {
					if (mediaObjKind.equals(TEXT_CARD)) {
						editTextMediaObject((MediaText) mediaObj, 
										    nameInput.getText().trim(), 
										    textInput.getText().trim(),
										    sizeInput.getText().trim(), 
										    DialogFactory.choosenColor, 
										    textXInput.getText().trim(), 
										    textYInput.getText().trim());
						
					} else if (mediaObjKind.equals(IMAGE_CARD)) {
						editImageMediaObject((MediaImage) mediaObj, 
											 nameInput.getText().trim(), 
											 imageFilePathButton.getText().trim(), 
											 imageXInput.getText().trim(), 
											 imageYInput.getText().trim());
						
					} else if (mediaObjKind.equals(AUDIO_CARD)) {
						editAudioMediaObject((MediaAudio) mediaObj, 
											 nameInput.getText().trim(), 
											 audioFilePathButton.getText().trim());
						
					} else if (mediaObjKind.equals(VIDEO_CARD)) {
						editVideoMediaObject((MediaVideo) mediaObj,
											 nameInput.getText().trim(), 
											 videoFilePathButton.getText().trim(), 
											 videoXInput.getText().trim(), 
											 videoYInput.getText().trim());
					}
					controller.getView().getMediaObjectListPanel().update(controller.getModel().getMediaObjectList(), null);
				}
				
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonPanel.add(addEditButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		
		buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());
		dialog.add(mediaObjectPanel);
		dialog.add(buttonPanel);
		
		dialog.setResizable(true);
		dialog.pack();
//		dialog.setResizable(false);
		
		return dialog;
	}
	/**
	 * Helper method to create the color chooser icon.
	 * 
	 * @param color  The color to compose the icon of.
	 * @param width  Width of the icon in pixels.
	 * @param height Height of the icon in pixels.
	 * @return		 A color chooser icon.
	 */
	private Icon createColoredIcon(final Color color, 
								   final int width, 
								   final int height) {
		int rgb = color.getRGB();
		BufferedImage colorImage = new BufferedImage(width, 
													 height, 
				                             		 BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				colorImage.setRGB(x, y, rgb);
			}
		}
		
		return new ImageIcon(colorImage);
	}
	private ActionListener createActionListenerForFileChooserButton(final JButton button) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(Configuration.getInstance().getSysProp("user.dir"));
				if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
					button.setText(fileChooser.getSelectedFile().getPath());
				}
			}
		};
	}
	private void addTextMediaObject(final StimulusPlugin controller,
									final String idInput,
									final String nameInput,
									final String textInput,
									final String sizeInput,
									final Color color,
									final String xCoordInput,
									final String yCoordInput) {
		
		MediaObjectList mediaObjList = controller.getModel().getMediaObjectList();
		View appView = controller.getApplication().getView();
		
		try {
			int size = Integer.parseInt(sizeInput);
			
			try {
				int x = Integer.parseInt(xCoordInput);
				int y = Integer.parseInt(yCoordInput);
				
				Point position = new Point(x, y);
				
				mediaObjList.add(new MediaText(idInput, nameInput, textInput, size, color, position));
			} catch (NumberFormatException e) {
				appView.show(new Notification<String>("Could not add text media object: position coordinates are not valid!", NotificationKind.Warn), true);
			}
		} catch (NumberFormatException e) {
			appView.show(new Notification<String>("Could not add text media object: text size is no integer!", NotificationKind.Warn), true);
		}
	}
	private void addImageMediaObject(final StimulusPlugin controller,
									 final String idInput,
									 final String nameInput,
									 final String filePathInput,
									 final String xCoordInput,
									 final String yCoordInput) {
		
		MediaObjectList mediaObjList = controller.getModel().getMediaObjectList();
		View appView = controller.getApplication().getView();
		
		try {
			int x = Integer.parseInt(xCoordInput);
			int y = Integer.parseInt(yCoordInput);
			
			Point position = new Point(x, y);
			
			mediaObjList.add(new MediaImage(idInput, nameInput, position, new File(filePathInput)));
		} catch (NumberFormatException e) {
			appView.show(new Notification<String>("Could not add image media object: position coordinates are not valid!", NotificationKind.Warn), true);
		}
	}
	private void addAudioMediaObject(final StimulusPlugin controller,
									 final String idInput,
									 final String nameInput,
									 final String filePathInput) {
		
		controller.getModel().getMediaObjectList()
			.add(new MediaAudio(idInput, nameInput, new File(filePathInput)));
	}
	private void addVideoMediaObject(final StimulusPlugin controller,
									 final String idInput,
									 final String nameInput,
									 final String filePathInput,
									 final String xCoordInput,
									 final String yCoordInput) {
		
		MediaObjectList mediaObjList = controller.getModel().getMediaObjectList();
		View appView = controller.getApplication().getView();
		
		try {
			int x = Integer.parseInt(xCoordInput);
			int y = Integer.parseInt(yCoordInput);
			
			Point position = new Point(x, y);
			
			mediaObjList.add(new MediaVideo(idInput, nameInput, position, new File(filePathInput)));
		} catch (NumberFormatException e) {
			appView.show(new Notification<String>("Could not add video media object: position coordinates are not valid!", NotificationKind.Warn), true);
		}
	}
	
	private void editTextMediaObject(final MediaText mediaObj,
									 final String nameInput,
									 final String textInput,
									 final String sizeInput,
									 final Color color,
									 final String xCoordInput,
									 final String yCoordInput) {
		try {
			int size = Integer.parseInt(sizeInput);
			int x = Integer.parseInt(xCoordInput);
			int y = Integer.parseInt(yCoordInput);
				
			Point position = new Point(x, y);
			
			mediaObj.setName(nameInput);
			mediaObj.setText(textInput);
			mediaObj.setTextSize(size);
			mediaObj.setColor(color);
			mediaObj.setVisualPosition(position);
		} catch (NumberFormatException e) {
		}
	}
	private void editImageMediaObject(final MediaImage mediaObj,
			 						  final String nameInput,
			 						  final String filePathInput,
			 						  final String xCoordInput,
			 						  final String yCoordInput) {
		try {
			int x = Integer.parseInt(xCoordInput);
			int y = Integer.parseInt(yCoordInput);
			
			Point position = new Point(x, y);
			
			mediaObj.setName(nameInput);
			mediaObj.setVisualPosition(position);
			mediaObj.setImageFile(new File(filePathInput));
		} catch (NumberFormatException e) {
		}
	}
	private void editAudioMediaObject(final MediaAudio mediaObj,
									  final String nameInput,
									  final String filePathInput) {
		mediaObj.setName(nameInput);
		mediaObj.setAudioFile(new File(filePathInput));
	}
	private void editVideoMediaObject(final MediaVideo mediaObj,
									  final String nameInput,
									  final String filePathInput,
									  final String xCoordInput,
									  final String yCoordInput) {
		try {
			int x = Integer.parseInt(xCoordInput);
			int y = Integer.parseInt(yCoordInput);
			
			Point position = new Point(x, y);
			
			mediaObj.setName(nameInput);
			mediaObj.setVisualPosition(position);
			mediaObj.setVideoFile(new File(filePathInput));
		} catch (NumberFormatException e) {
		}
	}
	
	JDialog createStimEventDialog(final Component parent, 
								  final StimulusPlugin controller) {
		
		final JDialog dialog = new JOptionPane().createDialog(parent, "Add event(s)");
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		dialog.setContentPane(contentPane);
		
		JLabel timeCaption = new JLabel("Time(s) in ms");
//		timeCaption.setMaximumSize(timeCaption.getPreferredSize());
		timeCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		final JTextField timeInput = new JTextField(DIALOG_TEXTFIELD_COLUMNS);
//		timeInput.setMaximumSize(timeInput.getPreferredSize());
		timeInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel durationCaption = new JLabel("Duration(s) in ms");
//		durationCaption.setMaximumSize(durationCaption.getPreferredSize());
		durationCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		final JTextField durationInput = new JTextField(DIALOG_TEXTFIELD_COLUMNS);
//		durationInput.setMaximumSize(durationInput.getPreferredSize());
		durationInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel mediaObjCaption = new JLabel("Media object ID(s)");
//		mediaObjCaption.setMaximumSize(mediaObjCaption.getPreferredSize());
		mediaObjCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		final JTextField mediaObjInput = new JTextField(DIALOG_TEXTFIELD_COLUMNS);
//		mediaObjInput.setMaximumSize(mediaObjInput.getPreferredSize());
		mediaObjInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JButton addButton = new JButton("Add");
		addButton.setMaximumSize(addButton.getPreferredSize());
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addEventsFromInput(controller, 
								   timeInput.getText(), 
								   durationInput.getText(), 
								   mediaObjInput.getText());
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(cancelButton.getPreferredSize());
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		
		JPanel eventPane = new JPanel();
		eventPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		eventPane.setLayout(new BoxLayout(eventPane, BoxLayout.Y_AXIS));
		eventPane.setBorder(BorderFactory.createTitledBorder("Event"));
		eventPane.add(timeCaption);
		eventPane.add(timeInput);
		eventPane.add(durationCaption);
		eventPane.add(durationInput);
		eventPane.add(mediaObjCaption);
		eventPane.add(mediaObjInput);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		buttonPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.add(addButton);
		buttonPane.add(cancelButton);
		
		eventPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, eventPane.getPreferredSize().height));
		dialog.add(eventPane);
		dialog.add(Box.createVerticalGlue());
		dialog.add(buttonPane);
		
		dialog.setResizable(true);
		dialog.pack();
//		dialog.setResizable(false);
		
		return dialog;
	}
	
	private void addEventsFromInput(final StimulusPlugin controller,
									final String timeInput, 
									final String durationInput, 
									final String _IDInput) {
		
		final MediaObjectList mediaObjectList = controller.getModel().getMediaObjectList();
		final Timetable timetable = controller.getModel().getTimetable();
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				List<String> times = parseRangeOrList(controller, timeInput);
				List<String> durations = parseRangeOrList(controller, durationInput);
				List<String> mediaObjIDs = parseRangeOrList(controller, _IDInput);
				
				try {
					if (times.size() == durations.size()
						&& times.size() == mediaObjIDs.size()) {
						for (int i = 0; i < times.size(); i++) {
							long time = Long.parseLong(times.get(i));
							long duration = Long.parseLong(durations.get(i));
							MediaObject mediaObj = mediaObjectList.getMediaObject(mediaObjIDs.get(i));
							if (mediaObj != null) {
								timetable.add(new StimEvent(time, duration, mediaObj));
							}
						}
					} else if (times.size() == durations.size()
							   && mediaObjIDs.size() == 1) {
						
						MediaObject mediaObj = mediaObjectList.getMediaObject(mediaObjIDs.get(0));
						if (mediaObj != null) {
							for (int i = 0; i < times.size(); i++) {
								long time = Long.parseLong(times.get(i));
								long duration = Long.parseLong(durations.get(i));
								timetable.add(new StimEvent(time, duration, mediaObj));
							}
						}
					} else if (times.size() == mediaObjIDs.size()
							   && durations.size() == 1) {
						
						long duration = Long.parseLong(durations.get(0));
						for (int i = 0; i < times.size(); i++) {
							long time = Long.parseLong(times.get(i));
							MediaObject mediaObj = mediaObjectList.getMediaObject(mediaObjIDs.get(i));
							if (mediaObj != null) {
								timetable.add(new StimEvent(time, duration, mediaObj));
							}	
						}
					} else if (times.size() > 1
							   && durations.size() == 1
							   && mediaObjIDs.size() == 1) {
						
						final long duration = Long.parseLong(durations.get(0));
						final MediaObject mediaObj = mediaObjectList.getMediaObject(mediaObjIDs.get(0));
						if (mediaObj != null) {
							List<StimEvent> eventsToAdd = new LinkedList<StimEvent>();
							for (int i = 0; i < times.size(); i++) {
								long time = Long.parseLong(times.get(i));
								eventsToAdd.add(new StimEvent(time, duration, mediaObj));
							}
							timetable.add(eventsToAdd);
						}
					} else {
//						controller.getApplication().getView()
//							.showMessage("Input error! Tried to set non compatible value tuples in event addition dialogue!", JOptionPane.ERROR_MESSAGE);
					}
				} catch(NumberFormatException e) {
//					controller.getApplication().getView()
//						.showMessage("Input error! A non valid time/duration was given!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		thread.start();
	}
	
	private List<String> parseRangeOrList(final StimulusPlugin controller, final String valueStr) {
		
		if (isRange(valueStr)) {
			String[] valueRange = valueStr.split(":");
			List<String> values = new LinkedList<String>();
			
			try {
				long start = Long.parseLong(valueRange[0]);
				long maximum = Long.parseLong(valueRange[2]);
				long increment = Long.parseLong(valueRange[1]);
				for (long i = start; i <= maximum; i += increment) {
					values.add(String.format("%d", i));
				}
				
			} catch(NumberFormatException e) {
//				controller.getApplication().getView()
//					.showMessage("Input error! A non valid time/duration range was given!", JOptionPane.ERROR_MESSAGE);
			}
			
			return values;
		} else {
			return Arrays.asList(valueStr.split(" "));
		}
	}
	
	private boolean isRange(final String rangeStr) {
		if (rangeStr.split(":").length == 3) {
			return true;
		}
		
		return false;
	}

}
