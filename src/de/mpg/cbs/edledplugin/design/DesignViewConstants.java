package de.mpg.cbs.edledplugin.design;

import java.awt.Font;

/**
 * Class containing various constants related to the view of the design plugin.
 * 
 * @author Oliver Z.
 */
public class DesignViewConstants {
	/* Paddings. */
	/** Top padding for the diagram (does not include caption). */
	public static final int TOP_PADDING = 65;
	/** Top padding for the caption. */
	public static final int TOP_CAPTION_PADDING = 5;
	/** Left padding for the diagram (does not include caption). */
	public static final int LEFT_PADDING = 55;
	/** Left padding for the caption. */
	public static final int LEFT_CAPTION_PADDING = 10;
	
	/* Fonts. */
	/** The font to use for captions. */
	public static final Font CAPTION_FONT = new Font("SansSerif", Font.PLAIN, 12);
	
	/* Tab names. */
	/** Text of the convolution view tab label. */
	public static final String CONVOLUTION_VIEW_NAME = "Convolution";
	/** Text of the orthogonality view tab label. */
	public static final String ORTHOGONALITY_VIEW_NAME = "Orthogonality";
	/** Text of the HRF view tab label. */
	public static final String HRF_VIEW_NAME = "HRF";
}
