package design.bart;

import java.util.List;

public class DesignElement {
	
	/* From BAElement.h */
	public enum ImageDataType {
	    IMAGE_DATA_FLOAT,
	    IMAGE_DATA_SHORT,
		IMAGE_DATA_BYTE,
		IMAGE_DATA_UBYTE,
		IMAGE_DATA_USHORT;
	}
	
	/* From BADesignElement.h */
	public static class Trial {
		public long id; // Stimulus number: unsigned int
		float onset;
		float duration; // in seconds
		float height;
	}
	
	/* From BADesignElement.h */
	private long repetitionTimeInMs = 0; 		 // unsigned int
	private long numberExplanatoryVariables = 0; // unsigned int
	private long numberTimesteps = 0;   		 // unsigned int
	private long numberRegressors = 0; 			 // unsigned int
	private long numberCovariates = 0; 		     // unsigned int
	private ImageDataType imageDataType = ImageDataType.IMAGE_DATA_FLOAT;
	
	/* Getter and setters. */
	public long getRepetitionTimeInMs() { return repetitionTimeInMs; }
	public void setRepetitionTimeInMs(long repetitionTimeInMs) { this.repetitionTimeInMs = repetitionTimeInMs; }
	public long getNumberExplanatoryVariables() { return numberExplanatoryVariables; }
	public void setNumberExplanatoryVariables(long numberExplanatoryVariables) { this.numberExplanatoryVariables = numberExplanatoryVariables; }
	public long getNumberTimesteps() { return numberTimesteps; }
	public void setNumberTimesteps(long numberTimesteps) { this.numberTimesteps = numberTimesteps; }
	public long getNumberRegressors() { return numberRegressors; }
	public void setNumberRegressors(long numberRegressors) { this.numberRegressors = numberRegressors; }
	public long getNumberCovariates() { return numberCovariates; }
	public void setNumberCovariates(long numberCovariates) { this.numberCovariates = numberCovariates; }
	public ImageDataType getImageDataType() { return imageDataType;	}
	public void setImageDataType(ImageDataType imageDataType) {	this.imageDataType = imageDataType;	}
	
	/* From NEDesignElementDyn */
	public static class Regressor {
		public List<Trial> regTrialList;
		public long regDerivations; // unsigned int
		public String regID;
		public String regDescription;
		public DesignKernel regConvolKernel;
	}
}
