package design.experimentdata;

/** Binding for /experimentData/paradigm/[swDesignStruct|gwDesignStruct]/timeBasedRegressor */
public class TimeBasedRegressor {
	public double length; // unsigned long
	public String name;
	public String regressorID; // type: ID
	public boolean scaleHeightToZeroMean;
	public String useRefFct; // type: IDREF
	public boolean useRefFctFirstDerivative = false;
	public boolean useRefFctSecondDerivative = false;
	public TbrDesign design;
	
	public TimeBasedRegressor(double length, 
							  String name,
							  String regressorID,
							  boolean scaleHeightToZeroMean,
							  String useRefFct,
							  boolean useRefFctFirstDerivative,
							  boolean useRefFctSecondDerivative,
							  TbrDesign design) {
		if (design == null) {
			throw new IllegalArgumentException("Tried to construct a " +
			    "TimeBasedRegressor with no TbrDesign object (design == null)");
		}
		
		this.length = length;
		this.name = name;
		this.regressorID = regressorID;
		this.scaleHeightToZeroMean = scaleHeightToZeroMean;
		this.useRefFct = useRefFct;
		this.useRefFctFirstDerivative = useRefFctFirstDerivative;
		this.useRefFctSecondDerivative = useRefFctSecondDerivative;
		this.design = design;
	}
}
