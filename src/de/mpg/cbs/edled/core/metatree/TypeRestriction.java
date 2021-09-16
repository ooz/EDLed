package de.mpg.cbs.edled.core.metatree;

import java.util.LinkedList;
import java.util.List;

/**
 * Class representing a XML schema type restriction.
 * 
 * @author Oliver Z.
 */
public class TypeRestriction {
	
	/** The type that is restricted. */
	private final String baseType;
	
	/** A concrete set of values is given. */
	private List<String> enumeration = null;
	
	/** Number of total allowed digits for numbers (in String representation). */
	private String totalDigits = null;
	/** Number of total allowed digits past the decimal point (in String representation). */
	private String fractionDigits = null;
	
	/** Exclusive maximum value for numbers (in String representation). */
	private String maxExclusive = null;
	/** Inclusive maximum value for numbers (in String representation). */
    private String maxInclusive = null;
    /** Exclusive minimum value for numbers (in String representation). */
    private String minExclusive = null;
    /** Inclusive minimum value for numbers (in String representation). */
    private String minInclusive = null;
	
    /** Exact length of the value string (number in String representation). */
	private String length = null;
	/** Maximum length of the value string (number in String representation). */
    private String maxLength = null;
    /** Minimum length of the value string (number in String representation). */
    private String minLength = null;
    
    /** Regular expression further specifing the data type. */
    private String pattern = null;
    /** Indicating how whitespace should be handled. */
    private String whiteSpace = null;
    
    /**
     * Constructor.
     * 
     * @param baseType The type further specified by the restriction.
     */
    public TypeRestriction(final String baseType) {
    	this.baseType = baseType;
    }
    
    public void addEnumerationValue(final String value) {
    	if (this.enumeration == null) {
    		this.enumeration = new LinkedList<String>();
    	}
    	
    	this.enumeration.add(value);
    }
    
    public void initTotalDigits(final String value) {
    	if (this.totalDigits == null) {
    		this.totalDigits = value;
    	}
    }
    public void initFractionDigits(final String value) {
    	if (this.fractionDigits == null) {
    		this.fractionDigits = value;
    	}
    }
    public void initMaxExclusive(final String value) {
    	if (this.maxExclusive == null) {
    		this.maxExclusive = value;
    	}
    }
    public void initMaxInclusive(final String value) {
    	if (this.maxInclusive == null) {
    		this.maxInclusive = value;
    	}
    }
    public void initMinExclusive(final String value) {
    	if (this.minExclusive == null) {
    		this.minExclusive = value;
    	}
    }
    public void initMinInclusive(final String value) {
    	if (this.minInclusive == null) {
    		this.minInclusive = value;
    	}
    }
    public void initLength(final String value) {
    	if (this.length == null) {
    		this.length = value;
    	}
    }
    public void initMaxLength(final String value) {
    	if (this.maxLength == null) {
    		this.maxLength = value;
    	}
    }
    public void initMinLength(final String value) {
    	if (this.minLength == null) {
    		this.minLength = value;
    	}
    }
    public void initPattern(final String value) {
    	if (this.pattern == null) {
    		this.pattern = value;
    	}
    }
    public void initWhitespace(final String value) {
    	if (this.whiteSpace == null) {
    		this.whiteSpace = value;
    	}
    }
    
    public String getBaseType()			 { return this.baseType; }
	public List<String> getEnumeration() { return this.enumeration; }
	public String getTotalDigits() 		 { return this.totalDigits; }
	public String getFractionDigits()    { return this.fractionDigits; }
	public String getMaxExclusive() 	 { return this.maxExclusive; }
    public String getMaxInclusive() 	 { return this.maxInclusive; }
    public String getMinExclusive() 	 { return this.minExclusive; }
    public String getMinInclusive() 	 { return this.minInclusive; }
	public String getLength() 			 { return this.length; }
    public String getMaxLength() 		 { return this.maxLength; }
    public String getMinLength() 		 { return this.minLength; }
    public String getPattern() 			 { return this.pattern; }
    public String getWhiteSpace() 		 { return this.whiteSpace; }
    
    @Override
    public String toString() {
		return "{enumeration=" + this.enumeration
	        + ", totalDigits=" + this.totalDigits
	        + ", fractionDigits=" + this.fractionDigits
	        + ", maxExclusive=" + this.maxExclusive
	        + ", maxInclusive=" + this.maxInclusive
	        + ", minExclusive=" + this.minExclusive
	        + ", minInclusive=" + this.minInclusive
	        + ", length=" + this.length
	        + ", maxLength=" + this.maxLength
	        + ", minLength=" + this.minLength
	        + ", pattern=" + this.pattern
	        + ", whiteSpace=" + this.whiteSpace
		     + "}::TypeRestriction";
    }
    
}