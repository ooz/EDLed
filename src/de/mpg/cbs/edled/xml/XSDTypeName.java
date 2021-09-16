package de.mpg.cbs.edled.xml;

/**
 * Enum of all 46 predefined XSD types.
 * 
 * @author Oliver Z.
 */
public enum XSDTypeName {
	/* Type roots. */
	anyType,
	anySimpleType,
	
	/* Time/date types. */
	duration,   		// Type check not yet implemented.
	dateTime,			// Type check not yet implemented.
	time,				// Type check not yet implemented.
	date,				// Type check not yet implemented.
	gYearMonth,			// Type check not yet implemented.
	gYear,				// Type check not yet implemented.
	gMonthDay,			// Type check not yet implemented.
	gDay,				// Type check not yet implemented.
	gMonth,				// Type check not yet implemented.
	
	/* Misc types. */
	boolean_,
	base64Binary,		// Type check not yet implemented.
	hexBinary,			// Type check not yet implemented.
	float_,
	double_,
	anyURI,
	QName,
	NOTATION,
	
	/* String/character types. */
	string,
	normalizedString,
	token,
	language,			// Type check not yet implemented.
	Name,				// Type check not yet implemented.
	NMTOKEN,
	NMTOKENS,			// Type check not yet implemented.
	NCName,
	ID,					// Type check not yet implemented.
	IDREF,				// Type check not yet implemented.
	IDREFS,				// Type check not yet implemented.
	ENTITY,				// Type check not yet implemented.
	ENTITIES,			// Type check not yet implemented.
	
	/* Numerical types. */
	decimal,
	integer,
	nonPositiveInteger,	// Type check not yet implemented.
	long_,				// Type check not yet implemented.
	nonNegativeInteger,
	negativeInteger,
	int_,
	unsignedLong,
	positiveInteger,	// Type check not yet implemented.	
	short_,				// Type check not yet implemented.
	unsignedInt,
	byte_,				// Type check not yet implemented.
	unsignedShort,		// Type check not yet implemented.
	unsignedByte;		// Type check not yet implemented.
	
	/**
	 * Converts a string value into a XSDTypeName enum value.
	 * Should be used instead of valueOf, because it catches the
	 * escaped versions:
	 * String "float" translates correctly to float_ with this method.
	 * In contras: valueOf would throw an exception.
	 * 
	 * @param typeNameString A XSDType-String that should be converted into a
	 * 						 XSDTypeName enum value.
	 * @return 				 XSDTypeName representing the given typeNameString.
	 * 					     Null if no representation is found.
	 */
	public static XSDTypeName valueOfSave(final String typeNameString) {
		try {
			return XSDTypeName.valueOf(typeNameString);
		} catch (Exception e) {
			if (typeNameString.equals("boolean")) {
				return XSDTypeName.boolean_;
			}
			if (typeNameString.equals("float")) {
				return XSDTypeName.float_;
			}
			if (typeNameString.equals("double")) {
				return XSDTypeName.double_;
			}
			if (typeNameString.equals("long")) {
				return XSDTypeName.long_;
			}
			if (typeNameString.equals("int")) {
				return XSDTypeName.int_;
			}
			if (typeNameString.equals("short")) {
				return XSDTypeName.short_;
			}
			if (typeNameString.equals("byte")) {
				return XSDTypeName.byte_;
			}
			
			return null;
		}
	}
	
	@Override
	public String toString() {
		return super.toString().replace("_", "");
	}
}
