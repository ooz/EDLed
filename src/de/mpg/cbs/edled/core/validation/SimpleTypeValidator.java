package de.mpg.cbs.edled.core.validation;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.TreeManager;
import de.mpg.cbs.edled.core.metatree.NodeConstraint;
import de.mpg.cbs.edled.core.metatree.TypeRestriction;
import de.mpg.cbs.edled.xml.XMLUtility;
import de.mpg.cbs.edled.xml.XSDTypeName;



/**
 * Class for validation of simple element and attribute types 
 * (including types derived by type restrictions).
 * 
 * @author Oliver Z.
 */
public class SimpleTypeValidator {
	
	/* Regular Expressons for  */
	// See: http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-CombiningChar
	private static final String COMBINING_CHAR_CLASS = "\\u0300-\\u0345\\u0360-\\u0361\\u0483-\\u0486\\u0591-\\u05A1\\u05A3-\\u05B9\\u05BB-\\u05BD\\u05BF\\u05C1-\\u05C2\\u05C4\\u064B-\\u0652\\u0670\\u06D6-\\u06DC\\u06DD-\\u06DF\\u06E0-\\u06E4\\u06E7-\\u06E8\\u06EA-\\u06ED\\u0901-\\u0903\\u093C\\u093E-\\u094C\\u094D\\u0951-\\u0954\\u0962-\\u0963\\u0981-\\u0983\\u09BC\\u09BE\\u09BF\\u09C0-\\u09C4\\u09C7-\\u09C8\\u09CB-\\u09CD\\u09D7\\u09E2-\\u09E3\\u0A02\\u0A3C\\u0A3E\\u0A3F\\u0A40-\\u0A42\\u0A47-\\u0A48\\u0A4B-\\u0A4D\\u0A70-\\u0A71\\u0A81-\\u0A83\\u0ABC\\u0ABE-\\u0AC5\\u0AC7-\\u0AC9\\u0ACB-\\u0ACD\\u0B01-\\u0B03\\u0B3C\\u0B3E-\\u0B43\\u0B47-\\u0B48\\u0B4B-\\u0B4D\\u0B56-\\u0B57\\u0B82-\\u0B83\\u0BBE-\\u0BC2\\u0BC6-\\u0BC8\\u0BCA-\\u0BCD\\u0BD7\\u0C01-\\u0C03\\u0C3E-\\u0C44\\u0C46-\\u0C48\\u0C4A-\\u0C4D\\u0C55-\\u0C56\\u0C82-\\u0C83\\u0CBE-\\u0CC4\\u0CC6-\\u0CC8\\u0CCA-\\u0CCD\\u0CD5-\\u0CD6\\u0D02-\\u0D03\\u0D3E-\\u0D43\\u0D46-\\u0D48\\u0D4A-\\u0D4D\\u0D57\\u0E31\\u0E34-\\u0E3A\\u0E47-\\u0E4E\\u0EB1\\u0EB4-\\u0EB9\\u0EBB-\\u0EBC\\u0EC8-\\u0ECD\\u0F18-\\u0F19\\u0F35\\u0F37\\u0F39\\u0F3E\\u0F3F\\u0F71-\\u0F84\\u0F86-\\u0F8B\\u0F90-\\u0F95\\u0F97\\u0F99-\\u0FAD\\u0FB1-\\u0FB7\\u0FB9\\u20D0-\\u20DC\\u20E1\\u302A-\\u302F\\u3099\\u309A";
	private static final String EXTENDER_CLASS = "\\u00B7\\u02D0\\u02D1\\u0387\\u0640\\u0E46\\u0EC6\\u3005\\u3031-\\u3035\\u309D-\\u309E\\u30FC-\\u30FE";
	
	private static final String ANYURI_PATTERN = "([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);/\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*";
	private static final String DECIMAL_PATTERN = "[\\+\\-]?[\\d]+[\\.[\\d]+]?";
	private static final String INTEGER_PATTERN = "[\\+\\-]?[\\d]+";
	private static final String NCNAME_PATTERN = "[[\\w" + "[" + COMBINING_CHAR_CLASS + "[" + EXTENDER_CLASS + "]" + "]" + "]&&[^\\d]][\\w\\.\\-" + "[" + COMBINING_CHAR_CLASS + "[" + EXTENDER_CLASS + "]" + "]" + "]*";
	private static final String NMTOKEN_PATTERN = "[[\\S]&&[^\\,]]+";
	private static final String NONNEGATIVE_INTEGER_PATTERN = "[\\+]?[\\d]+";
	
	private static final double UNSIGNED_INT_MAX  = 4294967295.0;
	private static final double UNSIGNED_LONG_MAX = 18446744073709551615.0;
	
	private static SimpleTypeValidator singleton = null;
	
	private final Map<XSDTypeName, Pattern> patterns = new HashMap<XSDTypeName, Pattern>();
	
	private final TypeRestrictionValidator typeRestrictionValidator;
	
	private SimpleTypeValidator() {
		// Precompile patterns for faster validation.
		this.patterns.put(XSDTypeName.anyURI, Pattern.compile(ANYURI_PATTERN));
		this.patterns.put(XSDTypeName.decimal, Pattern.compile(DECIMAL_PATTERN));
		this.patterns.put(XSDTypeName.integer, Pattern.compile(INTEGER_PATTERN));
		this.patterns.put(XSDTypeName.NCName, Pattern.compile(NCNAME_PATTERN));
		this.patterns.put(XSDTypeName.NMTOKEN, Pattern.compile(NMTOKEN_PATTERN));
		this.patterns.put(XSDTypeName.nonNegativeInteger, Pattern.compile(NONNEGATIVE_INTEGER_PATTERN));
		
		this.typeRestrictionValidator = new TypeRestrictionValidator();
	}


	
	public static SimpleTypeValidator getSingleton() {
		if (SimpleTypeValidator.singleton == null) {
			SimpleTypeValidator.singleton = new SimpleTypeValidator();
		}
		
		return SimpleTypeValidator.singleton;
	}
	
	public boolean validate(final Node toValidate, 
							final NodeConstraint constraint) {
		return validateAgainstBaseType(toValidate, constraint)
			   && validateAgainstTypeRestriction(toValidate, constraint);
	}
	
	private boolean validateAgainstBaseType(final Node toValidate, 
										    final NodeConstraint constraint) {
		
		String textToValidate = XMLUtility.getNodeValue(toValidate);
		if (textToValidate == null) {
			textToValidate = "";
		}
		
		String typeName = constraint.getTypeName();
		
		if (typeName == null) {
			return false;
		}
		
		if (typeName.equals("anyType")
			|| typeName.equals("anySimpleType")
			|| typeName.equals("string")) {
			return true;
		}
		if (typeName.equals("anyURI")) {
			return isAnyURI(textToValidate);
		}
		
		if (typeName.equals("boolean")) {
			return isBoolean(textToValidate);
		}
		if (typeName.equals("decimal")) {
			return isDecimal(textToValidate);
		}
		if (typeName.equals("double")) {
			return isDouble(textToValidate);
		}
		if (typeName.equals("int")) {
			return isInt(textToValidate);
		}
		if (typeName.equals("integer")) {
			return isInteger(textToValidate);
		}
		if (typeName.equals("long")) {
			return isLong(textToValidate);
		}
		if (typeName.equals("NCName")) {
			return isNCName(textToValidate);
		}
		if (typeName.equals("ID")) {
			return isValidID(textToValidate, toValidate);
		}
		if (typeName.equals("IDREF")) {
			return isValidIDREF(textToValidate);
		}
		if (typeName.equals("IDREFS")) {
			for (String id : splitListType(textToValidate)) {
				if (!isValidIDREF(id)) {
					return false;
				}
			}
			return true;
		}
		if (typeName.equals("NMTOKEN")) {
			return isNMToken(textToValidate);
		}
		if (typeName.equals("nonNegativeInteger")) {
			return isNonNegativeInteger(textToValidate);
		}
		if (typeName.equals("token")) {
			return isToken(textToValidate);
		}
		if (typeName.equals("unsignedInt")) {
			return isUnsignedInt(textToValidate);
		}
		if (typeName.equals("unsignedLong")) {
			return isUnsignedLong(textToValidate);
		}
		
		return false;
	}
	private boolean validateAgainstTypeRestriction(final Node toValidate,
												   final NodeConstraint constraint) {
		String textToValidate = XMLUtility.getNodeValue(toValidate);
		if (textToValidate == null) {
			textToValidate = "";
		}

		return this.typeRestrictionValidator.validate(toValidate, constraint);
	}
	
	private boolean isValidID(final String id, final Node node) {
		boolean valid = isNCName(id);
		
		if (valid) {
			if (TreeManager.getNodeForID(id) != node) {
				valid = false;
			}
		}
		
		return valid;
	}
	private boolean isValidIDREF(final String id) {
		return isNCName(id) && (TreeManager.getNodeForID(id) != null);
	}
	
	private boolean isAnyURI(final String toValidate) {
		return this.patterns.get(XSDTypeName.anyURI).matcher(toValidate).matches();
	}
	private boolean isBoolean(final String toValidate) {
		if (toValidate.equals("true")
			|| toValidate.equals("false")
			|| toValidate.equals("1")
			|| toValidate.equals("0")) {
			return true;
		}
		
		return false;
	}
	private boolean isDecimal(final String toValidate) {
		return this.patterns.get(XSDTypeName.decimal).matcher(toValidate).matches();
	}
	private boolean isDouble(final String toValidate) {
		boolean isDouble = true;
		
		try {
			Double.parseDouble(toValidate);
		} catch (NumberFormatException e) {
			isDouble = false;
		}
		
		return isDouble;
	}
	private boolean isInt(final String toValidate) {
		boolean isInt = true;
		
		try {
			Integer.parseInt(toValidate);
		} catch (NumberFormatException e) {
			isInt = false;
		}
		
		return isInt;
	}
	private boolean isInteger(final String toValidate) {
		return this.patterns.get(XSDTypeName.integer).matcher(toValidate).matches();
	}
	private boolean isLong(final String toValidate) {
		boolean isLong = true;
		
		try {
			Long.parseLong(toValidate);
		} catch (NumberFormatException e) {
			isLong = false;
		}
		
		return isLong;
	}
	private boolean isNCName(final String toValidate) {
		return this.patterns.get(XSDTypeName.NCName).matcher(toValidate).matches();
	}
	private boolean isNMToken(final String toValidate) {
		return this.patterns.get(XSDTypeName.NMTOKEN).matcher(toValidate).matches();
	}
	private boolean isNonNegativeInteger(final String toValidate) {
		return this.patterns.get(XSDTypeName.nonNegativeInteger).matcher(toValidate).matches();
	}
	private boolean isToken(final String toValidate) {
		
		if (toValidate.contains("\n")
			|| toValidate.contains("\t")
			|| toValidate.contains("\r")
			|| !toValidate.trim().equals(toValidate)) {
			return false;
		}
		for (String token : splitListType(toValidate)) {
			if (!isNMToken(token)) {
				return false;
			}
		}
		
		return true;
	}

	private boolean isUnsignedInt(final String toValidate) {
		boolean isUnsignedInt = isUnsignedLong(toValidate);
		try {
			double value = Double.parseDouble(toValidate);
			if (value > SimpleTypeValidator.UNSIGNED_INT_MAX) {
				isUnsignedInt = false;
			}
		} catch (NumberFormatException e) {
			isUnsignedInt = false;
		}
		
		return isUnsignedInt;
	}
	private boolean isUnsignedLong(final String toValidate) {
		boolean isUnsignedLong = isNonNegativeInteger(toValidate);
		
		try {
			/* Need to work with double here because Java doesn't support 
			 * unsigned types. 
			 */
			double value = Double.parseDouble(toValidate);
			if (!(value >= 0.0 && value <= SimpleTypeValidator.UNSIGNED_LONG_MAX)) {
				isUnsignedLong = false;
			}
		} catch (NumberFormatException e) {
			isUnsignedLong = false;
		}
		
		return isUnsignedLong;
	}
	
	private List<String> splitListType(final String toSplit) {
		return Arrays.asList(toSplit.split(" "));
	}
	
	private class TypeRestrictionValidator {
		
		/** 
		 * Key: type, Value: type's base type. 
		 * Value is null if type has no base type (root, anyType) 
		 */
		private final Map<XSDTypeName, XSDTypeName> predefinedTypeHierarchy  = new HashMap<XSDTypeName, XSDTypeName>();
		
		public TypeRestrictionValidator() {
			// Build up the predefined type hierarchy:
			this.predefinedTypeHierarchy.put(XSDTypeName.anyType, null);
			this.predefinedTypeHierarchy.put(XSDTypeName.anySimpleType, XSDTypeName.anyType);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.duration, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.dateTime, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.time, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.date, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.gYearMonth, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.gYear, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.gMonthDay, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.gDay, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.gMonth, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.boolean_, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.base64Binary, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.hexBinary, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.float_, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.double_, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.anyURI, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.QName, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.NOTATION, XSDTypeName.anySimpleType);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.string, XSDTypeName.anySimpleType);
			this.predefinedTypeHierarchy.put(XSDTypeName.decimal, XSDTypeName.anySimpleType);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.normalizedString, XSDTypeName.string);
			this.predefinedTypeHierarchy.put(XSDTypeName.token, XSDTypeName.normalizedString);
			this.predefinedTypeHierarchy.put(XSDTypeName.language, XSDTypeName.token);
			this.predefinedTypeHierarchy.put(XSDTypeName.Name, XSDTypeName.token);
			this.predefinedTypeHierarchy.put(XSDTypeName.NMTOKEN, XSDTypeName.token);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.NCName, XSDTypeName.Name);
			this.predefinedTypeHierarchy.put(XSDTypeName.NMTOKENS, XSDTypeName.NMTOKEN);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.ID, XSDTypeName.NCName);
			this.predefinedTypeHierarchy.put(XSDTypeName.IDREF, XSDTypeName.NCName);
			this.predefinedTypeHierarchy.put(XSDTypeName.ENTITY, XSDTypeName.NCName);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.IDREFS, XSDTypeName.IDREF);
			this.predefinedTypeHierarchy.put(XSDTypeName.ENTITIES, XSDTypeName.ENTITY);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.integer, XSDTypeName.decimal);
			this.predefinedTypeHierarchy.put(XSDTypeName.nonPositiveInteger, XSDTypeName.integer);
			this.predefinedTypeHierarchy.put(XSDTypeName.long_, XSDTypeName.integer);
			this.predefinedTypeHierarchy.put(XSDTypeName.nonNegativeInteger, XSDTypeName.integer);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.negativeInteger, XSDTypeName.nonPositiveInteger);
			this.predefinedTypeHierarchy.put(XSDTypeName.int_, XSDTypeName.long_);
			this.predefinedTypeHierarchy.put(XSDTypeName.unsignedLong, XSDTypeName.nonNegativeInteger);
			this.predefinedTypeHierarchy.put(XSDTypeName.positiveInteger, XSDTypeName.nonNegativeInteger);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.short_, XSDTypeName.int_);
			this.predefinedTypeHierarchy.put(XSDTypeName.unsignedInt, XSDTypeName.unsignedLong);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.byte_, XSDTypeName.short_);
			this.predefinedTypeHierarchy.put(XSDTypeName.unsignedShort, XSDTypeName.unsignedInt);
			
			this.predefinedTypeHierarchy.put(XSDTypeName.unsignedByte, XSDTypeName.unsignedShort);
		}
		
		public boolean validate(final Node node, 
								final NodeConstraint constraint) {
			String nodeValue = XMLUtility.getNodeValue(node);
			if (nodeValue == null) {
				nodeValue = "";
			}

			boolean valid = true;
			if (constraint.hasTypeRestriction()) {
				TypeRestriction restriction = constraint.getTypeRestriction();
				List<String> enumeration = restriction.getEnumeration();
				if (enumeration != null) {
					valid = valid && validEnumeration(nodeValue, enumeration); 
				}
				
				XSDTypeName baseType = XSDTypeName.valueOfSave(restriction.getBaseType());
				if (baseType == null) {
					return false;
				}
				
				String restrictionFacet = restriction.getLength();
				if (restrictionFacet != null) {
					valid = valid && validLength(nodeValue, restrictionFacet, true);
				}
				restrictionFacet = restriction.getMinLength();
				if (restrictionFacet != null) {
					valid = valid && validMinLength(nodeValue, restrictionFacet, true);
				}
				restrictionFacet = restriction.getMaxLength();
				if (restrictionFacet != null) {
					valid = valid && validMaxLength(nodeValue, restrictionFacet, true);
				}
				
				restrictionFacet = restriction.getPattern();
				if (restrictionFacet != null) {
					valid = valid && validPattern(nodeValue, restrictionFacet);
				}
				
				restrictionFacet = restriction.getWhiteSpace();
				if (restrictionFacet != null) {
					valid = valid && validWhiteSpace(nodeValue, baseType, restrictionFacet, true);
				}
				
				restrictionFacet = restriction.getMaxInclusive();
				if (restrictionFacet != null) {
					valid = valid && validMaxInclusive(nodeValue, restrictionFacet, true);
				}
				restrictionFacet = restriction.getMaxExclusive();
				if (restrictionFacet != null) {
					valid = valid && validMaxExclusive(nodeValue, restrictionFacet, true);
				}
				restrictionFacet = restriction.getMinExclusive();
				if (restrictionFacet != null) {
					valid = valid && validMinExclusive(nodeValue, restrictionFacet, true);
				}
				restrictionFacet = restriction.getMinInclusive();
				if (restrictionFacet != null) {
					valid = valid && validMinInclusive(nodeValue, restrictionFacet, true);
				}
				
				restrictionFacet = restriction.getTotalDigits();
				if (restrictionFacet != null) {
					valid = valid && validTotalDigits(nodeValue, restrictionFacet, true);
				}
				restrictionFacet = restriction.getFractionDigits();
				if (restrictionFacet != null) {
					valid = valid && validFractionDigits(nodeValue, restrictionFacet, true);
				}
			}
			
			return valid;
		}
		
		private boolean isDerivedFrom(final XSDTypeName someType,
									  final XSDTypeName possibleBaseType) {

			if (someType == possibleBaseType) {
				return true;
			}

			XSDTypeName baseType = this.predefinedTypeHierarchy.get(someType);
			if (baseType != null) {
				return isDerivedFrom(baseType, possibleBaseType);
			}

			return false;
		}
		
		private boolean validLength(final String value, 
									final String length, 
									final boolean fixed) {
			
			int lengthParsed;
			
			try {
				if (patterns.get(XSDTypeName.nonNegativeInteger).matcher(length).matches()) {
					lengthParsed = Integer.parseInt(length);
					
					// TODO: handle haxBinary and base64Binary (length == number of octets) seperately
					if (value.length() == lengthParsed) {
						return true;
					}
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return false;
		}
		private boolean validMinLength(final String value,
									   final String minLength,
									   final boolean fixed) {
			int minLengthParsed;
			
			try {
				if (patterns.get(XSDTypeName.nonNegativeInteger).matcher(minLength).matches()) {
					minLengthParsed = Integer.parseInt(minLength);
					
					// TODO: handle haxBinary and base64Binary (length == number of octets) seperately
					if (value.length() >= minLengthParsed) {
						return true;
					}
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return false;
		}
		private boolean validMaxLength(final String value,
									   final String maxLength,
									   final boolean fixed) {
			int maxLengthParsed;
			
			try {
				if (patterns.get(XSDTypeName.nonNegativeInteger).matcher(maxLength).matches()) {
					maxLengthParsed = Integer.parseInt(maxLength);
					
					// TODO: handle hexBinary and base64Binary (length == number of octets) seperately
					if (value.length() <= maxLengthParsed) {
						return true;
					}
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return false;
		}
		
		private boolean validPattern(final String value,
									 final String pattern) {
			return value.matches(pattern.replace("\\c+", NMTOKEN_PATTERN));
		}
		private boolean validEnumeration(final String value,
									     final List<String> enumeration) {
			return enumeration.contains(value);
		}
		private boolean validWhiteSpace(final String value,
										final XSDTypeName baseType,
										final String whiteSpaceProperty,
										final boolean fixed) {
			
			if (whiteSpaceProperty.equals("preserve")
				|| baseType == XSDTypeName.string) {
				return true;
			}
			if (whiteSpaceProperty.equals("replace")) {
				if (value.contains("\t") || value.contains("\n") || value.contains("\r")) {
					return false;
				} else {
					return true;
				}
			}
			if (whiteSpaceProperty.equals("collapse") 
				|| !isDerivedFrom(baseType, XSDTypeName.string)) {
				if (!value.matches("[\\x20]{2,}")    // contains no sequences of space characters
					&& value.equals(value.trim())) { // no leading or trailing spaces
					return true;
				}
			}
			return false;
		}
		
		private boolean validMinInclusive(final String value,
										  final String border,
										  final boolean fixed) {
			double valueParsed;
			double borderParsed;
			
			try {
				valueParsed = Double.parseDouble(value);
				borderParsed = Double.parseDouble(border);
				if (valueParsed >= borderParsed) {
					return true;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return false;
		}
		private boolean validMinExclusive(final String value,
										  final String border,
										  final boolean fixed) {
			double valueParsed;
			double borderParsed;
			
			try {
				valueParsed = Double.parseDouble(value);
				borderParsed = Double.parseDouble(border);
				if (valueParsed > borderParsed) {
					return true;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return false;
		}
		private boolean validMaxInclusive(final String value,
										  final String border,
										  final boolean fixed) {
			double valueParsed;
			double borderParsed;
			
			try {
				valueParsed = Double.parseDouble(value);
				borderParsed = Double.parseDouble(border);
				if (valueParsed <= borderParsed) {
					return true;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			
			return false;
		}
		private boolean validMaxExclusive(final String value,
										  final String border,
										  final boolean fixed) {
			double valueParsed;
			double borderParsed;
			
			try {
				valueParsed = Double.parseDouble(value);
				borderParsed = Double.parseDouble(border);
				if (valueParsed < borderParsed) {
					return true;
				}
			} catch (NumberFormatException e) {
			}
			
			return false;
		}
		
		private boolean validTotalDigits(final String value,
										 final String totalDigits,
										 final boolean fixed) {
			double valueParsed;
			int totalDigitsParsed;
			
			try {
				valueParsed = Double.parseDouble(value);
				totalDigitsParsed = Integer.parseInt(totalDigits);
				
				DecimalFormat format = new DecimalFormat("#*.#*");
				String formattedValue = format.format(valueParsed).replace(".", "");
				if (formattedValue.length() == totalDigitsParsed) {
					return true;
				}
			} catch (NumberFormatException e) {
			}
			
			return false;
		}
		private boolean validFractionDigits(final String value,
											final String fractionDigits,
											final boolean fixed) {
			double valueParsed;
			int fractionDigitsParsed;
			
			try {
				valueParsed = Double.parseDouble(value);
				fractionDigitsParsed = Integer.parseInt(fractionDigits);
				
				DecimalFormat format = new DecimalFormat("#*.#*");
				String formattedValue = format.format(valueParsed);
				List<String> numberParts = Arrays.asList(formattedValue.split("\\."));
				if (numberParts.size() == 2
					&& numberParts.get(1).length() == fractionDigitsParsed) {
					return true;
				}
				if (numberParts.size() == 1
					&& formattedValue.startsWith(".")
					&& numberParts.get(0).length() == fractionDigitsParsed) {
					return true;
				}
			} catch (NumberFormatException e) {
			}
			
			return false;
		}
	}

}
